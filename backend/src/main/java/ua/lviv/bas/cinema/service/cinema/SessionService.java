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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
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

	@Cacheable(key = "'public:' + #searchTerm + ':' + #date")
	public List<SessionScheduleResponse> getScheduleSessions(String searchTerm, LocalDate date) {
		LocalDateTime startOfDay;
		LocalDateTime endOfDay;

		if (date == null) {
			LocalDateTime now = LocalDateTime.now();
			startOfDay = now;
			endOfDay = now.plusMonths(3);
		} else {
			startOfDay = date.atStartOfDay();
			endOfDay = date.plusDays(1).atStartOfDay();
		}

		List<SessionScheduleProjection> projections = sessionRepository.findScheduleSessions(startOfDay, endOfDay,
				searchTerm);

		Map<Long, Integer> availableSeats = getAvailableSeatsBatch(
				projections.stream().map(SessionScheduleProjection::getId).collect(Collectors.toList()));

		return projections.stream().map(projection -> {
			SessionScheduleResponse response = sessionMapper.toScheduleResponse(projection);
			return new SessionScheduleResponse(response.id(), response.startTime(), response.endTime(),
					response.basePrice(), availableSeats.getOrDefault(projection.getId(), 0), response.movieId(),
					response.movieTitle(), response.moviePosterFileName(), response.movieAgeRating(),
					response.movieDuration(), response.hallId(), response.hallName(), response.hallCapacity());
		}).collect(Collectors.toList());
	}

	@Cacheable(key = "'admin:' + #hallId + ':' + #movieTitle + ':' + #status + ':' + #dateFrom + ':' + #dateTo + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
	public PageResponse<SessionAdminResponse> getSessionsForAdmin(Long hallId, String movieTitle,
			CinemaSessionStatus status, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

		String statusStr = status != null ? status.name() : null;

		Page<SessionAdminProjection> projectionPage = sessionRepository.findAdminSessionsNative(hallId, movieTitle,
				statusStr, dateFrom, dateTo, pageable);

		List<SessionAdminResponse> responses = projectionPage.getContent().stream().map(sessionMapper::toAdminResponse)
				.collect(Collectors.toList());

		Page<SessionAdminResponse> responsePage = new PageImpl<>(responses, pageable,
				projectionPage.getTotalElements());

		return PageResponse.from(responsePage);
	}

	@Cacheable(key = "'public:' + #id")
	public SessionScheduleResponse getSessionForPublic(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		SessionScheduleResponse response = sessionMapper.toScheduleResponse(session);
		return new SessionScheduleResponse(response.id(), response.startTime(), response.endTime(),
				response.basePrice(), getAvailableSeats(id), response.movieId(), response.movieTitle(),
				response.moviePosterFileName(), response.movieAgeRating(), response.movieDuration(), response.hallId(),
				response.hallName(), response.hallCapacity());
	}

	@Cacheable(key = "'admin:' + #id")
	public SessionAdminResponse getSessionForAdmin(Long id) {
		SessionAdminProjection projection = sessionRepository.findAdminProjectionById(id)
				.orElseThrow(() -> new SessionNotFoundException(id));
		return sessionMapper.toAdminResponse(projection);
	}

	@Cacheable(key = "'available:' + #sessionId")
	public Integer getAvailableSeats(Long sessionId) {
		return getAvailableSeatsBatch(List.of(sessionId)).getOrDefault(sessionId, 0);
	}

	private Map<Long, Integer> getAvailableSeatsBatch(List<Long> sessionIds) {
		if (sessionIds.isEmpty())
			return Map.of();
		return sessionRepository.findAvailableSeatsBatch(sessionIds).stream()
				.collect(Collectors.toMap(arr -> (Long) arr[0], arr -> ((Number) arr[1]).intValue()));
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true),
			@CacheEvict(value = "availableSeatsCount", allEntries = true) })
	@Transactional
	public SessionAdminResponse createSession(SessionCreateRequest request) {
		validateStartTime(request.startTime());

		Movie movie = movieRepository.getReferenceById(request.movieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.hallId());

		validateMovieAvailability(movie, request.startTime());
		validateNoTimeConflict(hall.getId(), request.startTime(),
				request.startTime().plusMinutes(movie.getDurationMinutes()), null);

		Session session = sessionMapper.toEntity(request);
		session.setMovie(movie);
		session.setHall(hall);

		Session saved = sessionRepository.save(session);
		log.info("Session created with ID: {}", saved.getId());

		return getSessionForAdmin(saved.getId());
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true),
			@CacheEvict(value = "availableSeatsCount", allEntries = true) })
	@Transactional
	public SessionAdminResponse updateSession(Long id, SessionUpdateRequest request) {
		Session session = sessionRepository.findByIdWithLock(id).orElseThrow(() -> new SessionNotFoundException(id));

		boolean hasChanges = false;

		if (request.startTime() != null && !request.startTime().equals(session.getStartTime())) {
			validateStartTime(request.startTime());
			session.setStartTime(request.startTime());
			hasChanges = true;
		}

		if (request.basePrice() != null && !request.basePrice().equals(session.getBasePrice())) {
			session.setBasePrice(request.basePrice());
			hasChanges = true;
		}

		if (request.movieId() != null && !request.movieId().equals(session.getMovie().getId())) {
			Movie movie = movieRepository.getReferenceById(request.movieId());
			validateMovieAvailability(movie, session.getStartTime());
			session.setMovie(movie);
			hasChanges = true;
		}

		if (request.hallId() != null && !request.hallId().equals(session.getHall().getId())) {
			CinemaHall hall = cinemaHallService.getHallEntityById(request.hallId());
			session.setHall(hall);
			hasChanges = true;
		}

		if (hasChanges) {
			validateNoTimeConflict(session.getHall().getId(), session.getStartTime(),
					session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), id);
			session = sessionRepository.save(session);
			log.info("Session updated with ID: {}", session.getId());
		}

		return getSessionForAdmin(session.getId());
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true),
			@CacheEvict(value = "availableSeatsCount", allEntries = true) })
	@Transactional
	public void deleteSession(Long id) {
		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}
		sessionRepository.deleteById(id);
		log.info("Session deleted with ID: {}", id);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true),
			@CacheEvict(value = "availableSeatsCount", allEntries = true) })
	@Transactional
	public void cancelSession(Long sessionId) {
		Session session = sessionRepository.findByIdWithLock(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		if (session.getStatus() == CinemaSessionStatus.CANCELLED)
			return;

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

	@Caching(evict = { @CacheEvict(cacheNames = "sessions", allEntries = true),
			@CacheEvict(value = "seatAvailability", allEntries = true),
			@CacheEvict(value = "availableSeatsCount", allEntries = true) })
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