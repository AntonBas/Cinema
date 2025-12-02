package ua.lviv.bas.cinema.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

	private final SessionService sessionService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<SessionAdminResponse> createSession(@Valid @RequestBody SessionRequest request) {
		log.info("POST /api/sessions - Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SessionAdminResponse> getSessionById(@PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session", id);
		SessionAdminResponse session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<SessionAdminResponse> updateSession(@PathVariable Long id,
			@Valid @RequestBody SessionRequest request) {
		log.info("PUT /api/sessions/{} - Updating session", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
		log.info("DELETE /api/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<Page<SessionAdminResponse>> getAllSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable,
			@RequestParam(required = false) String search) {
		log.info("GET /api/sessions - Retrieving all sessions with pagination");
		Page<SessionAdminResponse> sessions = sessionService.getAllSessions(pageable, search);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/date/{date}")
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/date/{} - Retrieving sessions by date", date);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/hall/{hallId}")
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByHall(@PathVariable Long hallId,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/hall/{} - Retrieving sessions by hall", hallId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByHall(hallId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/movie/{movieId}")
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByMovie(@PathVariable Long movieId,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/movie/{} - Retrieving sessions by movie", movieId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByMovie(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/available")
	public ResponseEntity<Page<SessionAdminResponse>> getAvailableSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/available - Retrieving available sessions");
		Page<SessionAdminResponse> sessions = sessionService.getAvailableSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/upcoming")
	public ResponseEntity<Page<SessionAdminResponse>> getUpcomingSessions(@RequestParam(defaultValue = "7") int days,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/upcoming - Retrieving upcoming sessions for {} days", days);
		Page<SessionAdminResponse> sessions = sessionService.getUpcomingSessions(days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/today")
	public ResponseEntity<Page<SessionAdminResponse>> getTodaySessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/today - Retrieving today's sessions");
		Page<SessionAdminResponse> sessions = sessionService.getTodaySessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/filter")
	public ResponseEntity<Page<SessionAdminResponse>> getFilteredSessions(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long hallId, @RequestParam(required = false) Long movieId,
			@RequestParam(required = false) Integer days,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/filter - Retrieving filtered sessions: date={}, hallId={}, movieId={}, days={}",
				date, hallId, movieId, days);
		Page<SessionAdminResponse> sessions = sessionService.getFilteredSessions(date, hallId, movieId, days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule - Retrieving schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/date/{date}")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessionsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/date/{} - Retrieving schedule sessions by date", date);
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/movie/{movieId}")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessionsByMovie(@PathVariable Long movieId,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/movie/{} - Retrieving schedule sessions by movie", movieId);
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByMovie(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/available")
	public ResponseEntity<Page<SessionScheduleResponse>> getAvailableScheduleSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/available - Retrieving available schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/upcoming")
	public ResponseEntity<Page<SessionScheduleResponse>> getUpcomingScheduleSessions(
			@RequestParam(defaultValue = "7") int days,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/upcoming - Retrieving upcoming schedule sessions for {} days", days);
		Page<SessionScheduleResponse> sessions = sessionService
				.getScheduleSessionsByDate(LocalDate.now().plusDays(days), pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/today")
	public ResponseEntity<Page<SessionScheduleResponse>> getTodayScheduleSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/today - Retrieving today's schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByDate(LocalDate.now(), pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/check-conflict")
	public ResponseEntity<Boolean> checkTimeConflict(@RequestParam Long hallId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
			@RequestParam Integer durationMinutes, @RequestParam(required = false) Long excludeSessionId) {
		log.info("GET /api/sessions/check-conflict - Checking time conflict for hall {}", hallId);
		boolean hasConflict = sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
		return ResponseEntity.ok(hasConflict);
	}
}