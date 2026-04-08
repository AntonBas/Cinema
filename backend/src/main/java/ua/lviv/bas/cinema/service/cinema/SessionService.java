package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionValidationException;
import ua.lviv.bas.cinema.mapper.cinema.SessionMapper;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.repository.cinema.specification.SessionSpecification;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "sessions")
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionMapper sessionMapper;
	private final SessionSpecification sessionSpecification;
	private final MovieRepository movieRepository;
	private final CinemaHallService cinemaHallService;
	private final AuditService auditService;

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true) })
	@Transactional
	public SessionResponse createSession(SessionCreateRequest request) {
		validateStartTime(request.startTime());

		Movie movie = movieRepository.getReferenceById(request.movieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.hallId());

		validateMovieAvailability(movie, request.startTime());
		validateNoTimeConflict(hall.getId(), request.startTime(),
				request.startTime().plusMinutes(movie.getDurationMinutes()), null);

		Session session = sessionMapper.toSession(request);
		session.setMovie(movie);
		session.setHall(hall);

		Session saved = sessionRepository.save(session);
		log.info("Session created with ID: {}", saved.getId());

		Map<String, Object> details = new HashMap<>();
		details.put("movieId", saved.getMovie().getId());
		details.put("movieTitle", saved.getMovie().getTitle());
		details.put("hallId", saved.getHall().getId());
		details.put("hallName", saved.getHall().getName());
		details.put("startTime", saved.getStartTime());
		details.put("basePrice", saved.getBasePrice());
		auditService.logChange("Session", saved.getId(), "Session #" + saved.getId(), AuditAction.CREATED, null,
				details);

		return sessionMapper.toSessionResponse(saved);
	}

	@Cacheable(key = "'public:schedule:' + #searchTerm + ':' + #date")
	public List<SessionScheduleResponse> getScheduleSessions(String searchTerm, LocalDate date) {
		Specification<Session> spec = sessionSpecification.forSchedule(searchTerm, date);
		List<Session> sessions = sessionRepository.findAll(spec);

		List<Long> sessionIds = sessions.stream().map(Session::getId).toList();
		Map<Long, Integer> availableSeats = getAvailableSeatsBatch(sessionIds);

		return sessions.stream().map(session -> {
			SessionScheduleProjection proj = sessionRepository.findScheduleProjectionById(session.getId())
					.orElseThrow();
			return sessionMapper.toSessionScheduleResponse(proj)
					.withAvailableSeats(availableSeats.getOrDefault(session.getId(), 0));
		}).toList();
	}

	@Cacheable(key = "'admin:list:' + #hallId + ':' + #movieTitle + ':' + #status + ':' + #dateFrom + ':' + #dateTo + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
	public PageResponse<SessionAdminResponse> getSessionsForAdmin(Long hallId, String movieTitle,
			CinemaSessionStatus status, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

		Specification<Session> spec = sessionSpecification.forAdmin(hallId, movieTitle, status, dateFrom, dateTo);
		Page<Session> page = sessionRepository.findAll(spec, pageable);

		List<Long> sessionIds = page.getContent().stream().map(Session::getId).toList();
		Map<Long, SessionAdminProjection> projections = sessionRepository.findAdminProjectionsByIds(sessionIds).stream()
				.collect(Collectors.toMap(SessionAdminProjection::getId, p -> p));

		List<SessionAdminResponse> responses = page.getContent().stream()
				.map(session -> sessionMapper.toSessionAdminResponse(projections.get(session.getId()))).toList();

		return PageResponse.from(new PageImpl<>(responses, pageable, page.getTotalElements()));
	}

	@Cacheable(key = "'admin:' + #id")
	public SessionResponse getSessionById(Long id) {
		return sessionRepository.findById(id).map(sessionMapper::toSessionResponse)
				.orElseThrow(() -> new SessionNotFoundException(id));
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true) })
	@Transactional
	public SessionResponse updateSession(Long id, SessionUpdateRequest request) {
		Session session = sessionRepository.findByIdWithLock(id).orElseThrow(() -> new SessionNotFoundException(id));

		Map<String, Object> oldDetails = Map.of("startTime", session.getStartTime(), "basePrice",
				session.getBasePrice(), "movieId", session.getMovie().getId(), "hallId", session.getHall().getId());

		if (request.startTime() != null && !request.startTime().equals(session.getStartTime())) {
			validateStartTime(request.startTime());
		}

		sessionMapper.updateSessionFromRequest(request, session);

		if (request.movieId() != null) {
			validateMovieAvailability(session.getMovie(), session.getStartTime());
		}

		validateNoTimeConflict(session.getHall().getId(), session.getStartTime(),
				session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), id);

		session = sessionRepository.save(session);
		log.info("Session updated with ID: {}", session.getId());

		Map<String, Object> newDetails = Map.of("startTime", session.getStartTime(), "basePrice",
				session.getBasePrice(), "movieId", session.getMovie().getId(), "hallId", session.getHall().getId());

		auditService.logChange("Session", id, "Session #" + id, AuditAction.UPDATED, oldDetails, newDetails);

		return sessionMapper.toSessionResponse(session);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true) })
	@Transactional
	public void deleteSession(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		sessionRepository.deleteById(id);
		log.info("Session deleted with ID: {}", id);

		Map<String, Object> details = new HashMap<>();
		details.put("movieTitle", session.getMovie().getTitle());
		details.put("hallName", session.getHall().getName());
		details.put("startTime", session.getStartTime());
		auditService.logChange("Session", session.getId(), "Session #" + session.getId(), AuditAction.DELETED, details,
				null);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true) })
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

		CinemaSessionStatus oldStatus = session.getStatus();
		session.setStatus(CinemaSessionStatus.CANCELLED);
		sessionRepository.save(session);
		log.info("Session cancelled with ID: {}", sessionId);

		auditService.logChange("Session", sessionId, "Session #" + sessionId, AuditAction.CANCELLED,
				Map.of("status", oldStatus), Map.of("status", CinemaSessionStatus.CANCELLED));
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true) })
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

		validateNoTimeConflict(session.getHall().getId(), session.getStartTime(),
				session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), sessionId);

		CinemaSessionStatus oldStatus = session.getStatus();
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		sessionRepository.save(session);
		log.info("Session reactivated with ID: {}", sessionId);

		auditService.logChange("Session", sessionId, "Session #" + sessionId, AuditAction.REACTIVATED,
				Map.of("status", oldStatus), Map.of("status", CinemaSessionStatus.SCHEDULED));
	}

	private Map<Long, Integer> getAvailableSeatsBatch(List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return Map.of();
		}
		return sessionRepository.findAvailableSeatsBatch(sessionIds).stream()
				.collect(Collectors.toMap(arr -> (Long) arr[0], arr -> ((Number) arr[1]).intValue()));
	}

	private void validateStartTime(LocalDateTime startTime) {
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

	private void validateNoTimeConflict(Long hallId, LocalDateTime startTime, LocalDateTime endTime,
			Long excludeSessionId) {
		if (sessionRepository.existsConflictingSession(hallId, startTime, endTime, excludeSessionId)) {
			throw new SessionTimeConflictException(hallId, startTime);
		}
	}
}