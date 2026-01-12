package ua.lviv.bas.cinema.service.cinema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionValidationException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionMapper sessionMapper;
	private final MovieRepository movieRepository;
	private final CinemaHallService cinemaHallService;

	@Transactional
	public SessionAdminResponse createSession(SessionCreateRequest request) {
		validateStartTime(request.getStartTime());
		Movie movie = movieRepository.findById(request.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));
		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());
		validateMovieAvailability(movie, request.getStartTime());

		LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());
		boolean hasConflict = sessionRepository.existsConflictingSession(hall.getId(), request.getStartTime(), endTime,
				null);
		if (hasConflict) {
			throw new SessionTimeConflictException(hall.getId(), request.getStartTime());
		}

		Session session = sessionMapper.toSession(request);
		session.setMovie(movie);
		session.setHall(hall);
		Session saved = sessionRepository.save(session);
		return toAdminResponse(saved);
	}

	@Transactional(readOnly = true)
	public SessionAdminResponse getSessionById(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		return toAdminResponse(session);
	}

	@Transactional(readOnly = true)
	public SessionScheduleResponse getSessionByIdForPublic(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		if (!session.getStatus().isActive() || session.getStartTime().isBefore(LocalDateTime.now())) {
			throw new SessionNotFoundException(id);
		}
		return toScheduleResponse(session);
	}

	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionUpdateRequest request) {
		Session session = sessionRepository.findByIdWithLock(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (request.getStartTime() != null) {
			validateStartTime(request.getStartTime());
			Movie movie = session.getMovie();
			CinemaHall hall = session.getHall();

			if (request.getMovieId() != null) {
				movie = movieRepository.findById(request.getMovieId())
						.orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));
			}

			if (request.getHallId() != null) {
				hall = cinemaHallService.getHallEntityById(request.getHallId());
			}

			if (movie != null && hall != null) {
				validateMovieAvailability(movie, request.getStartTime());
				LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());
				boolean hasConflict = sessionRepository.existsConflictingSession(hall.getId(), request.getStartTime(),
						endTime, id);
				if (hasConflict) {
					throw new SessionTimeConflictException(hall.getId(), request.getStartTime());
				}
			}
		}

		sessionMapper.updateSessionFromRequest(request, session);

		if (request.getMovieId() != null && !request.getMovieId().equals(session.getMovie().getId())) {
			Movie newMovie = movieRepository.findById(request.getMovieId())
					.orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));
			session.setMovie(newMovie);
		}

		if (request.getHallId() != null && !request.getHallId().equals(session.getHall().getId())) {
			CinemaHall newHall = cinemaHallService.getHallEntityById(request.getHallId());
			session.setHall(newHall);
		}

		Session updated = sessionRepository.save(session);
		return toAdminResponse(updated);
	}

	@Transactional
	public void deleteSession(Long id) {
		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}
		sessionRepository.deleteById(id);
	}

	@Transactional
	public void cancelSession(Long sessionId) {
		Session session = sessionRepository.findByIdWithLock(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		if (session.getStatus() == CinemaSessionStatus.CANCELLED) {
			return;
		}

		if (!session.getStatus().isActive()) {
			throw SessionOperationException.cannotCancelInactive();
		}

		if (session.getStartTime().minusHours(1).isBefore(LocalDateTime.now())) {
			throw SessionOperationException.cannotCancelTooLate();
		}

		session.setStatus(CinemaSessionStatus.CANCELLED);
		sessionRepository.save(session);
	}

	@Transactional
	public void reactivateSession(Long sessionId) {
		Session session = sessionRepository.findByIdWithLock(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		if (session.getStatus() != CinemaSessionStatus.CANCELLED) {
			throw SessionOperationException.onlyCancelledCanBeReactivated();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw SessionOperationException.cannotReactivatePast();
		}

		Movie movie = session.getMovie();
		CinemaHall hall = session.getHall();

		LocalDateTime endTime = session.getStartTime().plusMinutes(movie.getDurationMinutes());
		boolean hasConflict = sessionRepository.existsConflictingSession(hall.getId(), session.getStartTime(), endTime,
				sessionId);
		if (hasConflict) {
			throw new SessionTimeConflictException(hall.getId(), session.getStartTime());
		}

		session.setStatus(CinemaSessionStatus.SCHEDULED);
		sessionRepository.save(session);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsForAdmin(String search, LocalDate date, Long hallId, Long movieId,
			CinemaSessionStatus status, Pageable pageable) {

		Page<Session> sessions;

		if (search != null && !search.isBlank()) {
			sessions = sessionRepository.findByMovieTitle(search, true, pageable);
		} else if (date != null) {
			LocalDateTime start = date.atStartOfDay();
			LocalDateTime end = date.atTime(23, 59, 59);
			sessions = sessionRepository.findByStartTimeBetween(start, end, true, pageable);
		} else if (hallId != null) {
			sessions = sessionRepository.findByHallId(hallId, true, pageable);
		} else if (movieId != null) {
			sessions = sessionRepository.findByMovieId(movieId, true, pageable);
		} else if (status != null) {
			sessions = sessionRepository.findByStatus(status, pageable);
		} else {
			sessions = sessionRepository.findAll(pageable);
		}

		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getTodaySessions(Pageable pageable) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(23, 59, 59);

		Page<Session> sessions = sessionRepository.findByStartTimeBetween(startOfDay, endOfDay, true, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getScheduleSessions(LocalDate date, Long movieId, Integer daysAhead,
			Pageable pageable) {

		Page<Session> sessions;

		if (date != null) {
			LocalDateTime start = date.atStartOfDay();
			LocalDateTime end = date.atTime(23, 59, 59);
			sessions = sessionRepository.findByStartTimeBetween(start, end, false, pageable);
		} else if (movieId != null) {
			sessions = sessionRepository.findByMovieId(movieId, false, pageable);
		} else if (daysAhead != null) {
			LocalDateTime start = LocalDateTime.now();
			LocalDateTime end = start.plusDays(daysAhead);
			sessions = sessionRepository.findByStartTimeBetween(start, end, false, pageable);
		} else {
			sessions = sessionRepository.findAvailableSessions(pageable);
		}

		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getTodayPublicSessions(Pageable pageable) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(23, 59, 59);

		Page<Session> sessions = sessionRepository.findByStartTimeBetween(startOfDay, endOfDay, false, pageable);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
		return sessionRepository.existsConflictingSession(hallId, startTime, endTime, excludeSessionId);
	}

	@Transactional(readOnly = true)
	public List<Session> findSessionsToStart() {
		LocalDateTime now = LocalDateTime.now();
		return sessionRepository.findSessionsToStart(now);
	}

	@Transactional(readOnly = true)
	public List<Session> findSessionsToComplete() {
		LocalDateTime now = LocalDateTime.now();
		return sessionRepository.findSessionsToComplete(now);
	}

	public SessionAdminResponse toAdminResponse(Session session) {
		SessionAdminResponse response = sessionMapper.toSessionAdminResponse(session);
		response.setEndTime(calculateEndTime(session));

		int confirmedSeatsCount = session.getBookedSeats() != null
				? (int) session.getBookedSeats().stream().filter(bs -> bs.getStatus() == BookedSeatStatus.CONFIRMED)
						.count()
				: 0;

		response.setTicketsSold(confirmedSeatsCount);

		if (confirmedSeatsCount > 0) {
			response.setTotalRevenue(session.getBasePrice().multiply(BigDecimal.valueOf(confirmedSeatsCount)));
		} else {
			response.setTotalRevenue(BigDecimal.ZERO);
		}

		response.setHallCapacity(
				session.getHall() != null && session.getHall().getSeats() != null ? session.getHall().getSeats().size()
						: 0);

		return response;
	}

	public SessionScheduleResponse toScheduleResponse(Session session) {
		SessionScheduleResponse response = sessionMapper.toSessionScheduleResponse(session);
		response.setEndTime(calculateEndTime(session));

		int hallCapacity = 0;
		if (session.getHall() != null && session.getHall().getSeats() != null) {
			hallCapacity = (int) session.getHall().getSeats().stream().filter(Seat::isActive).count();
		}

		int confirmedSeatsCount = session.getBookedSeats() != null
				? (int) session.getBookedSeats().stream().filter(bs -> bs.getStatus() == BookedSeatStatus.CONFIRMED)
						.count()
				: 0;

		int availableSeats = Math.max(0, hallCapacity - confirmedSeatsCount);
		response.setAvailableSeats(availableSeats);
		response.setHallCapacity(hallCapacity);

		return response;
	}

	private LocalDateTime calculateEndTime(Session session) {
		if (session == null || session.getMovie() == null || session.getMovie().getDurationMinutes() == null
				|| session.getStartTime() == null) {
			return null;
		}
		return session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
	}

	private void validateStartTime(LocalDateTime startTime) {
		if (startTime == null) {
			throw SessionValidationException.startTimeRequired();
		}
		if (startTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
			throw SessionValidationException.tooCloseToStart(startTime);
		}
	}

	private void validateMovieAvailability(Movie movie, LocalDateTime sessionStartTime) {
		LocalDate sessionDate = sessionStartTime.toLocalDate();
		if (sessionDate.isBefore(movie.getReleaseDate())) {
			throw SessionValidationException.movieNotReleased(movie, sessionDate);
		}
		if (movie.getEndShowingDate() != null && sessionDate.isAfter(movie.getEndShowingDate())) {
			throw SessionValidationException.movieEndedShowing(movie, sessionDate);
		}
	}
}