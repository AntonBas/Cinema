package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

	@Cacheable(key = "'public:' + #searchTerm + ':' + #date + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
	public PageResponse<SessionScheduleResponse> getScheduleSessions(String searchTerm, LocalDate date,
			Pageable pageable) {
		Page<SessionScheduleProjection> page = sessionRepository.findUpcomingSessions(LocalDateTime.now(), date,
				pageable);

		if (!page.hasContent()) {
			return PageResponse.from(new PageImpl<>(List.of(), pageable, page.getTotalElements()));
		}

		List<Long> sessionIds = page.getContent().stream().map(SessionScheduleProjection::getId)
				.collect(Collectors.toList());

		Map<Long, Integer> availableSeats = getAvailableSeatsBatch(sessionIds);

		List<SessionScheduleResponse> responses = page.getContent().stream().map(projection -> {
			SessionScheduleResponse response = sessionMapper.toScheduleResponse(projection);
			response.setAvailableSeats(availableSeats.getOrDefault(projection.getId(), 0));
			return response;
		}).collect(Collectors.toList());

		Page<SessionScheduleResponse> responsePage = new PageImpl<>(responses, pageable, page.getTotalElements());

		return PageResponse.from(responsePage);
	}

	@Cacheable(key = "'admin:' + #filter.hashCode() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
	public PageResponse<SessionAdminResponse> getSessionsForAdmin(SessionFilterRequest filter, Pageable pageable) {
		Specification<Session> spec = sessionSpecification.buildForAdmin(filter);
		Page<Long> sessionIdsPage = sessionRepository.findAll(spec, pageable).map(Session::getId);

		if (!sessionIdsPage.hasContent()) {
			return PageResponse.from(new PageImpl<>(List.of(), pageable, sessionIdsPage.getTotalElements()));
		}

		List<SessionAdminProjection> projections = sessionRepository
				.findAdminProjectionsByIds(sessionIdsPage.getContent());

		List<SessionAdminResponse> responses = projections.stream().map(sessionMapper::toAdminResponse)
				.collect(Collectors.toList());

		Page<SessionAdminResponse> responsePage = new PageImpl<>(responses, pageable,
				sessionIdsPage.getTotalElements());

		return PageResponse.from(responsePage);
	}

	@Cacheable(key = "'public:' + #id")
	public SessionScheduleResponse getSessionForPublic(Long id) {
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		SessionScheduleResponse response = sessionMapper.toScheduleResponse(session);
		response.setAvailableSeats(getAvailableSeats(id));
		return response;
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

	@CacheEvict(allEntries = true)
	@Transactional
	public SessionAdminResponse createSession(SessionCreateRequest request) {
		validateStartTime(request.getStartTime());

		Movie movie = movieRepository.getReferenceById(request.getMovieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());

		validateMovieAvailability(movie, request.getStartTime());
		validateNoTimeConflict(hall.getId(), request.getStartTime(),
				request.getStartTime().plusMinutes(movie.getDurationMinutes()), null);

		Session session = sessionMapper.toEntity(request);
		session.setMovie(movie);
		session.setHall(hall);

		Session saved = sessionRepository.save(session);
		log.info("Session created with ID: {}", saved.getId());

		return getSessionForAdmin(saved.getId());
	}

	@CacheEvict(allEntries = true)
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
			validateNoTimeConflict(session.getHall().getId(), session.getStartTime(),
					session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), id);
			session = sessionRepository.save(session);
			log.info("Session updated with ID: {}", session.getId());
		}

		return getSessionForAdmin(session.getId());
	}

	@CacheEvict(allEntries = true)
	@Transactional
	public void deleteSession(Long id) {
		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}
		sessionRepository.deleteById(id);
		log.info("Session deleted with ID: {}", id);
	}

	@CacheEvict(allEntries = true)
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

	@CacheEvict(allEntries = true)
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