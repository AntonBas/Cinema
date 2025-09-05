package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;
import ua.lviv.bas.cinema.service.SeatService;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

	private final SeatService seatService;

	@GetMapping("/hall/{hallId}")
	public ResponseEntity<List<SeatDto>> getSeatsByHall(@PathVariable Long hallId) {
		try {
			List<SeatDto> seats = seatService.getSeatsByHallId(hallId);
			return ResponseEntity.ok(seats);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<SeatDto> getSeatById(@PathVariable Long id) {
		try {
			SeatDto seat = seatService.getSeatById(id);
			return ResponseEntity.ok(seat);
		} catch (jakarta.persistence.EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping
	public ResponseEntity<SeatDto> createSeat(@Valid @RequestBody SeatCreateDto seatCreateDto) {
		try {
			SeatDto createdSeat = seatService.createSeat(seatCreateDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdSeat);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<SeatDto> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatCreateDto seatUpdateDto) {
		try {
			SeatDto updatedSeat = seatService.updateSeat(id, seatUpdateDto);
			return ResponseEntity.ok(updatedSeat);
		} catch (jakarta.persistence.EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
		try {
			seatService.deleteSeat(id);
			return ResponseEntity.noContent().build();
		} catch (jakarta.persistence.EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/availability")
	public ResponseEntity<List<SeatDto>> getAvailableSeats(@RequestParam Long hallId, @RequestParam Long sessionId) {
		try {
			List<SeatDto> availableSeats = seatService.getAvailableSeatsForSession(hallId, sessionId);
			return ResponseEntity.ok(availableSeats);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/hall/{hallId}/session/{sessionId}")
	public ResponseEntity<List<SeatDto>> getAllSeatsWithAvailability(@PathVariable Long hallId,
			@PathVariable Long sessionId) {
		try {
			List<SeatDto> seats = seatService.getAllSeatsForSession(hallId, sessionId);
			return ResponseEntity.ok(seats);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}/availability")
	public ResponseEntity<Boolean> checkSeatAvailability(@PathVariable Long id, @RequestParam Long sessionId) {
		try {
			boolean isAvailable = seatService.isSeatAvailable(id, sessionId);
			return ResponseEntity.ok(isAvailable);
		} catch (jakarta.persistence.EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/count/available")
	public ResponseEntity<Integer> getAvailableSeatsCount(@RequestParam Long hallId, @RequestParam Long sessionId) {
		try {
			int availableCount = seatService.getAvailableSeatsCountForSession(hallId, sessionId);
			return ResponseEntity.ok(availableCount);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}