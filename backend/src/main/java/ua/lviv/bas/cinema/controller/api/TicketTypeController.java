package ua.lviv.bas.cinema.controller.api;

import java.util.List;
import java.util.stream.Collectors;

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
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@Slf4j
@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Ticket Types", description = "Public API for ticket types")
public class TicketTypeController {

	private final TicketTypeService ticketTypeService;

	@GetMapping
	@Operation(summary = "Get all active ticket types")
	public ResponseEntity<List<TicketTypeResponse>> getAllActiveTicketTypes() {
		log.info("Getting all active ticket types");
		List<TicketTypeResponse> ticketTypes = ticketTypeService.getAllTicketTypes(true);
		return ResponseEntity.ok(ticketTypes);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get ticket type by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type found"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found") })
	public ResponseEntity<TicketTypeResponse> getTicketTypeById(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		log.info("Getting ticket type by ID: {}", id);
		var ticketType = ticketTypeService.getTicketTypeById(id);
		return ResponseEntity.ok(ticketType);
	}

	@GetMapping("/code/{code}")
	@Operation(summary = "Get ticket type by code")
	public ResponseEntity<TicketTypeResponse> getTicketTypeByCode(
			@Parameter(description = "Ticket type code") @PathVariable String code) {
		log.info("Getting ticket type by code: {}", code);
		var ticketType = ticketTypeService.getTicketTypeByCode(code);
		return ResponseEntity.ok(ticketType);
	}

	@GetMapping("/simple")
	@Operation(summary = "Get simple list of active ticket types")
	public ResponseEntity<List<TicketTypeSimpleResponse>> getSimpleActiveTicketTypes() {
		log.info("Getting simple active ticket types");
		List<TicketTypeSimpleResponse> ticketTypes = ticketTypeService.getSimpleTicketTypes(true);
		return ResponseEntity.ok(ticketTypes);
	}

	@GetMapping("/age-validation")
	@Operation(summary = "Validate age for ticket type")
	public ResponseEntity<Boolean> validateAgeForTicketType(
			@Parameter(description = "Ticket type ID") @RequestParam Long ticketTypeId,
			@Parameter(description = "Age to validate") @RequestParam Integer age) {
		log.info("Validating age {} for ticket type ID: {}", age, ticketTypeId);
		boolean isValid = ticketTypeService.validateAgeForTicketType(ticketTypeId, age);
		return ResponseEntity.ok(isValid);
	}

	@GetMapping("/{id}/age-range")
	@Operation(summary = "Get formatted age range for ticket type")
	public ResponseEntity<String> getFormattedAgeRange(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		log.info("Getting age range for ticket type ID: {}", id);
		String ageRange = ticketTypeService.getFormattedAgeRange(id);
		return ResponseEntity.ok(ageRange);
	}

	@GetMapping("/available-for-age")
	@Operation(summary = "Get ticket types available for specific age")
	public ResponseEntity<List<TicketTypeSimpleResponse>> getTicketTypesForAge(
			@Parameter(description = "Age to check") @RequestParam Integer age) {
		log.info("Getting ticket types available for age: {}", age);
		List<TicketTypeSimpleResponse> allActive = ticketTypeService.getSimpleTicketTypes(true);

		List<TicketTypeSimpleResponse> available = allActive.stream().filter(tt -> {
			boolean isValid = ticketTypeService.validateAgeForTicketType(tt.getId(), age);
			log.debug("Ticket type {} (ID: {}) - valid for age {}: {}", tt.getDisplayName(), tt.getId(), age, isValid);
			return isValid;
		}).collect(Collectors.toList());

		log.info("Found {} ticket types available for age {}", available.size(), age);
		return ResponseEntity.ok(available);
	}
}