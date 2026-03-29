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
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.service.cinema.SeatService;

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
	@Operation(summary = "Update seat type")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat type updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid seat type"),
			@ApiResponse(responseCode = "404", description = "Seat not found") })
	public ResponseEntity<SeatResponse> updateSeatType(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "Seat ID", required = true, example = "5") @PathVariable Long seatId,
			@Parameter(description = "New seat type", required = true, example = "VIP") @RequestParam SeatType seatType) {
		log.info("PUT /api/admin/cinema-halls/{}/seats/{}/type - Updating seat type to {}", hallId, seatId, seatType);
		SeatResponse updated = seatService.updateSeatType(hallId, seatId, seatType);
		return ResponseEntity.ok(updated);
	}

	@PutMapping("/{seatId}/status")
	@Operation(summary = "Set seat active status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Seat status updated successfully"),
			@ApiResponse(responseCode = "404", description = "Seat not found") })
	public ResponseEntity<SeatResponse> setSeatActiveStatus(
			@Parameter(description = "Hall ID", required = true, example = "1") @PathVariable Long hallId,
			@Parameter(description = "Seat ID", required = true, example = "5") @PathVariable Long seatId,
			@Parameter(description = "Active status", required = true, example = "true") @RequestParam boolean active) {
		log.info("PUT /api/admin/cinema-halls/{}/seats/{}/status - Setting active status to {}", hallId, seatId,
				active);
		SeatResponse updated = seatService.setSeatActiveStatus(hallId, seatId, active);
		return ResponseEntity.ok(updated);
	}
}