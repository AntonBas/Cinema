package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.service.booking.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "APIs for managing and validating cinema tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

	private final TicketService ticketService;

	@GetMapping("/{ticketId}")
	@Operation(summary = "Get ticket details", description = "Retrieves detailed information about a specific ticket including QR code URL")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket details retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to ticket") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<TicketResponse> getTicket(
			@Parameter(description = "ID of the ticket", required = true, example = "1") @PathVariable Long ticketId,

			@AuthenticationPrincipal User user) {

		log.info("Fetching ticket ID: {} for user ID: {}", ticketId, user.getId());
		TicketResponse response = ticketService.getTicketById(ticketId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@Operation(summary = "Get user tickets", description = "Retrieves a list of tickets for the authenticated user, optionally filtered by status")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid status parameter") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<TicketResponse>> getUserTickets(
			@Parameter(description = "Filter by ticket status", example = "ACTIVE") @RequestParam(required = false) TicketStatus status,

			@AuthenticationPrincipal User user) {

		log.info("Fetching tickets for user ID: {} with status filter: {}", user.getId(), status);
		List<TicketResponse> tickets = ticketService.getUserTickets(user, status);
		return ResponseEntity.ok(tickets);
	}

	@GetMapping("/booking/{bookingId}")
	@Operation(summary = "Get tickets for booking", description = "Retrieves all tickets associated with a specific booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to booking") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<TicketResponse>> getBookingTickets(
			@Parameter(description = "ID of the booking", required = true, example = "1") @PathVariable Long bookingId,

			@AuthenticationPrincipal User user) {

		log.info("Fetching tickets for booking ID: {} for user ID: {}", bookingId, user.getId());
		List<TicketResponse> tickets = ticketService.getBookingTickets(bookingId, user);
		return ResponseEntity.ok(tickets);
	}

	@PostMapping("/validate/{ticketCode}")
	@Operation(summary = "Validate ticket", description = "Validates a ticket for entry and marks it as used if valid")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket validated successfully"),
			@ApiResponse(responseCode = "400", description = "Ticket validation failed"),
			@ApiResponse(responseCode = "404", description = "Ticket not found") })
	public ResponseEntity<String> validateTicket(
			@Parameter(description = "Unique ticket code", required = true, example = "TKT-ABC123DEF456") @PathVariable String ticketCode) {

		log.info("Validating ticket with code: {}", ticketCode);
		ticketService.validateTicket(ticketCode);
		return ResponseEntity.ok("Ticket validated successfully");
	}

	@GetMapping("/{ticketCode}/qr")
	@Operation(summary = "Get ticket QR code", description = "Generates and returns a QR code image for ticket validation")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "QR code generated successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket not found") })
	public ResponseEntity<Resource> getTicketQrCode(
			@Parameter(description = "Unique ticket code", required = true, example = "TKT-ABC123DEF456") @PathVariable String ticketCode) {

		log.info("Generating QR code for ticket: {}", ticketCode);
		byte[] qrCode = ticketService.generateTicketQRCode(ticketCode);

		ByteArrayResource resource = new ByteArrayResource(qrCode);

		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ticket-qr.png\"").body(resource);
	}

	@PostMapping("/{ticketId}/void")
	@Operation(summary = "Void a ticket", description = "Cancels a ticket if it hasn't been used yet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket voided successfully"),
			@ApiResponse(responseCode = "400", description = "Ticket cannot be voided"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to ticket") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<Void> voidTicket(
			@Parameter(description = "ID of the ticket to void", required = true, example = "1") @PathVariable Long ticketId,

			@AuthenticationPrincipal User user) {

		log.info("Voiding ticket ID: {} for user ID: {}", ticketId, user.getId());
		ticketService.voidTicket(ticketId, user);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{ticketCode}/status")
	@Operation(summary = "Check ticket status", description = "Checks the current status of a ticket without validating it")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ticket status retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Ticket not found") })
	public ResponseEntity<String> checkTicketStatus(
			@Parameter(description = "Unique ticket code", required = true, example = "TKT-ABC123DEF456") @PathVariable String ticketCode) {

		log.info("Checking status for ticket: {}", ticketCode);
		TicketStatus status = ticketService.checkTicketStatus(ticketCode);
		return ResponseEntity.ok(status != null ? status.toString() : "NOT_FOUND");
	}
}