package ua.lviv.bas.cinema.controller.api;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

	@GetMapping
	@Operation(summary = "Get user tickets with filters", description = "Get tickets with advanced filtering options")
	public ResponseEntity<PageResponse<TicketResponse>> getUserTickets(
			@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDateTo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDateTo,
			@RequestParam(required = false) Long movieId,
			@PageableDefault(size = 10, sort = "purchaseTime", direction = Sort.Direction.DESC) Pageable pageable) {

		User user = userDetails.getUser();
		log.info(
				"Getting tickets for user ID: {}, filters: status={}, purchaseDateFrom={}, purchaseDateTo={}, sessionDateFrom={}, sessionDateTo={}, movieId={}",
				user.getId(), status, purchaseDateFrom, purchaseDateTo, sessionDateFrom, sessionDateTo, movieId);

		TicketFilterRequest filter = TicketFilterRequest.builder().status(status).purchaseDateFrom(purchaseDateFrom)
				.purchaseDateTo(purchaseDateTo).sessionDateFrom(sessionDateFrom).sessionDateTo(sessionDateTo)
				.movieId(movieId).build();

		Page<TicketResponse> tickets = ticketRetrievalService.getUserTickets(user, filter, pageable);
		return ResponseEntity.ok(PageResponse.from(tickets));
	}

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming tickets", description = "Get upcoming tickets for authenticated user")
	public ResponseEntity<PageResponse<TicketResponse>> getUpcomingTickets(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PageableDefault(size = 10, sort = "sessionTime", direction = Sort.Direction.ASC) Pageable pageable) {

		User user = userDetails.getUser();
		log.info("Getting upcoming tickets for user ID: {}", user.getId());

		TicketFilterRequest filter = TicketFilterRequest.builder().status(TicketStatus.ACTIVE)
				.sessionDateFrom(LocalDate.now()).build();

		Page<TicketResponse> tickets = ticketRetrievalService.getUserTickets(user, filter, pageable);
		return ResponseEntity.ok(PageResponse.from(tickets));
	}

	@GetMapping("/{ticketId}")
	@Operation(summary = "Get ticket details", description = "Get ticket details by ID")
	public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();
		log.info("Getting ticket ID: {} for user ID: {}", ticketId, user.getId());
		TicketResponse ticket = ticketRetrievalService.getTicketById(ticketId, user);
		return ResponseEntity.ok(ticket);
	}

	@GetMapping("/code/{ticketCode}")
	@Operation(summary = "Get ticket by code", description = "Get ticket details by code")
	public ResponseEntity<TicketResponse> getTicketByCode(@PathVariable String ticketCode,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();
		log.info("Getting ticket by code: {} for user ID: {}", ticketCode, user.getId());
		TicketResponse ticket = ticketRetrievalService.getTicketByCode(ticketCode, user);
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
	@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
	public ResponseEntity<Void> validateTicket(@PathVariable String ticketCode) {
		log.info("Validating ticket: {}", ticketCode);
		ticketService.validateTicket(ticketCode);
		return ResponseEntity.ok().build();
	}
}