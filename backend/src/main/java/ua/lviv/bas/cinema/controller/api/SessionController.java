package ua.lviv.bas.cinema.controller.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Session API", description = "Public endpoints for viewing cinema sessions")
public class SessionController {

	private final SessionService sessionService;

	@GetMapping
	@Operation(summary = "Get schedule sessions", description = "Get paginated list of upcoming sessions.")
	@ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
	public ResponseEntity<PageResponse<SessionScheduleResponse>> getScheduleSessions(@Valid SessionFilterRequest filter,
			@Parameter(hidden = true) @PageableDefault(size = 12, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
		log.info("GET /api/sessions - Getting schedule sessions with filters: {}", filter);
		PageResponse<SessionScheduleResponse> page = sessionService.getScheduleSessions(filter, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID", description = "Retrieve session details for public view.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Session found"),
			@ApiResponse(responseCode = "404", description = "Session not found or not available") })
	public ResponseEntity<SessionScheduleResponse> getSessionById(
			@Parameter(description = "ID of the session", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session for public", id);
		SessionScheduleResponse session = sessionService.getSessionByIdForPublic(id);
		return ResponseEntity.ok(session);
	}
}