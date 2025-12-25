package ua.lviv.bas.cinema.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import ua.lviv.bas.cinema.service.common.CinemaHallService;

@Slf4j
@RestController
@RequestMapping("/api/admin/cinema-halls")
@RequiredArgsConstructor
@Tag(name = "Admin Cinema Hall Management", description = "Admin endpoints for managing cinema halls")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	@Operation(summary = "Create a new cinema hall", description = "Create a new cinema hall with specified configuration.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Cinema hall created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<CinemaHallResponse> createHall(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cinema hall creation request", required = true, content = @Content(schema = @Schema(implementation = CinemaHallRequest.class))) @Valid @RequestBody CinemaHallRequest request) {
		log.info("POST /api/admin/cinema-halls - Creating new cinema hall: {}", request.getName());
		CinemaHallResponse created = cinemaHallService.createHall(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update cinema hall", description = "Update existing cinema hall information.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cinema hall updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<CinemaHallResponse> updateHall(
			@Parameter(description = "ID of the cinema hall to update", required = true, example = "1") @PathVariable Long id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated cinema hall data", required = true, content = @Content(schema = @Schema(implementation = CinemaHallRequest.class))) @Valid @RequestBody CinemaHallRequest request) {
		log.info("PUT /api/admin/cinema-halls/{} - Updating cinema hall", id);
		CinemaHallResponse updated = cinemaHallService.updateHall(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete cinema hall", description = "Delete a cinema hall by its ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Cinema hall deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Cinema hall not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete hall with active sessions"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteHall(
			@Parameter(description = "ID of the cinema hall to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/admin/cinema-halls/{} - Deleting cinema hall", id);
		cinemaHallService.deleteHall(id);
		return ResponseEntity.noContent().build();
	}
}