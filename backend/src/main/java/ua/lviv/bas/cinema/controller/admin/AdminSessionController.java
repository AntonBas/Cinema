package ua.lviv.bas.cinema.controller.admin;

import java.time.LocalDate;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@Tag(name = "Admin Session Management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSessionController {

	private final SessionService sessionService;

	@PostMapping
	@Operation(summary = "Create new session")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Session created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Movie or hall not found") })
	public ResponseEntity<SessionAdminResponse> createSession(@RequestBody @Valid SessionCreateRequest request) {
		log.info("Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Session found"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> getSessionById(@PathVariable Long id) {
		log.info("Retrieving session {}", id);
		SessionAdminResponse session = sessionService.getSessionForAdmin(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update session")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Session updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> updateSession(@PathVariable Long id,
			@RequestBody @Valid SessionUpdateRequest request) {
		log.info("Updating session {}", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@PatchMapping("/{id}/cancel")
	@Operation(summary = "Cancel session")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session cancelled successfully"),
			@ApiResponse(responseCode = "400", description = "Cannot cancel session"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> cancelSession(@PathVariable Long id) {
		log.info("Cancelling session {}", id);
		sessionService.cancelSession(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/reactivate")
	@Operation(summary = "Reactivate session")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session reactivated successfully"),
			@ApiResponse(responseCode = "400", description = "Cannot reactivate session"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> reactivateSession(@PathVariable Long id) {
		log.info("Reactivating session {}", id);
		sessionService.reactivateSession(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete session")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
		log.info("Deleting session {}", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get sessions with filters")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<PageResponse<SessionAdminResponse>> getSessions(@RequestParam(required = false) Long hallId,
			@RequestParam(required = false) String movieTitle,
			@RequestParam(required = false) CinemaSessionStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@PageableDefault(size = 10) Pageable pageable) {

		log.info("Getting sessions with filters");
		PageResponse<SessionAdminResponse> page = sessionService.getSessionsForAdmin(hallId, movieTitle, status,
				dateFrom, dateTo, pageable);
		return ResponseEntity.ok(page);
	}
}