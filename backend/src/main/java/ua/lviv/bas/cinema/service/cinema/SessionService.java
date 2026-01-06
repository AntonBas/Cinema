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
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
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

		if (hasTimeConflict(hall.getId(), request.getStartTime(), movie.getDurationMinutes(), null)) {
			throw new SessionTimeConflictException(hall.getId(), request.getStartTime());
		}

		Session session = sessionMapper.toEntity(request);
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
		if (session.getStatus() != CinemaSessionStatus.SCHEDULED
				|| session.getStartTime().isBefore(LocalDateTime.now())) {
			throw new SessionNotFoundException(id);
		}
		return toScheduleResponse(session);
	}

	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionUpdateRequest request) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (request.getStartTime() != null) {
			validateStartTime(request.getStartTime());
			Movie movie = session.getMovie();
			if (movie == null && request.getMovieId() != null) {
				movie = movieRepository.findById(request.getMovieId())
						.orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));
			}
			CinemaHall hall = session.getHall();
			if (hall == null && request.getHallId() != null) {
				hall = cinemaHallService.getHallEntityById(request.getHallId());
			}
			if (movie != null && hall != null) {
				validateMovieAvailability(movie, request.getStartTime());
				if (hasTimeConflict(hall.getId(), request.getStartTime(), movie.getDurationMinutes(), id)) {
					throw new SessionTimeConflictException(hall.getId(), request.getStartTime());
				}
			}
		}

		sessionMapper.updateEntityFromDto(request, session);

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
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		if (session.getStatus() == CinemaSessionStatus.CANCELLED) {
			return;
		}

		if (!CinemaSessionStatus.isActive(session.getStatus())) {
			throw new IllegalStateException("Cannot cancel inactive session");
		}

		if (session.getStartTime().minusHours(1).isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("Cannot cancel session less than 1 hour before start");
		}

		session.setStatus(CinemaSessionStatus.CANCELLED);
		sessionRepository.save(session);
	}

	@Transactional
	public void reactivateSession(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		if (session.getStatus() != CinemaSessionStatus.CANCELLED) {
			throw new IllegalStateException("Only cancelled sessions can be reactivated");
		}

		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("Cannot reactivate past session");
		}

		Movie movie = session.getMovie();
		CinemaHall hall = session.getHall();

		if (hasTimeConflict(hall.getId(), session.getStartTime(), movie.getDurationMinutes(), sessionId)) {
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
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
		List<Session> conflictingSessions = sessionRepository.findConflictingSessions(hallId, startTime, endTime,
				excludeSessionId);
		return !conflictingSessions.isEmpty();
	}

	@Transactional(readOnly = true)
	public List<Session> findSessionsToStart() {
		LocalDateTime now = LocalDateTime.now();
		return sessionRepository.findByStatusAndStartTimeBefore(CinemaSessionStatus.SCHEDULED, now);
	}

	@Transactional(readOnly = true)
	public List<Session> findSessionsToComplete() {
		LocalDateTime now = LocalDateTime.now();
		return sessionRepository.findByStatusAndEndTimeBefore(CinemaSessionStatus.ONGOING, now);
	}

	public SessionAdminResponse toAdminResponse(Session session) {
		SessionAdminResponse response = sessionMapper.toAdminDto(session);
		response.setEndTime(getEndTime(session));

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
		SessionScheduleResponse response = sessionMapper.toScheduleDto(session);
		response.setEndTime(getEndTime(session));

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
		response.setHallCapacity(availableSeats + "/" + hallCapacity);

		return response;
	}

	public LocalDateTime getEndTime(Session session) {
		if (session == null || session.getMovie() == null || session.getMovie().getDurationMinutes() == null
				|| session.getStartTime() == null) {
			throw new IllegalStateException("Cannot calculate end time: missing data");
		}
		return session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
	}

	private void validateStartTime(LocalDateTime startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("Start time is required");
		}
		if (startTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
			throw new IllegalArgumentException("Session must start at least 30 minutes from now");
		}
	}

	private void validateMovieAvailability(Movie movie, LocalDateTime sessionStartTime) {
		LocalDate sessionDate = sessionStartTime.toLocalDate();
		if (sessionDate.isBefore(movie.getReleaseDate())) {
			throw new IllegalArgumentException("Movie '" + movie.getTitle() + "' releases on " + movie.getReleaseDate()
					+ " - cannot create session for " + sessionDate);
		}
		if (movie.getEndShowingDate() != null && sessionDate.isAfter(movie.getEndShowingDate())) {
			throw new IllegalArgumentException("Movie '" + movie.getTitle() + "' ended showing on "
					+ movie.getEndShowingDate() + " - cannot create session for " + sessionDate);
		}
	}
}