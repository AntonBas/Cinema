package ua.lviv.bas.cinema.controller.api;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ticket.TicketRetrievalService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

	private final TicketRetrievalService ticketRetrievalService;
	private final TicketService ticketService;

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming tickets", description = "Get upcoming tickets for authenticated user")
	public ResponseEntity<PageResponse<TicketResponse>> getUpcomingTickets(
			@AuthenticationPrincipal CustomUserDetails userDetails, @PageableDefault(size = 10) Pageable pageable) {

		User user = userDetails.getUser();
		log.info("Getting upcoming tickets for user ID: {}", user.getId());

		TicketFilterRequest filter = TicketFilterRequest.builder().status(TicketStatus.ACTIVE)
				.sessionDateFrom(LocalDate.now()).build();

		Page<TicketResponse> tickets = ticketRetrievalService.getUserTickets(user, filter, pageable);
		return ResponseEntity.ok(PageResponse.from(tickets));
	}

	@GetMapping("/history")
	@Operation(summary = "Get ticket history", description = "Get paginated history of past tickets")
	public ResponseEntity<PageResponse<TicketResponse>> getTicketHistory(
			@AuthenticationPrincipal CustomUserDetails userDetails, @PageableDefault(size = 10) Pageable pageable) {

		User user = userDetails.getUser();
		log.info("Getting ticket history for user ID: {}", user.getId());

		TicketFilterRequest filter = TicketFilterRequest.builder().status(TicketStatus.USED)
				.sessionDateTo(LocalDate.now().minusDays(1)).build();

		Page<TicketResponse> tickets = ticketRetrievalService.getUserTickets(user, filter, pageable);
		return ResponseEntity.ok(PageResponse.from(tickets));
	}

	@GetMapping("/code/{ticketCode}")
	@Operation(summary = "Get ticket by code", description = "Get ticket details by code (for QR scanning)")
	public ResponseEntity<TicketResponse> getTicketByCode(@PathVariable String ticketCode,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();
		log.info("Getting ticket by code: {} for user ID: {}", ticketCode, user.getId());
		TicketResponse ticket = ticketRetrievalService.getTicketByCode(ticketCode, user);
		return ResponseEntity.ok(ticket);
	}

	@GetMapping("/code/{ticketCode}/qr")
	@Operation(summary = "Get ticket QR code", description = "Generate QR code for ticket validation")
	public ResponseEntity<byte[]> getTicketQRCode(@PathVariable String ticketCode) {
		log.info("Generating QR code for ticket: {}", ticketCode);
		byte[] qrCode = ticketService.generateTicketQRCode(ticketCode);
		return ResponseEntity.ok().header("Content-Type", "image/png").body(qrCode);
	}

	@PostMapping("/code/{ticketCode}/validate")
	@Operation(summary = "Validate ticket", description = "Validate ticket for entry (used by staff)")
	@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
	public ResponseEntity<Void> validateTicket(@PathVariable String ticketCode) {
		log.info("Validating ticket: {}", ticketCode);
		ticketService.validateTicket(ticketCode);
		return ResponseEntity.ok().build();
	}
}