package ua.lviv.bas.cinema.controller.api;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.common.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Public Session API", description = "Public endpoints for viewing movie sessions")
public class SessionController {

	private final SessionService sessionService;

	@GetMapping("/schedule")
	@Operation(summary = "Get schedule sessions", description = "Retrieve sessions in schedule format")
	@ApiResponse(responseCode = "200", description = "Schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessions(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule - Retrieving schedule sessions");
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/date/{date}")
	@Operation(summary = "Get schedule sessions by date", description = "Retrieve schedule sessions for a specific date")
	@ApiResponse(responseCode = "200", description = "Schedule sessions retrieved successfully")
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessionsByDate(
			@Parameter(description = "Date to filter schedule sessions", required = true, example = "2024-01-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/schedule/date/{} - Retrieving schedule sessions by date", date);
		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessionsByDate(date, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/schedule/movie/{movieId}")
	@Operation(summary = "Get schedule sessions by movie", description = "Retrieve schedule sessions for a specific movie")
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

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming schedule sessions", description = "Retrieve upcoming sessions within specified days")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Upcoming sessions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid days parameter") })
	public ResponseEntity<Page<SessionScheduleResponse>> getUpcomingScheduleSessions(
			@Parameter(description = "Number of days to look ahead", example = "7") @RequestParam(defaultValue = "7") int days,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
		log.info("GET /api/sessions/upcoming - Retrieving upcoming schedule sessions for {} days", days);
		Page<SessionScheduleResponse> sessions = sessionService.getUpcomingScheduleSessions(days, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID (public)", description = "Retrieve session information for public view")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Session found"),
			@ApiResponse(responseCode = "404", description = "Session not found or not available") })
	public ResponseEntity<SessionScheduleResponse> getSessionById(
			@Parameter(description = "ID of the session", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session for public", id);
		SessionScheduleResponse session = sessionService.getSessionByIdForPublic(id);
		return ResponseEntity.ok(session);
	}
}