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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Cinema Sessions", description = "Administrative APIs for managing cinema sessions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSessionController {

	private final SessionService sessionService;

	@PostMapping
	@Operation(summary = "Create a new cinema session", description = "Creates a new cinema session with specified movie, hall, time, and pricing")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Session created successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Movie or hall not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "409", description = "Time conflict with existing session", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<SessionAdminResponse> createSession(@RequestBody SessionCreateRequest request) {

		log.info("POST /api/admin/sessions - Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID", description = "Retrieves detailed information about a specific cinema session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session found successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<SessionAdminResponse> getSessionById(
			@Parameter(description = "ID of the session to retrieve", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/admin/sessions/{} - Retrieving session for admin", id);
		SessionAdminResponse session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update cinema session", description = "Updates an existing cinema session with new information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session updated successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "409", description = "Time conflict with existing session", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<SessionAdminResponse> updateSession(
			@Parameter(description = "ID of the session to update", required = true, example = "1") @PathVariable Long id,

			@RequestBody SessionUpdateRequest request) {

		log.info("PUT /api/admin/sessions/{} - Updating session", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@PatchMapping("/{id}/cancel")
	@Operation(summary = "Cancel a session", description = "Cancels a scheduled cinema session. Cancelled sessions cannot have new bookings.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session cancelled successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Cannot cancel session (e.g., already started)", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> cancelSession(
			@Parameter(description = "ID of the session to cancel", required = true, example = "1") @PathVariable Long id) {

		log.info("PATCH /api/admin/sessions/{}/cancel - Cancelling session", id);
		sessionService.cancelSession(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/reactivate")
	@Operation(summary = "Reactivate a session", description = "Reactivates a previously cancelled cinema session")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session reactivated successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Cannot reactivate session (e.g., session time has passed)", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> reactivateSession(
			@Parameter(description = "ID of the session to reactivate", required = true, example = "1") @PathVariable Long id) {

		log.info("PATCH /api/admin/sessions/{}/reactivate - Reactivating session", id);
		sessionService.reactivateSession(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a session", description = "Permanently deletes a cinema session. This action is irreversible.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Cannot delete session (e.g., has active bookings)", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> deleteSession(
			@Parameter(description = "ID of the session to delete", required = true, example = "1") @PathVariable Long id) {

		log.info("DELETE /api/admin/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get all sessions with filtering", description = "Retrieves a paginated list of cinema sessions with various filtering options")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sessions retrieved successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<Page<SessionAdminResponse>> getSessions(
			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 20, sort = "startTime") Pageable pageable,

			@Parameter(description = "Search term for movie title or description", example = "Avengers") @RequestParam(required = false) String search,

			@Parameter(description = "Filter by session date (yyyy-MM-dd)", example = "2024-01-15") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "Filter by cinema hall ID", example = "1") @RequestParam(required = false) Long hallId,

			@Parameter(description = "Filter by movie ID", example = "5") @RequestParam(required = false) Long movieId,

			@Parameter(description = "Filter by session status", example = "SCHEDULED") @RequestParam(required = false) CinemaSessionStatus status) {

		log.info("GET /api/admin/sessions - search: '{}', date: {}, hallId: {}, movieId: {}, status: {}", search, date,
				hallId, movieId, status);

		Page<SessionAdminResponse> sessions = sessionService.getSessionsForAdmin(search, date, hallId, movieId, status,
				pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/check-conflict")
	@Operation(summary = "Check time conflict for a session", description = "Checks if a proposed session time conflicts with existing sessions in the same hall")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Conflict check completed", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Hall not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<Boolean> checkTimeConflict(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @RequestParam Long hallId,

			@Parameter(description = "Proposed start time of the session (yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2024-01-15T18:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

			@Parameter(description = "Duration of the session in minutes", required = true, example = "120") @RequestParam Integer durationMinutes,

			@Parameter(description = "Session ID to exclude from conflict check (for updates)", example = "5") @RequestParam(required = false) Long excludeSessionId) {

		log.info("GET /api/admin/sessions/check-conflict - Checking time conflict for hall {}", hallId);
		boolean hasConflict = sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
		return ResponseEntity.ok(hasConflict);
	}

	@GetMapping("/upcoming/today")
	@Operation(summary = "Get today's upcoming sessions", description = "Retrieves sessions scheduled for today with status information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sessions retrieved successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin or content manager access required", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<Page<SessionAdminResponse>> getTodaySessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 50, sort = "startTime") Pageable pageable) {

		log.info("GET /api/admin/sessions/upcoming/today - Retrieving today's sessions");
		Page<SessionAdminResponse> sessions = sessionService.getTodaySessions(pageable);
		return ResponseEntity.ok(sessions);
	}
}