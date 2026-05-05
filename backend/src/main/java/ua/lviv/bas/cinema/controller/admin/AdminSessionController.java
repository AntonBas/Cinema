package ua.lviv.bas.cinema.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@Tag(name = "Admin Session Management", description = "Admin endpoints for managing cinema sessions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSessionController {

    private final SessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Movie or hall not found")
    })
    public SessionResponse createSession(@RequestBody @Valid SessionRequest request) {
        log.info("Creating new session");
        return sessionService.createSession(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public SessionResponse getSessionById(@PathVariable Long id) {
        log.info("Retrieving session {}", id);
        return sessionService.getSession(id);
    }

    @GetMapping
    @Operation(summary = "Get sessions with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<SessionAdminResponse> getSessions(@RequestParam(required = false) Long hallId,
                                                          @RequestParam(required = false) String movieTitle,
                                                          @RequestParam(required = false) CinemaSessionStatus status,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                          @PageableDefault Pageable pageable) {

        log.info("Getting sessions with filters");
        return PageResponse.from(sessionService.getSessions(hallId, movieTitle, status, dateFrom, dateTo, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or time conflict"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public SessionResponse updateSession(@PathVariable Long id, @RequestBody @Valid SessionRequest request) {
        log.info("Updating session {}", id);
        return sessionService.updateSession(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel session"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public void cancelSession(@PathVariable Long id) {
        log.info("Cancelling session {}", id);
        sessionService.cancelSession(id);
    }

    @PatchMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reactivate session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session reactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot reactivate session"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public void reactivateSession(@PathVariable Long id) {
        log.info("Reactivating session {}", id);
        sessionService.reactivateSession(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public void deleteSession(@PathVariable Long id) {
        log.info("Deleting session {}", id);
        sessionService.deleteSession(id);
    }
}