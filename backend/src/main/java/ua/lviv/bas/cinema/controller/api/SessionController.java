package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Session API", description = "Public endpoints for viewing cinema sessions")
public class SessionController {

    private final SessionService sessionService;

    @RateLimit(value = 20, duration = 1)
    @GetMapping
    @Operation(summary = "Get schedule sessions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    })
    public ResponseEntity<List<SessionScheduleResponse>> getSchedule(@RequestParam(required = false) String searchTerm,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                     @RequestParam(required = false) Long movieId) {
        log.info("Getting schedule sessions for date: {}, search: {}, movieId: {}", date, searchTerm, movieId);
        var sessions = sessionService.getSchedule(searchTerm, date, movieId);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)).body(sessions);
    }
}