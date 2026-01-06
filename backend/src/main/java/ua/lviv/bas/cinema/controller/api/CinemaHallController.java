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
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.service.cinema.CinemaHallService;

@Slf4j
@RestController
@RequestMapping("/api/cinema-halls")
@RequiredArgsConstructor
@Tag(name = "Cinema Hall API", description = "Public endpoints for viewing cinema halls")
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@GetMapping("/{id}")
	@Operation(summary = "Get cinema hall by ID", description = "Retrieve cinema hall information by its unique identifier.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cinema hall found"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<CinemaHallResponse> getHallById(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/cinema-halls/{} - Retrieving cinema hall", id);
		CinemaHallResponse hall = cinemaHallService.getHallById(id);
		return ResponseEntity.ok(hall);
	}

	@GetMapping
	@Operation(summary = "Get all cinema halls", description = "Retrieve list of all available cinema halls.")
	@ApiResponse(responseCode = "200", description = "List of cinema halls retrieved successfully")
	public ResponseEntity<List<CinemaHallResponse>> getAllHalls() {
		log.info("GET /api/cinema-halls - Retrieving all cinema halls");
		List<CinemaHallResponse> halls = cinemaHallService.getAllHalls();
		return ResponseEntity.ok(halls);
	}

	@GetMapping("/{id}/with-seats")
	@Operation(summary = "Get cinema hall with detailed seat information", description = "Retrieve cinema hall information including all seats with their details.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Hall with seats retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<CinemaHallWithSeatsResponse> getHallWithSeats(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/with-seats - Retrieving hall with seats", id);
		CinemaHallWithSeatsResponse hallWithSeats = cinemaHallService.getHallWithSeats(id);
		return ResponseEntity.ok(hallWithSeats);
	}

	@GetMapping("/{id}/layout")
	@Operation(summary = "Get cinema hall layout", description = "Retrieve detailed layout of cinema hall with seat organization by rows.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Hall layout retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<HallLayoutResponse> getHallLayout(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/layout - Retrieving hall layout", id);
		HallLayoutResponse layout = cinemaHallService.getHallLayout(id);
		return ResponseEntity.ok(layout);
	}

	@GetMapping("/search")
	@Operation(summary = "Search cinema halls by name", description = "Search cinema halls by partial name match.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid search parameters") })
	public ResponseEntity<List<CinemaHallResponse>> searchHalls(
			@Parameter(description = "Partial name to search for (case-insensitive)", example = "Hall A") @RequestParam(required = false) String name) {
		log.info("GET /api/cinema-halls/search?name={} - Searching cinema halls", name);
		List<CinemaHallResponse> halls = cinemaHallService.searchHalls(name);
		return ResponseEntity.ok(halls);
	}
}