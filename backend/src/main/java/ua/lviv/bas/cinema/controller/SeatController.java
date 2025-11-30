package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.service.SeatService;

@Slf4j
@RestController
@RequestMapping("/api/cinema-halls/{hallId}/seats")
@RequiredArgsConstructor
public class SeatController {

	private final SeatService seatService;

	@GetMapping
	public ResponseEntity<List<SeatResponse>> getSeatsByHall(@PathVariable Long hallId) {
		log.info("GET /api/cinema-halls/{}/seats - Retrieving all seats for hall", hallId);
		List<SeatResponse> seats = seatService.getSeatsByHall(hallId);
		return ResponseEntity.ok(seats);
	}

	@GetMapping("/{seatId}")
	public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long hallId, @PathVariable Long seatId) {
		log.info("GET /api/cinema-halls/{}/seats/{} - Retrieving seat", hallId, seatId);
		SeatResponse seat = seatService.getSeatById(seatId);
		return ResponseEntity.ok(seat);
	}

	@GetMapping("/position")
	public ResponseEntity<SeatResponse> getSeatByPosition(@PathVariable Long hallId, @RequestParam int row,
			@RequestParam int number) {
		log.info("GET /api/cinema-halls/{}/seats/position?row={}&number={} - Retrieving seat by position", hallId, row,
				number);
		SeatResponse seat = seatService.getSeatByPosition(hallId, row, number);
		return ResponseEntity.ok(seat);
	}

	@PutMapping("/{seatId}/type")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<SeatResponse> updateSeatType(@PathVariable Long hallId, @PathVariable Long seatId,
			@RequestParam SeatType seatType) {
		log.info("PUT /api/cinema-halls/{}/seats/{}/type?seatType={} - Updating seat type", hallId, seatId, seatType);
		SeatResponse updated = seatService.updateSeatType(seatId, seatType);
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/check-availability")
	public ResponseEntity<Boolean> checkSeatAvailability(@PathVariable Long hallId, @RequestParam int row,
			@RequestParam int number) {
		log.info("GET /api/cinema-halls/{}/seats/check-availability?row={}&number={} - Checking seat availability",
				hallId, row, number);
		boolean available = seatService.isSeatAvailable(hallId, row, number);
		return ResponseEntity.ok(available);
	}

	@GetMapping("/count")
	public ResponseEntity<Long> countSeatsByHall(@PathVariable Long hallId) {
		log.info("GET /api/cinema-halls/{}/seats/count - Counting seats for hall", hallId);
		long count = seatService.countSeatsByHall(hallId);
		return ResponseEntity.ok(count);
	}

	@GetMapping("/by-type")
	public ResponseEntity<List<SeatResponse>> getSeatsByType(@PathVariable Long hallId,
			@RequestParam SeatType seatType) {
		log.info("GET /api/cinema-halls/{}/seats/by-type?seatType={} - Retrieving seats by type", hallId, seatType);
		List<SeatResponse> seats = seatService.getSeatsByType(hallId, seatType);
		return ResponseEntity.ok(seats);
	}
}