package ua.lviv.bas.cinema.service.common;

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
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
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
import ua.lviv.bas.cinema.service.query.SessionQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionQueryService sessionQueryService;
	private final SessionMapper sessionMapper;
	private final MovieRepository movieRepository;
	private final CinemaHallService cinemaHallService;

	@Transactional
	public SessionAdminResponse createSession(SessionCreateRequest request) {
		log.info("Creating session: movieId={}, hallId={}, startTime={}", request.getMovieId(), request.getHallId(),
				request.getStartTime());

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
		log.info("Session created successfully: id={}", saved.getId());

		return toAdminResponse(saved);
	}

	@Transactional(readOnly = true)
	public SessionAdminResponse getSessionById(Long id) {
		log.debug("Retrieving session by id: {}", id);
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		return toAdminResponse(session);
	}

	@Transactional(readOnly = true)
	public SessionScheduleResponse getSessionByIdForPublic(Long id) {
		log.debug("Retrieving session for public by id: {}", id);
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (session.getStatus() != CinemaSessionStatus.SCHEDULED
				|| session.getStartTime().isBefore(LocalDateTime.now())) {
			throw new SessionNotFoundException(id);
		}

		return toScheduleResponse(session);
	}

	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionUpdateRequest request) {
		log.info("Updating session: id={}", id);

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
		log.info("Session updated successfully: id={}", id);

		return toAdminResponse(updated);
	}

	@Transactional
	public SessionAdminResponse updateSessionStatus(Long id, CinemaSessionStatus status) {
		log.info("Updating session status: id={}, status={}", id, status);

		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		validateStatusUpdate(session, status);
		session.setStatus(status);

		Session updated = sessionRepository.save(session);
		log.info("Session status updated successfully: id={}, newStatus={}", id, status);

		return toAdminResponse(updated);
	}

	@Transactional
	public void deleteSession(Long id) {
		log.info("Deleting session: id={}", id);

		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}

		sessionRepository.deleteById(id);
		log.info("Session deleted successfully: id={}", id);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getAllSessionsForAdmin(Pageable pageable, String search) {
		log.debug("Retrieving all sessions for admin with pagination");
		Page<Session> sessions = sessionQueryService.findByMovieTitle(search, pageable, true);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByDateForAdmin(LocalDate date, Pageable pageable) {
		log.debug("Retrieving sessions for date for admin: {} with pagination", date);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(startOfDay, endOfDay, pageable, true);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByHallForAdmin(Long hallId, Pageable pageable) {
		log.debug("Retrieving sessions for hall for admin: {} with pagination", hallId);
		Page<Session> sessions = sessionQueryService.findByHallId(hallId, pageable, true);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByMovieForAdmin(Long movieId, Pageable pageable) {
		log.debug("Retrieving sessions for movie for admin: {} with pagination", movieId);
		Page<Session> sessions = sessionQueryService.findByMovieId(movieId, pageable, true);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByStatus(CinemaSessionStatus status, Pageable pageable) {
		log.debug("Retrieving sessions by status: {} with pagination", status);
		Page<Session> sessions = sessionQueryService.findByStatus(status, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getAvailableSessionsForAdmin(Pageable pageable) {
		log.debug("Retrieving available sessions for admin with pagination");
		Page<Session> sessions = sessionQueryService.findAvailableSessions(pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getFilteredSessions(SessionFilter filter) {
		log.debug("Retrieving filtered sessions with filter: {}", filter);
		Page<Session> sessions = sessionQueryService.findFilteredSessions(filter);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getScheduleSessions(Pageable pageable) {
		log.debug("Retrieving schedule sessions with pagination");
		Page<Session> sessions = sessionQueryService.findAvailableSessions(pageable);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getScheduleSessionsByDate(LocalDate date, Pageable pageable) {
		log.debug("Retrieving schedule sessions for date: {} with pagination", date);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(startOfDay, endOfDay, pageable, false);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getScheduleSessionsByMovie(Long movieId, Pageable pageable) {
		log.debug("Retrieving schedule sessions for movie: {} with pagination", movieId);
		Page<Session> sessions = sessionQueryService.findByMovieId(movieId, pageable, false);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getUpcomingScheduleSessions(int days, Pageable pageable) {
		log.debug("Retrieving upcoming schedule sessions for next {} days with pagination", days);
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(days);

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(start, end, pageable, false);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
		List<Session> conflictingSessions = sessionQueryService.findConflictingSessions(hallId, startTime, endTime,
				excludeSessionId);
		return !conflictingSessions.isEmpty();
	}

	public SessionAdminResponse toAdminResponse(Session session) {
		SessionAdminResponse response = sessionMapper.toAdminDto(session);

		response.setEndTime(getEndTime(session));

		int bookedSeatsCount = session.getBookedSeats() != null
				? (int) session.getBookedSeats().stream().filter(bs -> bs.getTicket() != null).count()
				: 0;
		response.setTicketsSold(bookedSeatsCount);

		if (bookedSeatsCount > 0) {
			response.setTotalRevenue(session.getBasePrice().multiply(BigDecimal.valueOf(bookedSeatsCount)));
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
			hallCapacity = session.getHall().getSeats().size();
		}

		int bookedSeatsCount = session.getBookedSeats() != null
				? (int) session.getBookedSeats().stream().filter(bs -> bs.getTicket() != null).count()
				: 0;
		int availableSeats = Math.max(0, hallCapacity - bookedSeatsCount);

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

	public boolean isAvailable(Session session) {
		return session != null && session.getStartTime() != null && session.getStartTime().isAfter(LocalDateTime.now())
				&& session.getStatus() == CinemaSessionStatus.SCHEDULED;
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

	private void validateStatusUpdate(Session session, CinemaSessionStatus newStatus) {
		if (session.getStatus() == CinemaSessionStatus.COMPLETED) {
			throw new IllegalStateException("Cannot change status of completed session");
		}

		if (newStatus == CinemaSessionStatus.CANCELLED) {
			if (session.getStartTime().minusHours(1).isBefore(LocalDateTime.now())) {
				throw new IllegalArgumentException("Cannot cancel session less than 1 hour before start");
			}
		}

		if (newStatus == CinemaSessionStatus.COMPLETED && session.getStartTime().isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("Cannot mark future session as completed");
		}
	}
}