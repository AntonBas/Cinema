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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.common.SessionService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

	private final SessionService sessionService;

	@GetMapping
	public ResponseEntity<Page<SessionScheduleResponse>> getScheduleSessions(
			@PageableDefault(size = 20, sort = "startTime") Pageable pageable,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long movieId, @RequestParam(required = false) Integer daysAhead) {

		log.info("GET /api/sessions - date: {}, movieId: {}, daysAhead: {}", date, movieId, daysAhead);

		Page<SessionScheduleResponse> sessions = sessionService.getScheduleSessions(date, movieId, daysAhead, pageable);
		return ResponseEntity.ok(sessions);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SessionScheduleResponse> getSessionById(@PathVariable Long id) {
		log.info("GET /api/sessions/{} - Retrieving session for public", id);
		SessionScheduleResponse session = sessionService.getSessionByIdForPublic(id);
		return ResponseEntity.ok(session);
	}
}