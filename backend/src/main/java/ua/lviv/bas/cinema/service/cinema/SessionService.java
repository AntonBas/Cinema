package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.domain.specification.SessionSpecification;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionValidationException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
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
	private final SeatRepository seatRepository;
	private final SessionSpecification sessionSpecification;

	@Cacheable(key = "'public-schedule-' + #filter.hashCode() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public PageResponse<SessionScheduleResponse> getScheduleSessions(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting public schedule sessions - filter: {}", filter);

		Page<SessionScheduleProjection> page = sessionRepository.findAllScheduleProjections(LocalDateTime.now(),
				pageable);

		Map<Long, Integer> hallCapacities = getHallCapacities(
				page.getContent().stream().map(SessionScheduleProjection::getHallId).collect(Collectors.toList()));

		Map<Long, Integer> availableSeats = sessionRepository.findAvailableSeatsForSessions(
				page.getContent().stream().map(SessionScheduleProjection::getId).collect(Collectors.toList()));

		return PageResponse.from(page.map(projection -> {
			SessionScheduleResponse response = sessionMapper.toSessionScheduleResponse(projection);
			response.setHallCapacity(hallCapacities.get(projection.getHallId()));
			response.setAvailableSeats(availableSeats.get(projection.getId()));
			return response;
		}));
	}

	@Cacheable(key = "'admin-' + #filter.hashCode() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public PageResponse<SessionAdminResponse> getSessionsForAdmin(SessionFilterRequest filter, Pageable pageable) {
		log.info("Getting admin sessions - filter: {}", filter);

		Specification<Session> spec = sessionSpecification.buildForAdmin(filter);
		Page<SessionAdminProjection> page = sessionRepository.findAllAdminProjections(spec, pageable);

		Map<Long, Integer> hallCapacities = getHallCapacities(
				page.getContent().stream().map(SessionAdminProjection::getHallId).collect(Collectors.toList()));

		return PageResponse.from(page.map(projection -> {
			SessionAdminResponse response = sessionMapper.toSessionAdminResponse(projection);
			response.setHallCapacity(hallCapacities.get(projection.getHallId()));
			return response;
		}));
	}

	@Cacheable(key = "'public-' + #id")
	public SessionScheduleResponse getSessionByIdForPublic(Long id) {
		SessionScheduleProjection projection = sessionRepository.findScheduleProjectionById(id)
				.orElseThrow(() -> new SessionNotFoundException(id));
		SessionScheduleResponse response = sessionMapper.toSessionScheduleResponse(projection);
		response.setHallCapacity(getHallCapacity(projection.getHallId()));
		response.setAvailableSeats(getAvailableSeats(projection.getId()));
		return response;
	}

	@Cacheable(key = "'admin-' + #id")
	public SessionAdminResponse getSessionById(Long id) {
		SessionAdminProjection projection = sessionRepository.findAdminProjectionById(id)
				.orElseThrow(() -> new SessionNotFoundException(id));
		SessionAdminResponse response = sessionMapper.toSessionAdminResponse(projection);
		response.setHallCapacity(getHallCapacity(projection.getHallId()));
		return response;
	}

	@Cacheable(key = "'conflict-check-' + #hallId + '-' + #startTime + '-' + #excludeSessionId")
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

		Movie movie = movieRepository.getReferenceById(request.getMovieId());
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

		boolean hasChanges = false;

		if (request.getStartTime() != null && !request.getStartTime().equals(session.getStartTime())) {
			validateStartTime(request.getStartTime());
			session.setStartTime(request.getStartTime());
			hasChanges = true;
		}

		if (request.getBasePrice() != null && !request.getBasePrice().equals(session.getBasePrice())) {
			session.setBasePrice(request.getBasePrice());
			hasChanges = true;
		}

		if (request.getMovieId() != null && !request.getMovieId().equals(session.getMovie().getId())) {
			Movie movie = movieRepository.getReferenceById(request.getMovieId());
			validateMovieAvailability(movie, session.getStartTime());
			session.setMovie(movie);
			hasChanges = true;
		}

		if (request.getHallId() != null && !request.getHallId().equals(session.getHall().getId())) {
			CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());
			session.setHall(hall);
			hasChanges = true;
		}

		if (hasChanges) {
			LocalDateTime endTime = session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
			validateNoTimeConflict(session.getHall().getId(), session.getStartTime(), endTime, id);

			session = sessionRepository.save(session);
			log.info("Session updated with ID: {}", session.getId());
		}

		return getSessionById(session.getId());
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

	private Map<Long, Integer> getHallCapacities(List<Long> hallIds) {
		if (hallIds.isEmpty())
			return Map.of();
		return seatRepository.countSeatsByHallIds(hallIds).stream()
				.collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
	}

	private Integer getHallCapacity(Long hallId) {
		return Long.valueOf(seatRepository.countByHallId(hallId)).intValue();
	}

	private Integer getAvailableSeats(Long sessionId) {
		return sessionRepository.findAvailableSeatsForSessions(List.of(sessionId)).get(sessionId);
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