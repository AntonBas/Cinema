package ua.lviv.bas.cinema.service.cinema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.specification.SessionSpecification;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
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
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "sessions")
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionMapper sessionMapper;
	private final MovieRepository movieRepository;
	private final CinemaHallService cinemaHallService;
	private final SessionSpecification sessionSpecification;

	@Cacheable(key = "'public-schedule-' + #filter.hashCode() + '-' + #pageable")
	public Page<SessionScheduleResponse> getScheduleSessions(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting public schedule sessions - filter: {}", filter);

		Specification<Session> spec = sessionSpecification.build(filter)
				.and((root, query, cb) -> cb.equal(root.get("status"), CinemaSessionStatus.SCHEDULED))
				.and((root, query, cb) -> cb.greaterThan(root.get("startTime"), LocalDateTime.now()));

		return sessionRepository.findAll(spec, pageable).map(this::toScheduleResponse);
	}

	@Cacheable(key = "'public-' + #id")
	public SessionScheduleResponse getSessionByIdForPublic(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (session.getStatus() != CinemaSessionStatus.SCHEDULED
				|| session.getStartTime().isBefore(LocalDateTime.now())) {
			throw new SessionNotFoundException(id);
		}

		return toScheduleResponse(session);
	}

	@Cacheable(key = "'admin-' + #filter.hashCode() + '-' + #pageable")
	public Page<SessionAdminResponse> getSessionsForAdmin(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting admin sessions - filter: {}", filter);

		return sessionRepository.findAll(sessionSpecification.build(filter), pageable).map(this::toAdminResponse);
	}

	@Cacheable(key = "'admin-' + #id")
	public SessionAdminResponse getSessionById(Long id) {
		return sessionRepository.findById(id).map(this::toAdminResponse)
				.orElseThrow(() -> new SessionNotFoundException(id));
	}

	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
		return sessionRepository.existsConflictingSession(hallId, startTime, endTime, excludeSessionId);
	}

	@Caching(evict = { @CacheEvict(allEntries = true, cacheNames = "sessions"),
			@CacheEvict(allEntries = true, cacheNames = "movies"),
			@CacheEvict(allEntries = true, cacheNames = "halls") })
	@Transactional
	public SessionAdminResponse createSession(SessionCreateRequest request) {
		validateStartTime(request.getStartTime());

		Movie movie = movieRepository.findById(request.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException(request.getMovieId()));

		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());
		validateMovieAvailability(movie, request.getStartTime());

		LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());
		validateNoTimeConflict(hall.getId(), request.getStartTime(), endTime, null);

		Session session = sessionMapper.toSession(request);
		session.setMovie(movie);
		session.setHall(hall);

		Session saved = sessionRepository.save(session);
		log.info("Session created with ID: {}", saved.getId());

		return toAdminResponse(saved);
	}

	@Caching(evict = { @CacheEvict(allEntries = true, cacheNames = "sessions"),
			@CacheEvict(allEntries = true, cacheNames = "movies"),
			@CacheEvict(allEntries = true, cacheNames = "halls") })
	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionUpdateRequest request) {
		Session session = sessionRepository.findByIdWithLock(id).orElseThrow(() -> new SessionNotFoundException(id));

		LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : session.getStartTime();
		Movie movie = request.getMovieId() != null ? movieRepository.findById(request.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException(request.getMovieId())) : session.getMovie();
		CinemaHall hall = request.getHallId() != null ? cinemaHallService.getHallEntityById(request.getHallId())
				: session.getHall();

		if (request.getStartTime() != null || request.getMovieId() != null || request.getHallId() != null) {
			validateStartTime(startTime);
			validateMovieAvailability(movie, startTime);

			LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());
			validateNoTimeConflict(hall.getId(), startTime, endTime, id);
		}

		sessionMapper.updateSessionFromRequest(request, session);

		if (request.getMovieId() != null) {
			session.setMovie(movie);
		}

		if (request.getHallId() != null) {
			session.setHall(hall);
		}

		Session updated = sessionRepository.save(session);
		log.info("Session updated with ID: {}", updated.getId());

		return toAdminResponse(updated);
	}

	@Caching(evict = { @CacheEvict(allEntries = true, cacheNames = "sessions"),
			@CacheEvict(allEntries = true, cacheNames = "movies"),
			@CacheEvict(allEntries = true, cacheNames = "halls") })
	@Transactional
	public void deleteSession(Long id) {
		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}

		sessionRepository.deleteById(id);
		log.info("Session deleted with ID: {}", id);
	}

	@Caching(evict = { @CacheEvict(allEntries = true, cacheNames = "sessions"),
			@CacheEvict(allEntries = true, cacheNames = "movies"),
			@CacheEvict(allEntries = true, cacheNames = "halls") })
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
		log.info("Session cancelled with ID: {}", sessionId);
	}

	@Caching(evict = { @CacheEvict(allEntries = true, cacheNames = "sessions"),
			@CacheEvict(allEntries = true, cacheNames = "movies"),
			@CacheEvict(allEntries = true, cacheNames = "halls") })
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

		LocalDateTime endTime = session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
		validateNoTimeConflict(session.getHall().getId(), session.getStartTime(), endTime, sessionId);

		session.setStatus(CinemaSessionStatus.SCHEDULED);
		sessionRepository.save(session);
		log.info("Session reactivated with ID: {}", sessionId);
	}

	private SessionAdminResponse toAdminResponse(Session session) {
		SessionAdminResponse response = sessionMapper.toSessionAdminResponse(session);
		response.setEndTime(calculateEndTime(session));

		int hallCapacity = calculateHallCapacity(session);
		int ticketsSold = calculateTicketsSold(session);

		response.setHallCapacity(hallCapacity);
		response.setTicketsSold(ticketsSold);
		response.setTotalRevenue(calculateRevenue(session.getBasePrice(), ticketsSold));

		return response;
	}

	private SessionScheduleResponse toScheduleResponse(Session session) {
		SessionScheduleResponse response = sessionMapper.toSessionScheduleResponse(session);
		response.setEndTime(calculateEndTime(session));

		int hallCapacity = calculateHallCapacity(session);
		int ticketsSold = calculateTicketsSold(session);

		response.setAvailableSeats(Math.max(0, hallCapacity - ticketsSold));
		response.setHallCapacity(hallCapacity);

		return response;
	}

	private int calculateHallCapacity(Session session) {
		return (int) session.getHall().getSeats().stream().filter(seat -> seat.isActive()).count();
	}

	private int calculateTicketsSold(Session session) {
		return (int) session.getBookedSeats().stream().filter(bs -> bs.getStatus() == BookedSeatStatus.CONFIRMED)
				.count();
	}

	private BigDecimal calculateRevenue(BigDecimal basePrice, int ticketsSold) {
		return ticketsSold > 0 ? basePrice.multiply(BigDecimal.valueOf(ticketsSold)) : BigDecimal.ZERO;
	}

	private LocalDateTime calculateEndTime(Session session) {
		if (session.getMovie() == null || session.getMovie().getDurationMinutes() == null) {
			return null;
		}
		return session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
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