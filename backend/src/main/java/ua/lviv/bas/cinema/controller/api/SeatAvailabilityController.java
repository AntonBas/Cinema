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
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;
import ua.lviv.bas.cinema.service.booking.SeatAvailabilityService;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Seat Availability", description = "APIs for seat availability and price information")
public class SeatAvailabilityController {

	private final SeatAvailabilityService seatAvailabilityService;

	@GetMapping("/{sessionId}/seats/availability")
	@Operation(summary = "Get seat availability for a session", description = "Returns detailed information about seat availability, prices, and booking status for a specific cinema session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seat availability information retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found"),
			@ApiResponse(responseCode = "400", description = "Invalid session ID format") })
	public ResponseEntity<SeatAvailabilityResponse> getSeatAvailability(
			@Parameter(description = "ID of the cinema session", required = true, example = "1") @PathVariable Long sessionId) {

		log.info("Fetching seat availability for session ID: {}", sessionId);
		SeatAvailabilityResponse response = seatAvailabilityService.getSeatAvailability(sessionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{sessionId}/seats/{seatId}/availability")
	@Operation(summary = "Check specific seat availability", description = "Validates if a specific seat is available for booking in a given session")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat availability checked successfully"),
			@ApiResponse(responseCode = "400", description = "Seat is not available"),
			@ApiResponse(responseCode = "404", description = "Session or seat not found") })
	public ResponseEntity<Void> checkSeatAvailability(
			@Parameter(description = "ID of the cinema session", required = true, example = "1") @PathVariable Long sessionId,

			@Parameter(description = "ID of the seat", required = true, example = "25") @PathVariable Long seatId) {

		log.info("Checking availability for session ID: {}, seat ID: {}", sessionId, seatId);
		seatAvailabilityService.validateSeatAvailability(sessionId, seatId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{sessionId}/available-seats/count")
	@Operation(summary = "Get available seats count", description = "Returns the total number of available seats for a specific cinema session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Available seats count retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found") })
	public ResponseEntity<Integer> getAvailableSeatsCount(
			@Parameter(description = "ID of the cinema session", required = true, example = "1") @PathVariable Long sessionId) {

		log.info("Fetching available seats count for session ID: {}", sessionId);
		int count = seatAvailabilityService.getAvailableSeatsCount(sessionId);
		return ResponseEntity.ok(count);
	}
}