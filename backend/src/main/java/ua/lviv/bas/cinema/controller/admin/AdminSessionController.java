package ua.lviv.bas.cinema.controller.admin;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSessionController {

	private final SessionService sessionService;

	@PostMapping
	public ResponseEntity<SessionAdminResponse> createSession(@RequestBody SessionCreateRequest request) {
		log.info("POST /api/admin/sessions - Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SessionAdminResponse> getSessionById(@PathVariable Long id) {
		log.info("GET /api/admin/sessions/{} - Retrieving session for admin", id);
		SessionAdminResponse session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SessionAdminResponse> updateSession(@PathVariable Long id,
			@RequestBody SessionUpdateRequest request) {
		log.info("PUT /api/admin/sessions/{} - Updating session", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Void> cancelSession(@PathVariable Long id) {
		log.info("PATCH /api/admin/sessions/{}/cancel - Cancelling session", id);
		sessionService.cancelSession(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/reactivate")
	public ResponseEntity<Void> reactivateSession(@PathVariable Long id) {
		log.info("PATCH /api/admin/sessions/{}/reactivate - Reactivating session", id);
		sessionService.reactivateSession(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
		log.info("DELETE /api/admin/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<Page<SessionAdminResponse>> getSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long hallId, @RequestParam(required = false) Long movieId,
			@RequestParam(required = false) CinemaSessionStatus status) {

		log.info("GET /api/admin/sessions - search: '{}', date: {}, hallId: {}, movieId: {}, status: {}", search, date,
				hallId, movieId, status);

		Page<SessionAdminResponse> sessions = sessionService.getSessionsForAdmin(search, date, hallId, movieId, status,
				pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/check-conflict")
	public ResponseEntity<Boolean> checkTimeConflict(@RequestParam Long hallId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
			@RequestParam Integer durationMinutes, @RequestParam(required = false) Long excludeSessionId) {

		log.info("GET /api/admin/sessions/check-conflict - Checking time conflict for hall {}", hallId);
		boolean hasConflict = sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
		return ResponseEntity.ok(hasConflict);
	}
}