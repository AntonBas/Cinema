package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.service.booking.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for processing and managing payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@Operation(summary = "Create a payment for a booking", description = "Creates a payment record for a booking and prepares it for processing")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Payment created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request or booking not eligible for payment"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "409", description = "Payment already in progress") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request,
			@AuthenticationPrincipal User user) {

		log.info("Creating payment for booking ID: {} by user ID: {}", request.getBookingId(), user.getId());
		PaymentResponse response = paymentService.createPayment(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/liqpay/callback")
	@Operation(summary = "LiqPay payment callback", description = "Endpoint for LiqPay payment gateway callbacks (used internally by payment system)")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Callback processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid callback data") })
	public ResponseEntity<String> processLiqPayCallback(@Valid @RequestBody LiqPayCallbackRequest callbackRequest) {

		log.info("Received LiqPay callback for order ID: {}", callbackRequest.getOrderId());
		paymentService.processLiqPayCallback(callbackRequest);
		return ResponseEntity.ok("OK");
	}

	@GetMapping("/{paymentId}/liqpay-data")
	@Operation(summary = "Get LiqPay payment data", description = "Returns prepared data for LiqPay payment gateway integration")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment data retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to payment"),
			@ApiResponse(responseCode = "400", description = "Payment is not in pending state") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<PaymentLiqPayDataResponse> getLiqPayPaymentData(
			@Parameter(description = "ID of the payment", required = true, example = "1") @PathVariable Long paymentId,

			@AuthenticationPrincipal User user) {

		log.info("Fetching LiqPay data for payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentLiqPayDataResponse response = paymentService.preparePaymentData(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{paymentId}/status")
	@Operation(summary = "Get payment status", description = "Retrieves the current status and details of a payment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to payment") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<PaymentResponse> getPaymentStatus(
			@Parameter(description = "ID of the payment", required = true, example = "1") @PathVariable Long paymentId,

			@AuthenticationPrincipal User user) {

		log.info("Fetching status for payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentResponse response = paymentService.getPaymentStatus(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{paymentId}/retry")
	@Operation(summary = "Retry a failed payment", description = "Retries a previously failed payment with a new payment request")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment retry initiated successfully"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to payment"),
			@ApiResponse(responseCode = "400", description = "Payment cannot be retried") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<PaymentResponse> retryPayment(
			@Parameter(description = "ID of the payment to retry", required = true, example = "1") @PathVariable Long paymentId,

			@AuthenticationPrincipal User user) {

		log.info("Retrying payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentResponse response = paymentService.retryPayment(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/user")
	@Operation(summary = "Get user payments", description = "Retrieves a list of all payments for the authenticated user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payments retrieved successfully") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<PaymentResponse>> getUserPayments(@AuthenticationPrincipal User user) {

		log.info("Fetching all payments for user ID: {}", user.getId());
		List<PaymentResponse> payments = paymentService.getUserPayments(user.getId());
		return ResponseEntity.ok(payments);
	}
}