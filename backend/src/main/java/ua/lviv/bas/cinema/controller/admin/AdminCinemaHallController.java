package ua.lviv.bas.cinema.controller.admin;

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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.service.cinema.CinemaHallService;

@Slf4j
@RestController
@RequestMapping("/api/admin/cinema-halls")
@RequiredArgsConstructor
@Tag(name = "Admin Cinema Hall Management", description = "Admin endpoints for managing cinema halls")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminCinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	@Operation(summary = "Create a new cinema hall")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Cinema hall created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "409", description = "Hall with this name already exists") })
	public ResponseEntity<CinemaHallResponse> createHall(@Valid @RequestBody CinemaHallRequest request) {
		log.info("POST /api/admin/cinema-halls - Creating new cinema hall: {}", request.getName());
		CinemaHallResponse created = cinemaHallService.createHall(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	@Operation(summary = "Get all cinema halls")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Halls retrieved successfully") })
	public ResponseEntity<List<CinemaHallResponse>> getAllHalls() {
		log.info("GET /api/admin/cinema-halls - Getting all cinema halls");
		List<CinemaHallResponse> halls = cinemaHallService.getAllHalls();
		return ResponseEntity.ok(halls);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get cinema hall by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Hall found"),
			@ApiResponse(responseCode = "404", description = "Hall not found") })
	public ResponseEntity<CinemaHallResponse> getHallById(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/admin/cinema-halls/{} - Getting cinema hall", id);
		CinemaHallResponse hall = cinemaHallService.getHallById(id);
		return ResponseEntity.ok(hall);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update cinema hall")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Hall updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "Hall not found"),
			@ApiResponse(responseCode = "409", description = "Cannot update hall with active sessions") })
	public ResponseEntity<CinemaHallResponse> updateHall(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long id,
			@Valid @RequestBody CinemaHallRequest request) {
		log.info("PUT /api/admin/cinema-halls/{} - Updating cinema hall", id);
		CinemaHallResponse updated = cinemaHallService.updateHall(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete cinema hall")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Hall deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Hall not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete hall with active sessions") })
	public ResponseEntity<Void> deleteHall(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/admin/cinema-halls/{} - Deleting cinema hall", id);
		cinemaHallService.deleteHall(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/layout")
	@Operation(summary = "Get hall layout")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Layout retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Hall not found") })
	public ResponseEntity<HallLayoutResponse> getHallLayout(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/admin/cinema-halls/{}/layout - Getting hall layout", id);
		HallLayoutResponse layout = cinemaHallService.getHallLayout(id);
		return ResponseEntity.ok(layout);
	}
}