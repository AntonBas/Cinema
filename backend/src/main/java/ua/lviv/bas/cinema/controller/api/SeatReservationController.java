package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.reservation.SeatReservationService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Seat Reservation", description = "APIs for seat reservation and availability")
public class SeatReservationController {
	private final SeatReservationService seatReservationService;

	@GetMapping("/{sessionId}/seats/availability")
	@Operation(summary = "Get seat availability for a session", description = "Returns detailed information about seat availability, prices, and booking status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seat availability information retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SeatReservationResponse> getSeatAvailability(
			@Parameter(description = "ID of the cinema session", required = true) @PathVariable Long sessionId) {

		log.info("Fetching seat availability for session ID: {}", sessionId);
		SeatReservationResponse response = seatReservationService.getSeatAvailability(sessionId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{sessionId}/seats/{seatId}/hold")
	@Operation(summary = "Temporarily hold a seat", description = "Places a temporary hold on a seat for 5 minutes while user selects tickets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat held successfully"),
			@ApiResponse(responseCode = "404", description = "Session or seat not found"),
			@ApiResponse(responseCode = "409", description = "Seat is already reserved") })
	public ResponseEntity<Void> temporaryHoldSeat(
			@Parameter(description = "ID of the cinema session", required = true) @PathVariable Long sessionId,
			@Parameter(description = "ID of the seat", required = true) @PathVariable Long seatId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		log.info("User {} placing temporary hold on seat {} in session {}", userDetails.getUserId(), seatId, sessionId);

		seatReservationService.temporaryHoldSeat(sessionId, seatId, userDetails.getUser());
		return ResponseEntity.ok().build();
	}
}