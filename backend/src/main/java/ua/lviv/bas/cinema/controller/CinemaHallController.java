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
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.CinemaHallDto;
import ua.lviv.bas.cinema.dto.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.CinemaHallWithSeatsDto;
import ua.lviv.bas.cinema.dto.HallLayoutDto;
import ua.lviv.bas.cinema.dto.SeatLayoutRequest;
import ua.lviv.bas.cinema.service.CinemaHallService;

@Slf4j
@RestController
@RequestMapping("/api/cinema-halls")
@RequiredArgsConstructor
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	public ResponseEntity<CinemaHallDto> createHall(@Valid @RequestBody CinemaHallRequest request) {
		log.info("POST /api/cinema-halls - Creating new cinema hall: {}", request.getName());
		CinemaHallDto created = cinemaHallService.createHall(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CinemaHallDto> getHallById(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{} - Retrieving cinema hall", id);
		CinemaHallDto hall = cinemaHallService.getHallById(id);
		return ResponseEntity.ok(hall);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CinemaHallDto> updateHall(@PathVariable Long id,
			@Valid @RequestBody CinemaHallRequest request) {
		log.info("PUT /api/cinema-halls/{} - Updating cinema hall", id);
		CinemaHallDto updated = cinemaHallService.updateHall(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
		log.info("DELETE /api/cinema-halls/{} - Deleting cinema hall", id);
		cinemaHallService.deleteHall(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<CinemaHallDto>> getAllHalls() {
		log.info("GET /api/cinema-halls - Retrieving all cinema halls");
		List<CinemaHallDto> halls = cinemaHallService.getAllHalls();
		return ResponseEntity.ok(halls);
	}

	@PostMapping("/{id}/seats")
	public ResponseEntity<CinemaHallWithSeatsDto> generateSeats(@PathVariable Long id,
			@Valid @RequestBody SeatLayoutRequest request) {
		log.info("POST /api/cinema-halls/{}/seats - Generating seats layout", id);
		CinemaHallWithSeatsDto hallWithSeats = cinemaHallService.generateSeats(id, request);
		return ResponseEntity.ok(hallWithSeats);
	}

	@GetMapping("/{id}/with-seats")
	public ResponseEntity<CinemaHallWithSeatsDto> getHallWithSeats(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/with-seats - Retrieving hall with seats", id);
		CinemaHallWithSeatsDto hallWithSeats = cinemaHallService.getHallWithSeats(id);
		return ResponseEntity.ok(hallWithSeats);
	}

	@GetMapping("/{id}/layout")
	public ResponseEntity<HallLayoutDto> getHallLayout(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/layout - Retrieving hall layout", id);
		HallLayoutDto layout = cinemaHallService.getHallLayout(id);
		return ResponseEntity.ok(layout);
	}

	@GetMapping("/search")
	public ResponseEntity<List<CinemaHallDto>> searchHalls(@RequestParam(required = false) String name) {
		log.info("GET /api/cinema-halls/search?name={} - Searching cinema halls", name);
		List<CinemaHallDto> halls = cinemaHallService.searchHalls(name);
		return ResponseEntity.ok(halls);
	}
}