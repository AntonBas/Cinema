package ua.lviv.bas.cinema.service;

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
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.service.query.SessionQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionQueryService sessionQueryService;
	private final SessionMapper sessionMapper;
	private final MovieService movieService;
	private final CinemaHallService cinemaHallService;

	@Transactional
	public SessionAdminResponse createSession(SessionRequest request) {
		log.info("Creating session: movieId={}, hallId={}, startTime={}", request.getMovieId(), request.getHallId(),
				request.getStartTime());

		if (!request.isStartTimeValid()) {
			throw new IllegalArgumentException("Session must start at least 30 minutes from now");
		}

		Movie movie = movieService.getMovieEntityById(request.getMovieId());
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

	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionRequest request) {
		log.info("Updating session: id={}", id);

		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (!request.isStartTimeValid()) {
			throw new IllegalArgumentException("Session must start at least 30 minutes from now");
		}

		Movie movie = movieService.getMovieEntityById(request.getMovieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());

		validateMovieAvailability(movie, request.getStartTime());

		if (hasTimeConflict(hall.getId(), request.getStartTime(), movie.getDurationMinutes(), id)) {
			throw new SessionTimeConflictException(hall.getId(), request.getStartTime());
		}

		session.setStartTime(request.getStartTime());
		session.setBasePrice(request.getBasePrice());
		session.setMovie(movie);
		session.setHall(hall);

		Session updated = sessionRepository.save(session);
		log.info("Session updated successfully: id={}", id);

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
	public Page<SessionAdminResponse> getAllSessions(Pageable pageable, String search) {
		log.debug("Retrieving all sessions with pagination");
		Page<Session> sessions = sessionQueryService.findByMovieTitle(search, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByDate(LocalDate date, Pageable pageable) {
		log.debug("Retrieving sessions for date: {} with pagination", date);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(startOfDay, endOfDay, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByHall(Long hallId, Pageable pageable) {
		log.debug("Retrieving sessions for hall: {} with pagination", hallId);
		Page<Session> sessions = sessionQueryService.findByHallId(hallId, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getSessionsByMovie(Long movieId, Pageable pageable) {
		log.debug("Retrieving sessions for movie: {} with pagination", movieId);
		Page<Session> sessions = sessionQueryService.findByMovieId(movieId, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getAvailableSessions(Pageable pageable) {
		log.debug("Retrieving available sessions with pagination");
		Page<Session> sessions = sessionQueryService.findAvailableSessions(pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getUpcomingSessions(int days, Pageable pageable) {
		log.debug("Retrieving upcoming sessions for next {} days with pagination", days);
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(days);

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(start, end, pageable);
		return sessions.map(this::toAdminResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getTodaySessions(Pageable pageable) {
		log.debug("Retrieving today's sessions with pagination");
		return getSessionsByDate(LocalDate.now(), pageable);
	}

	@Transactional(readOnly = true)
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
		List<Session> conflictingSessions = sessionQueryService.findConflictingSessions(hallId, startTime, endTime,
				excludeSessionId);
		return !conflictingSessions.isEmpty();
	}

	@Transactional(readOnly = true)
	public Page<SessionAdminResponse> getFilteredSessions(LocalDate date, Long hallId, Long movieId, Integer days,
			Pageable pageable) {
		log.debug("Retrieving filtered sessions: date={}, hallId={}, movieId={}, days={}", date, hallId, movieId, days);

		LocalDateTime startTime = null;
		LocalDateTime endTime = null;

		if (date != null) {
			startTime = date.atStartOfDay();
			endTime = date.atTime(23, 59, 59);
		} else if (days != null) {
			startTime = LocalDateTime.now();
			endTime = startTime.plusDays(days);
		}

		Page<Session> sessions = sessionQueryService.findFiltered(startTime, endTime, hallId, movieId, pageable);
		return sessions.map(this::toAdminResponse);
	}

	public SessionAdminResponse toAdminResponse(Session session) {
		SessionAdminResponse response = sessionMapper.toAdminDto(session);

		response.setEndTime(session.getEndTime());
		response.setAvailable(session.isAvailable());

		int ticketsSold = session.getTickets() != null ? session.getTickets().size() : 0;
		response.setTicketsSold(ticketsSold);

		if (ticketsSold > 0) {
			response.setTotalRevenue(session.getBasePrice().multiply(BigDecimal.valueOf(ticketsSold)));
		} else {
			response.setTotalRevenue(BigDecimal.ZERO);
		}

		response.setHallCapacity(session.getHall() != null ? session.getHall().getCapacity() : 0);

		return response;
	}

	public SessionScheduleResponse toScheduleResponse(Session session) {
		SessionScheduleResponse response = sessionMapper.toScheduleDto(session);

		response.setEndTime(session.getEndTime());

		int hallCapacity = 0;
		if (session.getHall() != null) {
			hallCapacity = session.getHall().getCapacity();
		}

		int ticketsSold = session.getTickets() != null ? session.getTickets().size() : 0;
		int availableSeats = Math.max(0, hallCapacity - ticketsSold);

		response.setAvailableSeats(availableSeats);
		response.setHallCapacity(availableSeats + "/" + hallCapacity);

		return response;
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

		Page<Session> sessions = sessionQueryService.findByStartTimeBetween(startOfDay, endOfDay, pageable);
		return sessions.map(this::toScheduleResponse);
	}

	@Transactional(readOnly = true)
	public Page<SessionScheduleResponse> getScheduleSessionsByMovie(Long movieId, Pageable pageable) {
		log.debug("Retrieving schedule sessions for movie: {} with pagination", movieId);
		Page<Session> sessions = sessionQueryService.findByMovieId(movieId, pageable);
		return sessions.map(this::toScheduleResponse);
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