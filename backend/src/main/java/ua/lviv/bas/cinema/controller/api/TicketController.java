package ua.lviv.bas.cinema.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {
	private final ControllerFacade controllerFacade;
	private final TicketService ticketService;

	@GetMapping
	@Operation(summary = "Get user tickets", description = "Get all tickets for authenticated user with pagination")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Page<TicketResponse>> getUserTickets(@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) String search, @AuthenticationPrincipal CustomUserDetails userDetails,
			@PageableDefault(size = 10, sort = "purchaseTime", direction = Sort.Direction.DESC) Pageable pageable) {
		User user = userDetails.getUser();
		log.info("Getting tickets for user ID: {} with status: {}, search: {}, page: {}, size: {}", user.getId(),
				status, search, pageable.getPageNumber(), pageable.getPageSize());
		Page<TicketResponse> tickets = controllerFacade.getUserTickets(user, status, search, pageable);
		return ResponseEntity.ok(tickets);
	}

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming tickets", description = "Get upcoming tickets for authenticated user with pagination")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Page<TicketResponse>> getUpcomingTickets(@RequestParam(required = false) String search,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PageableDefault(size = 10, sort = "booking.session.startTime", direction = Sort.Direction.ASC) Pageable pageable) {
		User user = userDetails.getUser();
		log.info("Getting upcoming tickets for user ID: {}, search: {}, page: {}, size: {}", user.getId(), search,
				pageable.getPageNumber(), pageable.getPageSize());
		Page<TicketResponse> tickets = controllerFacade.getUpcomingTickets(user, search, pageable);
		return ResponseEntity.ok(tickets);
	}

	@GetMapping("/{ticketId}")
	@Operation(summary = "Get ticket details", description = "Get ticket details by ID")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Getting ticket ID: {} for user ID: {}", ticketId, user.getId());
		TicketResponse ticket = controllerFacade.getTicketById(ticketId, user);
		return ResponseEntity.ok(ticket);
	}

	@GetMapping("/code/{ticketCode}")
	@Operation(summary = "Get ticket by code", description = "Get ticket details by code")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<TicketResponse> getTicketByCode(@PathVariable String ticketCode,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Getting ticket by code: {} for user ID: {}", ticketCode, user.getId());
		TicketResponse ticket = controllerFacade.getTicketByCode(ticketCode, user);
		return ResponseEntity.ok(ticket);
	}

	@GetMapping("/{ticketCode}/qr")
	@Operation(summary = "Get ticket QR code", description = "Generate QR code for ticket validation")
	public ResponseEntity<byte[]> getTicketQRCode(@PathVariable String ticketCode) {
		log.info("Generating QR code for ticket: {}", ticketCode);
		byte[] qrCode = ticketService.generateTicketQRCode(ticketCode);
		return ResponseEntity.ok().header("Content-Type", "image/png").body(qrCode);
	}

	@PostMapping("/{ticketCode}/validate")
	@Operation(summary = "Validate ticket", description = "Validate ticket for entry (used by staff)")
	public ResponseEntity<Void> validateTicket(@PathVariable String ticketCode) {
		log.info("Validating ticket: {}", ticketCode);
		ticketService.validateTicket(ticketCode);
		return ResponseEntity.ok().build();
	}
}