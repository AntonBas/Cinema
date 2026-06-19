package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.service.booking.SeatReservationService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Seat Reservation", description = "APIs for seat reservation and availability")
public class SeatReservationController {

    private final SeatReservationService seatReservationService;

    @GetMapping("/{sessionId}/seats")
    @Operation(summary = "Get seat availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat availability retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public SeatReservationResponse getAvailability(@PathVariable Long sessionId) {
        log.info("GET /api/sessions/{}/seats", sessionId);
        return seatReservationService.getAvailability(sessionId);
    }

    @PostMapping("/{sessionId}/seats/{seatId}/hold")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Hold a seat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat held successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Session or seat not found"),
            @ApiResponse(responseCode = "409", description = "Seat is already reserved")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public void hold(@PathVariable Long sessionId, @PathVariable Long seatId,
                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("POST /api/sessions/{}/seats/{}/hold - user: {}", sessionId, seatId, userDetails.getUserId());
        seatReservationService.hold(sessionId, seatId, userDetails.getUser());
    }

    @DeleteMapping("/{sessionId}/seats/{seatId}/hold")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel hold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hold cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public void cancel(@PathVariable Long sessionId, @PathVariable Long seatId,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/sessions/{}/seats/{}/hold - user: {}", sessionId, seatId, userDetails.getUserId());
        seatReservationService.cancel(sessionId, seatId, userDetails.getUser());
    }
}