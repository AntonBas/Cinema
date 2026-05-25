package ua.lviv.bas.cinema.service.cinema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
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
import ua.lviv.bas.cinema.service.booking.SeatReservationService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionSpecification sessionSpecification;
    private final MovieRepository movieRepository;
    private final CinemaHallService cinemaHallService;
    private final SeatReservationService seatReservationService;
    private final AuditService auditService;

    @CacheEvict(value = {"sessions", "seatAvailability"}, allEntries = true)
    @Transactional
    public SessionResponse createSession(SessionRequest request) {
        validateStartTime(request.startTime());

        var movie = movieRepository.getReferenceById(request.movieId());
        var hall = cinemaHallService.getHallEntity(request.hallId());

        validateMovieAvailability(movie, request.startTime());
        validateNoTimeConflict(hall.getId(), hall.getName(), request.startTime(),
                request.startTime().plusMinutes(movie.getDurationMinutes()), null);

        var session = sessionMapper.toEntity(request);
        session.setMovie(movie);
        session.setHall(hall);

        var saved = sessionRepository.save(session);
        log.info("Session created with ID: {}", saved.getId());
        auditCreate(saved);

        return sessionMapper.toSessionResponse(saved);
    }

    @Cacheable(value = "sessions", key = "'schedule:' + #searchTerm + ':' + #date + ':' + #movieId")
    public List<SessionScheduleResponse> getSchedule(String searchTerm, LocalDate date, Long movieId) {
        Specification<Session> spec = sessionSpecification.forSchedule(searchTerm, date, movieId);
        var sessions = sessionRepository.findAll(spec);

        if (sessions.isEmpty()) {
            return List.of();
        }

        var sessionIds = sessions.stream().map(Session::getId).toList();
        var availableSeats = seatReservationService.getAvailableSeatsBatch(sessionIds);

        var projections = sessionRepository.findScheduleProjectionsByIds(sessionIds)
                .stream()
                .collect(Collectors.toMap(SessionScheduleProjection::getId, p -> p));

        return sessions.stream().map(session -> {
            var proj = projections.get(session.getId());
            return sessionMapper.toSessionScheduleResponse(proj)
                    .withAvailableSeats(availableSeats.getOrDefault(session.getId(), 0));
        }).toList();
    }

    @Cacheable(value = "sessions", key = "'admin:' + #hallId + ':' + #movieTitle + ':' + #status + ':' + #dateFrom + ':' + #dateTo + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<SessionAdminResponse> getSessions(Long hallId, String movieTitle, CinemaSessionStatus status,
                                                  LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        Specification<Session> spec = sessionSpecification.forAdmin(hallId, movieTitle, status, dateFrom, dateTo);
        var page = sessionRepository.findAll(spec, pageable);

        var sessionIds = page.getContent().stream().map(Session::getId).toList();
        var projections = sessionRepository.findAdminProjectionsByIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionAdminProjection::getId, p -> p));

        var responses = page.getContent().stream()
                .map(session -> sessionMapper.toSessionAdminResponse(projections.get(session.getId()))).toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Cacheable(value = "sessions", key = "#id")
    public SessionResponse getSession(Long id) {
        return sessionRepository.findById(id).map(sessionMapper::toSessionResponse)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }

    @CacheEvict(value = {"sessions", "seatAvailability"}, allEntries = true)
    @Transactional
    public SessionResponse updateSession(Long id, SessionRequest request) {
        var session = sessionRepository.findByIdWithLock(id).orElseThrow(() -> new SessionNotFoundException(id));

        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("startTime", session.getStartTime());
        oldDetails.put("basePrice", session.getBasePrice());
        oldDetails.put("movieId", session.getMovie().getId());
        oldDetails.put("hallId", session.getHall().getId());

        if (request.startTime() != null && !request.startTime().equals(session.getStartTime())) {
            validateStartTime(request.startTime());
        }

        sessionMapper.updateEntity(request, session);

        if (request.movieId() != null) {
            validateMovieAvailability(session.getMovie(), session.getStartTime());
        }

        validateNoTimeConflict(session.getHall().getId(), session.getHall().getName(), session.getStartTime(),
                session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), id);

        session = sessionRepository.save(session);
        log.info("Session updated with ID: {}", session.getId());

        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("startTime", session.getStartTime());
        newDetails.put("basePrice", session.getBasePrice());
        newDetails.put("movieId", session.getMovie().getId());
        newDetails.put("hallId", session.getHall().getId());

        auditService.logChange("Session", id, "Session #" + id, AuditAction.UPDATED, oldDetails, newDetails);

        return sessionMapper.toSessionResponse(session);
    }

    @CacheEvict(value = {"sessions", "seatAvailability"}, allEntries = true)
    @Transactional
    public void deleteSession(Long id) {
        var session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

        sessionRepository.deleteById(id);
        log.info("Session deleted with ID: {}", id);
        auditDelete(session);
    }

    @CacheEvict(value = {"sessions", "seatAvailability"}, allEntries = true)
    @Transactional
    public void cancelSession(Long sessionId) {
        var session = sessionRepository.findByIdWithLock(sessionId)
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

        var oldStatus = session.getStatus();
        session.setStatus(CinemaSessionStatus.CANCELLED);
        sessionRepository.save(session);
        log.info("Session cancelled with ID: {}", sessionId);

        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", CinemaSessionStatus.CANCELLED);

        auditService.logChange("Session", sessionId, "Session #" + sessionId, AuditAction.CANCELLED, oldDetails,
                newDetails);
    }

    @CacheEvict(value = {"sessions", "seatAvailability"}, allEntries = true)
    @Transactional
    public void reactivateSession(Long sessionId) {
        var session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != CinemaSessionStatus.CANCELLED) {
            throw SessionOperationException.onlyCancelledCanBeReactivated();
        }

        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw SessionOperationException.cannotReactivatePast();
        }

        validateNoTimeConflict(session.getHall().getId(), session.getHall().getName(), session.getStartTime(),
                session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()), sessionId);

        var oldStatus = session.getStatus();
        session.setStatus(CinemaSessionStatus.SCHEDULED);
        sessionRepository.save(session);
        log.info("Session reactivated with ID: {}", sessionId);

        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", CinemaSessionStatus.SCHEDULED);

        auditService.logChange("Session", sessionId, "Session #" + sessionId, AuditAction.REACTIVATED, oldDetails,
                newDetails);
    }

    private void validateStartTime(LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw SessionValidationException.tooCloseToStart(startTime);
        }
    }

    private void validateMovieAvailability(Movie movie, LocalDateTime sessionStartTime) {
        var sessionDate = sessionStartTime.toLocalDate();

        if (sessionDate.isBefore(movie.getReleaseDate())) {
            throw SessionValidationException.movieNotReleased(movie, sessionDate);
        }

        if (movie.getEndShowingDate() != null && sessionDate.isAfter(movie.getEndShowingDate())) {
            throw SessionValidationException.movieEndedShowing(movie, sessionDate);
        }
    }

    private void validateNoTimeConflict(Long hallId, String hallName, LocalDateTime startTime, LocalDateTime endTime,
                                        Long excludeSessionId) {
        if (sessionRepository.existsConflictingSession(hallId, startTime, endTime, excludeSessionId)) {
            throw new SessionTimeConflictException(hallName, startTime);
        }
    }

    private void auditCreate(Session session) {
        var details = new HashMap<String, Object>();
        details.put("movieId", session.getMovie().getId());
        details.put("movieTitle", session.getMovie().getTitle());
        details.put("hallId", session.getHall().getId());
        details.put("hallName", session.getHall().getName());
        details.put("startTime", session.getStartTime());
        details.put("basePrice", session.getBasePrice());
        auditService.logChange("Session", session.getId(), "Session #" + session.getId(), AuditAction.CREATED, null,
                details);
    }

    private void auditDelete(Session session) {
        var details = new HashMap<String, Object>();
        details.put("movieTitle", session.getMovie().getTitle());
        details.put("hallName", session.getHall().getName());
        details.put("startTime", session.getStartTime());
        auditService.logChange("Session", session.getId(), "Session #" + session.getId(), AuditAction.DELETED, details,
                null);
    }
}