package ua.lviv.bas.cinema.controller.api;

import java.util.List;

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
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.service.cinema.SeatService;

@Slf4j
@RestController
@RequestMapping("/api/cinema-halls/{hallId}/seats")
@RequiredArgsConstructor
@Tag(name = "Seat API", description = "Public endpoints for viewing seats")
public class SeatController {

	private final SeatService seatService;

	@GetMapping
	@Operation(summary = "Get all seats in cinema hall", description = "Retrieve all seats for a specific cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seats retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<List<SeatResponse>> getSeatsByHall(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId) {
		log.info("GET /api/cinema-halls/{}/seats - Retrieving all seats for hall", hallId);
		List<SeatResponse> seats = seatService.getSeatsByHall(hallId);
		return ResponseEntity.ok(seats);
	}

	@GetMapping("/{seatId}")
	@Operation(summary = "Get seat by ID", description = "Retrieve specific seat information by its ID within a cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat found"),
			@ApiResponse(responseCode = "404", description = "Seat or cinema hall not found") })
	public ResponseEntity<SeatResponse> getSeatById(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,

			@Parameter(description = "ID of the seat", required = true, example = "5") @PathVariable Long seatId) {
		log.info("GET /api/cinema-halls/{}/seats/{} - Retrieving seat", hallId, seatId);
		SeatResponse seat = seatService.getSeatById(seatId);
		return ResponseEntity.ok(seat);
	}

	@GetMapping("/position")
	@Operation(summary = "Get seat by position", description = "Retrieve seat information by its row and number position within a cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat found"),
			@ApiResponse(responseCode = "404", description = "Seat not found at specified position") })
	public ResponseEntity<SeatResponse> getSeatByPosition(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,

			@Parameter(description = "Row number (starting from 1)", required = true, example = "5") @RequestParam int row,

			@Parameter(description = "Seat number within the row (starting from 1)", required = true, example = "12") @RequestParam int number) {
		log.info("GET /api/cinema-halls/{}/seats/position?row={}&number={} - Retrieving seat by position", hallId, row,
				number);
		SeatResponse seat = seatService.getSeatByPosition(hallId, row, number);
		return ResponseEntity.ok(seat);
	}

	@GetMapping("/check-availability")
	@Operation(summary = "Check seat availability", description = "Check if a specific seat is available (not blocked or reserved).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Availability status retrieved"),
			@ApiResponse(responseCode = "400", description = "Invalid seat position") })
	public ResponseEntity<Boolean> checkSeatAvailability(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,

			@Parameter(description = "Row number (starting from 1)", required = true, example = "5") @RequestParam int row,

			@Parameter(description = "Seat number within the row (starting from 1)", required = true, example = "12") @RequestParam int number) {
		log.info("GET /api/cinema-halls/{}/seats/check-availability?row={}&number={} - Checking seat availability",
				hallId, row, number);
		boolean available = seatService.isSeatAvailable(hallId, row, number);
		return ResponseEntity.ok(available);
	}

	@GetMapping("/count")
	@Operation(summary = "Count seats in hall", description = "Get total number of seats in a cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat count retrieved"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<Long> countSeatsByHall(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId) {
		log.info("GET /api/cinema-halls/{}/seats/count - Counting seats for hall", hallId);
		long count = seatService.countSeatsByHall(hallId);
		return ResponseEntity.ok(count);
	}

	@GetMapping("/by-type")
	@Operation(summary = "Get seats by type", description = "Retrieve all seats of a specific type within a cinema hall.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seats retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<List<SeatResponse>> getSeatsByType(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,

			@Parameter(description = "Type of seats to retrieve", required = true, example = "VIP") @RequestParam SeatType seatType) {
		log.info("GET /api/cinema-halls/{}/seats/by-type?seatType={} - Retrieving seats by type", hallId, seatType);
		List<SeatResponse> seats = seatService.getSeatsByType(hallId, seatType);
		return ResponseEntity.ok(seats);
	}
}