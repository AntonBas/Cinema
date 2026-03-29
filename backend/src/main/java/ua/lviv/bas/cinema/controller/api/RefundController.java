package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.service.booking.refund.RefundService;

@Slf4j
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Ticket refund operations")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {
	private final RefundService refundService;

	@RateLimit(value = 3, duration = 10, key = "user")
	@PostMapping
	@Operation(summary = "Refund a ticket", description = "Process refund for a ticket with automatic calculation")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request or ticket not refundable"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<RefundResponse> refundTicket(@Valid @RequestBody RefundRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User user = userDetails.getUser();
		log.info("Processing refund for ticket {} by user {}", request.ticketId(), user.getId());
		RefundResponse response = refundService.processRefund(request, user.getId());
		return ResponseEntity.ok(response);
	}
}