package ua.lviv.bas.cinema.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;
import ua.lviv.bas.cinema.service.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

	private final SessionService sessionService;

	@PostMapping
	public ResponseEntity<SessionDto> createSession(@Valid @RequestBody SessionRequest request) {
		log.info("POST /api/sessions - Creating new session");
		SessionDto created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SessionDto> getSessionById(@PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session", id);
		SessionDto session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SessionDto> updateSession(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
		log.info("PUT /api/sessions/{} - Updating session", id);
		SessionDto updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
		log.info("DELETE /api/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<Page<SessionDto>> getAllSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable,
			@RequestParam(required = false) String search) {
		log.info("GET /api/sessions - Retrieving all sessions with pagination");
		Page<SessionDto> sessions = sessionService.getAllSessions(pageable, search);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/date/{date}")
	public ResponseEntity<Page<SessionDto>> getSessionsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/date/{} - Retrieving sessions by date", date);
		Page<SessionDto> sessions = sessionService.getSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/hall/{hallId}")
	public ResponseEntity<Page<SessionDto>> getSessionsByHall(@PathVariable Long hallId,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/hall/{} - Retrieving sessions by hall", hallId);
		Page<SessionDto> sessions = sessionService.getSessionsByHall(hallId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/movie/{movieId}")
	public ResponseEntity<Page<SessionDto>> getSessionsByMovie(@PathVariable Long movieId,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/movie/{} - Retrieving sessions by movie", movieId);
		Page<SessionDto> sessions = sessionService.getSessionsByMovie(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/available")
	public ResponseEntity<Page<SessionDto>> getAvailableSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/available - Retrieving available sessions");
		Page<SessionDto> sessions = sessionService.getAvailableSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/upcoming")
	public ResponseEntity<Page<SessionDto>> getUpcomingSessions(@RequestParam(defaultValue = "7") int days,
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/upcoming - Retrieving upcoming sessions for {} days", days);
		Page<SessionDto> sessions = sessionService.getUpcomingSessions(days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/today")
	public ResponseEntity<Page<SessionDto>> getTodaySessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/today - Retrieving today's sessions");
		Page<SessionDto> sessions = sessionService.getTodaySessions(pageable);
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