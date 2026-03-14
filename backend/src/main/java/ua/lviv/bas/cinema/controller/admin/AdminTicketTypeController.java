package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@Slf4j
@RestController
@RequestMapping("/api/admin/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Admin Ticket Types", description = "Admin API for managing ticket types")
@SecurityRequirement(name = "bearerAuth")
public class AdminTicketTypeController {

	private final TicketTypeService ticketTypeService;

	@PostMapping
	@Operation(summary = "Create a new ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Ticket type created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "409", description = "Ticket type with this display name already exists") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<TicketTypeResponse> createTicketType(
			@Parameter(description = "Ticket type creation request") @Valid @RequestBody TicketTypeCreateRequest request) {
		log.info("Creating new ticket type with display name: {}", request.getDisplayName());
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketTypeService.createTicketType(request));
	}

	@GetMapping
	@Operation(summary = "Get all ticket types with pagination and filters")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PageResponse<TicketTypeResponse>> getTicketTypes(
			@Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
			@Parameter(description = "Filter by category") @RequestParam(required = false) TicketTypeCategory category,
			@Parameter(description = "Search by display name") @RequestParam(required = false) String search,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
		log.info("Getting ticket types with filters - active: {}, category: {}, search: {}", active, category, search);
		return ResponseEntity.ok(ticketTypeService.getTicketTypesForAdmin(active, category, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get ticket type by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type found"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<TicketTypeResponse> getTicketTypeById(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		log.info("Getting ticket type by ID: {}", id);
		return ResponseEntity.ok(ticketTypeService.getTicketTypeById(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Ticket type with this display name already exists") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<TicketTypeResponse> updateTicketType(
			@Parameter(description = "Ticket type ID") @PathVariable Long id,
			@Parameter(description = "Ticket type update request") @Valid @RequestBody TicketTypeUpdateRequest request) {
		log.info("Updating ticket type ID: {}", id);
		return ResponseEntity.ok(ticketTypeService.updateTicketType(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Ticket type deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Ticket type is in use and cannot be deleted") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> deleteTicketType(@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		log.info("Deleting ticket type ID: {}", id);
		ticketTypeService.deleteTicketType(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/toggle-active")
	@Operation(summary = "Toggle ticket type active status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type status toggled successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Cannot deactivate ticket type in use") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<TicketTypeResponse> toggleTicketTypeActive(
			@Parameter(description = "Ticket type ID") @PathVariable Long id) {
		log.info("Toggling active status for ticket type ID: {}", id);
		return ResponseEntity.ok(ticketTypeService.toggleTicketTypeActiveStatus(id));
	}
}