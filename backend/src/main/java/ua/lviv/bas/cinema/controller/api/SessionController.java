package ua.lviv.bas.cinema.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	@Operation(summary = "Get schedule sessions")
	@ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
	public ResponseEntity<List<SessionScheduleResponse>> getScheduleSessions(
			@RequestParam(required = false) String searchTerm,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		log.info("Getting schedule sessions for date: {}, search: {}", date, searchTerm);
		List<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(searchTerm, date);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get session by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Session found"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SessionScheduleResponse> getSessionById(@PathVariable Long id) {
		log.info("Retrieving session {}", id);
		SessionScheduleResponse session = sessionService.getSessionForPublic(id);
		return ResponseEntity.ok(session);
	}
}