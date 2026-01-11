package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.service.booking.RefundService;

@Slf4j
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Ticket refund operations")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

	private final RefundService refundService;

	@PostMapping("/preview")
	@Operation(summary = "Get refund preview", description = "Calculate refund amount for a ticket")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Refund preview calculated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	public ResponseEntity<RefundPreviewResponse> getRefundPreview(@Valid @RequestBody RefundPreviewRequest request,
			@AuthenticationPrincipal User user) {

		log.info("Getting refund preview for ticket {} by user {}", request.getTicketId(), user.getId());
		RefundPreviewResponse response = refundService.getRefundPreview(request, user.getId());
		return ResponseEntity.ok(response);
	}

	@PostMapping
	@Operation(summary = "Process refund", description = "Process automatic refund for a ticket")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request or ticket not refundable"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "403", description = "Access denied"),
			@ApiResponse(responseCode = "500", description = "Refund processing failed") })
	public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request,
			@AuthenticationPrincipal User user) {

		log.info("Processing refund for ticket {} by user {}", request.getTicketId(), user.getId());
		RefundResponse response = refundService.processRefund(request, user.getId());
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@Operation(summary = "Get user refunds", description = "Get refund history for authenticated user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	public ResponseEntity<List<RefundResponse>> getUserRefunds(@AuthenticationPrincipal User user) {
		log.info("Getting refunds for user {}", user.getId());
		List<RefundResponse> refunds = refundService.getUserRefunds(user.getId());
		return ResponseEntity.ok(refunds);
	}
}