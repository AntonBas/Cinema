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
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Session Management", description = "Endpoints for managing movie sessions")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

	private final SessionService sessionService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Create new session", description = "Create a new movie session. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Session created successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Movie or hall not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<SessionAdminResponse> createSession(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Session creation request", required = true, content = @Content(schema = @Schema(implementation = SessionRequest.class))) @Valid @RequestBody SessionRequest request) {
		log.info("POST /api/sessions - Creating new session");
		SessionAdminResponse created = sessionService.createSession(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID", description = "Retrieve detailed session information by its ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session found", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionAdminResponse> getSessionById(
			@Parameter(description = "ID of the session", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session", id);
		SessionAdminResponse session = sessionService.getSessionById(id);
		return ResponseEntity.ok(session);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Update session", description = "Update existing session information. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Session updated successfully", content = @Content(schema = @Schema(implementation = SessionAdminResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
			@ApiResponse(responseCode = "404", description = "Session not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<SessionAdminResponse> updateSession(
			@Parameter(description = "ID of the session to update", required = true, example = "1") @PathVariable Long id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated session data", required = true, content = @Content(schema = @Schema(implementation = SessionRequest.class))) @Valid @RequestBody SessionRequest request) {
		log.info("PUT /api/sessions/{} - Updating session", id);
		SessionAdminResponse updated = sessionService.updateSession(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Delete session", description = "Delete a session by its ID. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete session with tickets sold"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteSession(
			@Parameter(description = "ID of the session to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/sessions/{} - Deleting session", id);
		sessionService.deleteSession(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get all sessions (paginated)", description = "Retrieve paginated list of all sessions with optional search.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<Page<SessionAdminResponse>> getAllSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable,

			@Parameter(description = "Search term (searches in movie title, hall name)", example = "Inception") @RequestParam(required = false) String search) {
		log.info("GET /api/sessions - Retrieving all sessions with pagination");
		Page<SessionAdminResponse> sessions = sessionService.getAllSessions(pageable, search);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/date/{date}")
	@Operation(summary = "Get sessions by date", description = "Retrieve sessions scheduled for a specific date.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByDate(
			@Parameter(description = "Date to filter sessions", required = true, example = "2024-01-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/date/{} - Retrieving sessions by date", date);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/hall/{hallId}")
	@Operation(summary = "Get sessions by cinema hall", description = "Retrieve sessions scheduled in a specific cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByHall(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/hall/{} - Retrieving sessions by hall", hallId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByHall(hallId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/movie/{movieId}")
	@Operation(summary = "Get sessions by movie", description = "Retrieve sessions for a specific movie.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<Page<SessionAdminResponse>> getSessionsByMovie(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long movieId,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/movie/{} - Retrieving sessions by movie", movieId);
		Page<SessionAdminResponse> sessions = sessionService.getSessionsByMovie(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/available")
	@Operation(summary = "Get available sessions", description = "Retrieve sessions that are currently available for booking.")
	@ApiResponse(responseCode = "200", description = "Available sessions retrieved successfully")
	public ResponseEntity<Page<SessionAdminResponse>> getAvailableSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/available - Retrieving available sessions");
		Page<SessionAdminResponse> sessions = sessionService.getAvailableSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming sessions", description = "Retrieve upcoming sessions within the specified number of days.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Upcoming sessions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid days parameter") })
	public ResponseEntity<Page<SessionAdminResponse>> getUpcomingSessions(
			@Parameter(description = "Number of days to look ahead", example = "7") @RequestParam(defaultValue = "7") int days,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/upcoming - Retrieving upcoming sessions for {} days", days);
		Page<SessionAdminResponse> sessions = sessionService.getUpcomingSessions(days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/today")
	@Operation(summary = "Get today's sessions", description = "Retrieve sessions scheduled for today.")
	@ApiResponse(responseCode = "200", description = "Today's sessions retrieved successfully")
	public ResponseEntity<Page<SessionAdminResponse>> getTodaySessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/today - Retrieving today's sessions");
		Page<SessionAdminResponse> sessions = sessionService.getTodaySessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/filter")
	@Operation(summary = "Filter sessions", description = "Filter sessions by date, hall, movie, or days ahead with pagination.")
	@ApiResponse(responseCode = "200", description = "Filtered sessions retrieved successfully")
	public ResponseEntity<Page<SessionAdminResponse>> getFilteredSessions(
			@Parameter(description = "Filter by specific date", example = "2024-01-15") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "Filter by cinema hall ID", example = "1") @RequestParam(required = false) Long hallId,

			@Parameter(description = "Filter by movie ID", example = "1") @RequestParam(required = false) Long movieId,

			@Parameter(description = "Filter by days ahead from today", example = "7") @RequestParam(required = false) Integer days,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/filter - Retrieving filtered sessions: date={}, hallId={}, movieId={}, days={}",
				date, hallId, movieId, days);
		Page<SessionAdminResponse> sessions = sessionService.getFilteredSessions(date, hallId, movieId, days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule")
	@Operation(summary = "Get schedule sessions", description = "Retrieve sessions in schedule format (public view).")
	@ApiResponse(responseCode = "200", description = "Schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule - Retrieving schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/date/{date}")
	@Operation(summary = "Get schedule sessions by date", description = "Retrieve schedule sessions for a specific date (public view).")
	@ApiResponse(responseCode = "200", description = "Schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessionsByDate(
			@Parameter(description = "Date to filter schedule sessions", required = true, example = "2024-01-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/date/{} - Retrieving schedule sessions by date", date);
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/movie/{movieId}")
	@Operation(summary = "Get schedule sessions by movie", description = "Retrieve schedule sessions for a specific movie (public view).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Schedule sessions retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessionsByMovie(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long movieId,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/movie/{} - Retrieving schedule sessions by movie", movieId);
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByMovie(movieId, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/available")
	@Operation(summary = "Get available schedule sessions", description = "Retrieve available sessions in schedule format (public view).")
	@ApiResponse(responseCode = "200", description = "Available schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getAvailableScheduleSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/available - Retrieving available schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/upcoming")
	@Operation(summary = "Get upcoming schedule sessions", description = "Retrieve upcoming schedule sessions within specified days (public view).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Upcoming schedule sessions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid days parameter") })
	public ResponseEntity<Page<SessionScheduleResponse>> getUpcomingScheduleSessions(
			@Parameter(description = "Number of days to look ahead", example = "7") @RequestParam(defaultValue = "7") int days,

			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/upcoming - Retrieving upcoming schedule sessions for {} days", days);
		Page<SessionScheduleResponse> sessions = sessionService
				.getScheduleSessionsByDate(LocalDate.now().plusDays(days), pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/today")
	@Operation(summary = "Get today's schedule sessions", description = "Retrieve today's sessions in schedule format (public view).")
	@ApiResponse(responseCode = "200", description = "Today's schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getTodayScheduleSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/today - Retrieving today's schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByDate(LocalDate.now(), pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/check-conflict")
	@Operation(summary = "Check time conflict", description = "Check if a new session would conflict with existing sessions in the same hall.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Conflict check completed", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Invalid parameters"),
			@ApiResponse(responseCode = "404", description = "Hall not found") })
	public ResponseEntity<Boolean> checkTimeConflict(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @RequestParam Long hallId,

			@Parameter(description = "Proposed start time", required = true, example = "2024-01-15T18:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

			@Parameter(description = "Duration in minutes", required = true, example = "120") @RequestParam Integer durationMinutes,

			@Parameter(description = "Session ID to exclude (for updates)", example = "1") @RequestParam(required = false) Long excludeSessionId) {
		log.info("GET /api/sessions/check-conflict - Checking time conflict for hall {}", hallId);
		boolean hasConflict = sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
		return ResponseEntity.ok(hasConflict);
	}
}