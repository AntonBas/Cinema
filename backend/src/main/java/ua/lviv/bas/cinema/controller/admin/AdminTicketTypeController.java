package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@RestController
@RequestMapping("/api/admin/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Admin Ticket Types", description = "Admin API for managing ticket types")
public class AdminTicketTypeController {

	private final TicketTypeService ticketTypeService;
	private final TicketTypeMapper ticketTypeMapper;

	@PostMapping
	@Operation(summary = "Create a new ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Ticket type created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "409", description = "Ticket type with this code already exists") })
	public ResponseEntity<TicketTypeResponse> createTicketType(
			@Valid @RequestBody TicketTypeCreateRequest createRequest) {
		var ticketType = ticketTypeService.createTicketType(createRequest);
		var response = ticketTypeMapper.toResponseDto(ticketType);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@Operation(summary = "Get all ticket types")
	public ResponseEntity<List<TicketTypeResponse>> getAllTicketTypes(
			@Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {

		List<TicketTypeResponse> response;
		if (active != null) {
			var ticketTypes = active ? ticketTypeService.getAllActiveTicketTypes()
					: ticketTypeService.getAllTicketTypes().stream().filter(t -> !t.isActive()).toList();
			response = ticketTypeMapper.toResponseDtoList(ticketTypes);
		} else {
			response = ticketTypeMapper.toResponseDtoList(ticketTypeService.getAllTicketTypes());
		}

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

	@PutMapping("/{id}")
	@Operation(summary = "Update ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found") })
	public ResponseEntity<TicketTypeResponse> updateTicketType(
			@Parameter(description = "Ticket type ID") @PathVariable Long id,
			@Valid @RequestBody TicketTypeUpdateRequest updateRequest) {
		var ticketType = ticketTypeService.updateTicketType(id, updateRequest);
		var response = ticketTypeMapper.toResponseDto(ticketType);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Ticket type deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Ticket type is in use and cannot be deleted") })
	public ResponseEntity<Void> deleteTicketType(@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		ticketTypeService.deleteTicketType(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/toggle-active")
	@Operation(summary = "Toggle ticket type active status")
	public ResponseEntity<TicketTypeResponse> toggleTicketTypeActive(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		var ticketType = ticketTypeService.toggleTicketTypeActiveStatus(id);
		var response = ticketTypeMapper.toResponseDto(ticketType);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}/activate")
	@Operation(summary = "Activate ticket type")
	public ResponseEntity<TicketTypeResponse> activateTicketType(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		var ticketType = ticketTypeService.getTicketTypeById(id);
		ticketType.setActive(true);
		var updated = ticketTypeService.updateTicketType(id, TicketTypeUpdateRequest.builder().active(true).build());
		var response = ticketTypeMapper.toResponseDto(updated);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}/deactivate")
	@Operation(summary = "Deactivate ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type deactivated"),
			@ApiResponse(responseCode = "409", description = "Ticket type is in use and cannot be deactivated") })
	public ResponseEntity<TicketTypeResponse> deactivateTicketType(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		var ticketType = ticketTypeService.getTicketTypeById(id);
		ticketType.setActive(false);
		var updated = ticketTypeService.updateTicketType(id, TicketTypeUpdateRequest.builder().active(false).build());
		var response = ticketTypeMapper.toResponseDto(updated);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/simple")
	@Operation(summary = "Get simple list of ticket types")
	public ResponseEntity<List<TicketTypeSimpleResponse>> getSimpleTicketTypes(
			@Parameter(description = "Filter by active status") @RequestParam(required = false, defaultValue = "true") Boolean active) {

		List<TicketTypeSimpleResponse> response;
		if (active) {
			response = ticketTypeMapper.toSimpleDtoList(ticketTypeService.getAllActiveTicketTypes());
		} else {
			response = ticketTypeMapper.toSimpleDtoList(ticketTypeService.getAllTicketTypes());
		}

		return ResponseEntity.ok(response);
	}
}