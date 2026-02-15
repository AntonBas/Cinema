package ua.lviv.bas.cinema.service.cinema;

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
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.specification.SessionSpecification;
import ua.lviv.bas.cinema.dto.common.PageResponse;
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
	public PageResponse<SessionScheduleResponse> getScheduleSessions(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting public schedule sessions - filter: {}", filter);

		Specification<Session> spec = sessionSpecification.buildForSchedule(filter);
		Page<Session> page = sessionRepository.findAll(spec, pageable);

		return PageResponse.from(page.map(sessionMapper::toSessionScheduleResponse));
	}

	@Cacheable(key = "'public-' + #id")
	public SessionScheduleResponse getSessionByIdForPublic(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		return sessionMapper.toSessionScheduleResponse(session);
	}

	@Cacheable(key = "'admin-' + #filter.hashCode() + '-' + #pageable")
	public PageResponse<SessionAdminResponse> getSessionsForAdmin(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting admin sessions - filter: {}", filter);

		Specification<Session> spec = sessionSpecification.buildForAdmin(filter);
		Page<Session> page = sessionRepository.findAll(spec, pageable);

		return PageResponse.from(page.map(sessionMapper::toSessionAdminResponse));
	}

	@Cacheable(key = "'admin-' + #id")
	public SessionAdminResponse getSessionById(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		return sessionMapper.toSessionAdminResponse(session);
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

		return getSessionById(saved.getId());
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

		if (request.getStartTime() != null) {
			session.setStartTime(request.getStartTime());
		}

		Session updated = sessionRepository.save(session);
		log.info("Session updated with ID: {}", updated.getId());

		return getSessionById(updated.getId());
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