package ua.lviv.bas.cinema.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.service.common.SeatService;

@Slf4j
@RestController
@RequestMapping("/api/admin/cinema-halls/{hallId}/seats")
@RequiredArgsConstructor
@Tag(name = "Admin Seat Management", description = "Admin endpoints for managing seats")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminSeatController {

	private final SeatService seatService;

	@PutMapping("/{seatId}/type")
	@Operation(summary = "Update seat type", description = "Change the type of a specific seat (e.g., STANDARD to VIP).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat type updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid seat type"),
			@ApiResponse(responseCode = "404", description = "Seat not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<SeatResponse> updateSeatType(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "ID of the seat to update", required = true, example = "5") @PathVariable Long seatId,
			@Parameter(description = "New seat type", required = true, example = "VIP") @RequestParam SeatType seatType) {
		log.info("PUT /api/admin/cinema-halls/{}/seats/{}/type?seatType={} - Updating seat type", hallId, seatId,
				seatType);
		SeatResponse updated = seatService.updateSeatType(seatId, seatType);
		return ResponseEntity.ok(updated);
	}

	@PutMapping("/{seatId}/activate")
	@Operation(summary = "Activate seat", description = "Activate a seat that was previously deactivated")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat activated successfully"),
			@ApiResponse(responseCode = "404", description = "Seat not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<SeatResponse> activateSeat(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "ID of the seat to activate", required = true, example = "5") @PathVariable Long seatId) {
		log.info("PUT /api/admin/cinema-halls/{}/seats/{}/activate - Activating seat", hallId, seatId);
		SeatResponse updated = seatService.activateSeat(seatId);
		return ResponseEntity.ok(updated);
	}

	@PutMapping("/{seatId}/deactivate")
	@Operation(summary = "Deactivate seat", description = "Deactivate a seat (makes it unavailable for booking)")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat deactivated successfully"),
			@ApiResponse(responseCode = "404", description = "Seat not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<SeatResponse> deactivateSeat(
			@Parameter(description = "ID of the cinema hall", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "ID of the seat to deactivate", required = true, example = "5") @PathVariable Long seatId) {
		log.info("PUT /api/admin/cinema-halls/{}/seats/{}/deactivate - Deactivating seat", hallId, seatId);
		SeatResponse updated = seatService.deactivateSeat(seatId);
		return ResponseEntity.ok(updated);
	}
}