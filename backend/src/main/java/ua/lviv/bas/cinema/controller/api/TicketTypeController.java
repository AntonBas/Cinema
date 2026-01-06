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
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Ticket Types", description = "Public API for ticket types")
public class TicketTypeController {

	private final TicketTypeService ticketTypeService;
	private final TicketTypeMapper ticketTypeMapper;

	@GetMapping
	@Operation(summary = "Get all active ticket types")
	public ResponseEntity<List<TicketTypeResponse>> getAllActiveTicketTypes() {
		var ticketTypes = ticketTypeService.getAllActiveTicketTypes();
		var response = ticketTypeMapper.toResponseDtoList(ticketTypes);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get ticket type by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type found"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found") })
	public ResponseEntity<TicketTypeResponse> getTicketTypeById(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		var ticketType = ticketTypeService.getTicketTypeById(id);
		var response = ticketTypeMapper.toResponseDto(ticketType);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/code/{code}")
	@Operation(summary = "Get ticket type by code")
	public ResponseEntity<TicketTypeResponse> getTicketTypeByCode(
			@Parameter(description = "Ticket type code") @PathVariable String code) {
		var ticketType = ticketTypeService.getTicketTypeByCode(code);
		var response = ticketTypeMapper.toResponseDto(ticketType);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/simple")
	@Operation(summary = "Get simple list of active ticket types")
	public ResponseEntity<List<TicketTypeSimpleResponse>> getSimpleActiveTicketTypes() {
		var response = ticketTypeMapper.toSimpleDtoList(ticketTypeService.getAllActiveTicketTypes());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/age-validation")
	@Operation(summary = "Validate age for ticket type")
	public ResponseEntity<Boolean> validateAgeForTicketType(
			@Parameter(description = "Ticket type ID") @RequestParam Long ticketTypeId,
			@Parameter(description = "Age to validate") @RequestParam Integer age) {
		boolean isValid = ticketTypeService.validateAgeForTicketType(ticketTypeId, age);
		return ResponseEntity.ok(isValid);
	}

	@GetMapping("/{id}/age-range")
	@Operation(summary = "Get formatted age range for ticket type")
	public ResponseEntity<String> getFormattedAgeRange(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		String ageRange = ticketTypeService.getFormattedAgeRange(id);
		return ResponseEntity.ok(ageRange);
	}

	@GetMapping("/available-for-age")
	@Operation(summary = "Get ticket types available for specific age")
	public ResponseEntity<List<TicketTypeSimpleResponse>> getTicketTypesForAge(
			@Parameter(description = "Age to check") @RequestParam Integer age) {
		var allActive = ticketTypeService.getAllActiveTicketTypes();
		var available = allActive.stream().filter(tt -> ticketTypeService.isAgeValidForTicketType(tt, age)).toList();
		var response = ticketTypeMapper.toSimpleDtoList(available);
		return ResponseEntity.ok(response);
	}
}