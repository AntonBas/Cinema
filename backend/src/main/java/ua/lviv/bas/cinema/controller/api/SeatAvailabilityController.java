package ua.lviv.bas.cinema.controller.api;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatReservationResponse;
import ua.lviv.bas.cinema.service.booking.availability.SeatReservationService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Seat Availability", description = "APIs for seat availability and price information")
public class SeatAvailabilityController {
	private final SeatReservationService seatAvailabilityService;

	@GetMapping("/{sessionId}/seats/availability")
	@Operation(summary = "Get seat availability for a session", description = "Returns detailed information about seat availability, prices, and booking status for a specific cinema session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seat availability information retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<SeatReservationResponse> getSeatAvailability(
			@Parameter(description = "ID of the cinema session", required = true) @PathVariable Long sessionId) {

		log.info("Fetching seat availability for session ID: {}", sessionId);
		SeatReservationResponse response = seatAvailabilityService.getSeatAvailability(sessionId);
		return ResponseEntity.ok(response);
	}
}