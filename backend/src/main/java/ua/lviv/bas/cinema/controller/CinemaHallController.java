package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cinema Hall Management", description = "Endpoints for managing cinema halls")
@SecurityRequirement(name = "bearerAuth")
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Create a new cinema hall", description = "Create a new cinema hall with specified configuration. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Cinema hall created successfully", content = @Content(schema = @Schema(implementation = CinemaHallResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<CinemaHallResponse> createHall(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cinema hall creation request", required = true, content = @Content(schema = @Schema(implementation = CinemaHallRequest.class))) @Valid @RequestBody CinemaHallRequest request) {
		log.info("POST /api/cinema-halls - Creating new cinema hall: {}", request.getName());
		CinemaHallResponse created = cinemaHallService.createHall(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get cinema hall by ID", description = "Retrieve cinema hall information by its unique identifier.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Cinema hall found", content = @Content(schema = @Schema(implementation = CinemaHallResponse.class))),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<CinemaHallResponse> getHallById(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/cinema-halls/{} - Retrieving cinema hall", id);
		CinemaHallResponse hall = cinemaHallService.getHallById(id);
		return ResponseEntity.ok(hall);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Update cinema hall", description = "Update existing cinema hall information. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Cinema hall updated successfully", content = @Content(schema = @Schema(implementation = CinemaHallResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<CinemaHallResponse> updateHall(
			@Parameter(description = "ID of the cinema hall to update", required = true, example = "1") @PathVariable Long id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated cinema hall data", required = true, content = @Content(schema = @Schema(implementation = CinemaHallRequest.class))) @Valid @RequestBody CinemaHallRequest request) {
		log.info("PUT /api/cinema-halls/{} - Updating cinema hall", id);
		CinemaHallResponse updated = cinemaHallService.updateHall(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Delete cinema hall", description = "Delete a cinema hall by its ID. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Cinema hall deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete hall with active sessions"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteHall(
			@Parameter(description = "ID of the cinema hall to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/cinema-halls/{} - Deleting cinema hall", id);
		cinemaHallService.deleteHall(id);
		return ResponseEntity.noContent().build();
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Hall with seats retrieved successfully", content = @Content(schema = @Schema(implementation = CinemaHallWithSeatsResponse.class))),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found") })
	public ResponseEntity<CinemaHallWithSeatsResponse> getHallWithSeats(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/cinema-halls/{}/with-seats - Retrieving hall with seats", id);
		CinemaHallWithSeatsResponse hallWithSeats = cinemaHallService.getHallWithSeats(id);
		return ResponseEntity.ok(hallWithSeats);
	}

	@GetMapping("/{id}/layout")
	@Operation(summary = "Get cinema hall layout", description = "Retrieve detailed layout of cinema hall with seat organization by rows.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Hall layout retrieved successfully", content = @Content(schema = @Schema(implementation = HallLayoutResponse.class))),
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