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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.service.common.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@Tag(name = "Admin Session Management", description = "Admin endpoints for managing movie sessions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSessionController {

	private final SessionService sessionService;

	@PostMapping
	@Operation(summary = "Create new session", description = "Create a new movie session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Session created successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Movie or hall not found") })
	public ResponseEntity<SessionAdminResponse> createSession(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Session creation request", required = true, content = @Content(schema = @Schema(implementation = SessionCreateRequest.class))) @Valid @RequestBody SessionCreateRequest request) {
		log.info("POST /api/admin/sessions - Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID (admin)", description = "Retrieve detailed session information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session found", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> getSessionById(
			@Parameter(description = "ID of the session", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/admin/sessions/{} - Retrieving session for admin", id);
		SessionAdminResponse session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update session", description = "Update existing session information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session updated successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> updateSession(
			@Parameter(description = "ID of the session to update", required = true, example = "1") @PathVariable Long id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated session data", required = true, content = @Content(schema = @Schema(implementation = SessionUpdateRequest.class))) @Valid @RequestBody SessionUpdateRequest request) {
		log.info("PUT /api/admin/sessions/{} - Updating session", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@PutMapping("/{id}/status")
	@Operation(summary = "Update session status", description = "Update only the status of a session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session status updated successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid status or business rule violation"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> updateSessionStatus(
			@Parameter(description = "ID of the session", required = true, example = "1") @PathVariable Long id,
			@Parameter(description = "New status for the session", required = true, example = "CANCELLED") @RequestParam CinemaSessionStatus status) {
		log.info("PUT /api/admin/sessions/{}/status - Updating session status to {}", id, status);
		SessionAdminResponse updated = sessionService.updateSessionStatus(id, status);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete session", description = "Delete a session by its ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete session with tickets sold") })
	public ResponseEntity<Void> deleteSession(
			@Parameter(description = "ID of the session to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/admin/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get all sessions", description = "Retrieve paginated list of all sessions")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<Page<SessionAdminResponse>> getAllSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable,
			@Parameter(description = "Search term (searches in movie title)", example = "Inception") @RequestParam(required = false) String search) {
		log.info("GET /api/admin/sessions - Retrieving all sessions with pagination");
		Page<SessionAdminResponse> sessions = sessionService.getAllSessionsForAdmin(pageable, search);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/filter")
	@Operation(summary = "Filter sessions", description = "Filter sessions with advanced options")
	@ApiResponse(responseCode = "200", description = "Filtered sessions retrieved successfully")
	public ResponseEntity<Page<SessionAdminResponse>> getFilteredSessions(@Valid SessionFilter filter,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/admin/sessions/filter - Retrieving filtered sessions: {}", filter);
		Page<SessionAdminResponse> sessions = sessionService.getFilteredSessions(filter);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/date/{date}")
	@Operation(summary = "Get sessions by date", description = "Retrieve sessions scheduled for a specific date")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByDate(
			@Parameter(description = "Date to filter sessions", required = true, example = "2024-01-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/admin/sessions/date/{} - Retrieving sessions by date", date);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByDateForAdmin(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/hall/{hallId}")
	@Operation(summary = "Get sessions by cinema hall", description = "Retrieve sessions scheduled in a specific hall")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByHall(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/admin/sessions/hall/{} - Retrieving sessions by hall", hallId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByHallForAdmin(hallId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/movie/{movieId}")
	@Operation(summary = "Get sessions by movie", description = "Retrieve sessions for a specific movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByMovie(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long movieId,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/admin/sessions/movie/{} - Retrieving sessions by movie", movieId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByMovieForAdmin(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/status/{status}")
	@Operation(summary = "Get sessions by status", description = "Retrieve sessions filtered by status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByStatus(
			@Parameter(description = "Status to filter sessions", required = true, example = "SCHEDULED") @PathVariable CinemaSessionStatus status,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/admin/sessions/status/{} - Retrieving sessions by status", status);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByStatus(status, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/check-conflict")
	@Operation(summary = "Check time conflict", description = "Check if a session would conflict with existing ones")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Conflict check completed", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Invalid parameters"),
			@ApiResponse(responseCode = "404", description = "Hall not found") })
	public ResponseEntity<Boolean> checkTimeConflict(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @RequestParam Long hallId,
			@Parameter(description = "Proposed start time", required = true, example = "2024-01-15T18:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
			@Parameter(description = "Duration in minutes", required = true, example = "120") @RequestParam Integer durationMinutes,
			@Parameter(description = "Session ID to exclude (for updates)", example = "1") @RequestParam(required = false) Long excludeSessionId) {
		log.info("GET /api/admin/sessions/check-conflict - Checking time conflict for hall {}", hallId);
		boolean hasConflict = sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
		return ResponseEntity.ok(hasConflict);
	}
}