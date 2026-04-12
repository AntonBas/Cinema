package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeResponse;
import ua.lviv.bas.cinema.service.ticket.TicketTypeService;

@Slf4j
@RestController
@RequestMapping("/api/admin/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Admin Ticket Types", description = "Admin API for managing ticket types")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminTicketTypeController {

	private final TicketTypeService ticketTypeService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a new ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Ticket type created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "409", description = "Ticket type with this display name already exists") })
	public TicketTypeResponse createTicketType(@Valid @RequestBody TicketTypeRequest request) {
		log.info("POST /api/admin/ticket-types - Creating ticket type: {}", request.displayName());
		return ticketTypeService.createTicketType(request);
	}

	@GetMapping
	@Operation(summary = "Get all ticket types")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully") })
	public PageResponse<TicketTypeResponse> getTicketTypes(@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) TicketTypeCategory category, @RequestParam(required = false) String query,
			@PageableDefault(size = 10) Pageable pageable) {
		log.info("GET /api/admin/ticket-types - active: {}, category: {}, query: {}", active, category, query);
		return PageResponse.from(ticketTypeService.getTicketTypes(active, category, query, pageable));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Ticket type with this display name already exists") })
	public TicketTypeResponse updateTicketType(@PathVariable Long id, @Valid @RequestBody TicketTypeRequest request) {
		log.info("PUT /api/admin/ticket-types/{} - Updating ticket type", id);
		return ticketTypeService.updateTicketType(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete ticket type")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Ticket type deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Ticket type is in use and cannot be deleted") })
	public void deleteTicketType(@PathVariable Long id) {
		log.info("DELETE /api/admin/ticket-types/{} - Deleting ticket type", id);
		ticketTypeService.deleteTicketType(id);
	}

	@PatchMapping("/{id}/toggle")
	@Operation(summary = "Toggle ticket type active status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket type status toggled successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket type not found"),
			@ApiResponse(responseCode = "409", description = "Cannot deactivate ticket type in use") })
	public TicketTypeResponse toggleActive(@PathVariable Long id) {
		log.info("PATCH /api/admin/ticket-types/{}/toggle - Toggling active status", id);
		return ticketTypeService.toggleActiveStatus(id);
	}
}