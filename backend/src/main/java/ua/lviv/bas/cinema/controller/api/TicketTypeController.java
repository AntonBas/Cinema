package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@Slf4j
@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Ticket Types", description = "Public API for ticket types")
public class TicketTypeController {
	private final TicketTypeService ticketTypeService;

	@GetMapping("/dropdown")
	@Operation(summary = "Get active ticket types for user dropdown")
	public ResponseEntity<List<TicketTypeUserResponse>> getDropdownTypes() {
		log.info("Getting ticket types for user dropdown");
		List<TicketTypeUserResponse> ticketTypes = ticketTypeService.getActiveTicketTypesForUser();
		return ResponseEntity.ok(ticketTypes);
	}
}