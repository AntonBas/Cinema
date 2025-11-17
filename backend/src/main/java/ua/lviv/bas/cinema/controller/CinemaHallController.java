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
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.service.CinemaHallService;

@Slf4j
@RestController
@RequestMapping("/api/cinema-halls")
@RequiredArgsConstructor
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	public ResponseEntity<CinemaHallResponse> createHall(@Valid @RequestBody CinemaHallRequest request) {
		log.info("POST /api/cinema-halls - Creating new cinema hall: {}", request.getName());
		CinemaHallResponse created = cinemaHallService.createHall(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CinemaHallResponse> getHallById(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{} - Retrieving cinema hall", id);
		CinemaHallResponse hall = cinemaHallService.getHallById(id);
		return ResponseEntity.ok(hall);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CinemaHallResponse> updateHall(@PathVariable Long id,
			@Valid @RequestBody CinemaHallRequest request) {
		log.info("PUT /api/cinema-halls/{} - Updating cinema hall", id);
		CinemaHallResponse updated = cinemaHallService.updateHall(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
		log.info("DELETE /api/cinema-halls/{} - Deleting cinema hall", id);
		cinemaHallService.deleteHall(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<CinemaHallResponse>> getAllHalls() {
		log.info("GET /api/cinema-halls - Retrieving all cinema halls");
		List<CinemaHallResponse> halls = cinemaHallService.getAllHalls();
		return ResponseEntity.ok(halls);
	}

	@GetMapping("/{id}/with-seats")
	public ResponseEntity<CinemaHallWithSeatsResponse> getHallWithSeats(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/with-seats - Retrieving hall with seats", id);
		CinemaHallWithSeatsResponse hallWithSeats = cinemaHallService.getHallWithSeats(id);
		return ResponseEntity.ok(hallWithSeats);
	}

	@GetMapping("/{id}/layout")
	public ResponseEntity<HallLayoutResponse> getHallLayout(@PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/layout - Retrieving hall layout", id);
		HallLayoutResponse layout = cinemaHallService.getHallLayout(id);
		return ResponseEntity.ok(layout);
	}

	@GetMapping("/search")
	public ResponseEntity<List<CinemaHallResponse>> searchHalls(@RequestParam(required = false) String name) {
		log.info("GET /api/cinema-halls/search?name={} - Searching cinema halls", name);
		List<CinemaHallResponse> halls = cinemaHallService.searchHalls(name);
		return ResponseEntity.ok(halls);
	}
}