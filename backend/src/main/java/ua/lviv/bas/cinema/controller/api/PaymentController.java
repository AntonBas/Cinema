package ua.lviv.bas.cinema.controller.api;

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
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
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
	@Operation(summary = "Create payment", description = "Creates a payment for a booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Payment created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "409", description = "Payment already in progress") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Creating payment for booking ID: {} by user ID: {}", request.getBookingId(), user.getId());
		PaymentResponse response = paymentService.createPayment(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{paymentId}/liqpay-data")
	@Operation(summary = "Get LiqPay data", description = "Returns prepared data for LiqPay payment gateway")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment data retrieved"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied"),
			@ApiResponse(responseCode = "400", description = "Payment is not pending") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentLiqPayDataResponse> getLiqPayPaymentData(
			@Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Fetching LiqPay data for payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentLiqPayDataResponse response = paymentService.preparePaymentData(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{paymentId}")
	@Operation(summary = "Get payment by ID", description = "Retrieves payment information by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment retrieved"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentResponse> getPaymentById(
			@Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Fetching payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentResponse response = paymentService.getPaymentStatus(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{paymentId}/status")
	@Operation(summary = "Get payment status", description = "Retrieves the current status of a payment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment status retrieved"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentResponse> getPaymentStatus(
			@Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Fetching status for payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentResponse response = paymentService.getPaymentStatus(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{paymentId}/retry")
	@Operation(summary = "Retry payment", description = "Retries a previously failed payment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment retry initiated"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied"),
			@ApiResponse(responseCode = "400", description = "Payment cannot be retried") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentResponse> retryPayment(
			@Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Retrying payment ID: {} for user ID: {}", paymentId, user.getId());
		PaymentResponse response = paymentService.retryPayment(paymentId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/booking/{bookingId}")
	@Operation(summary = "Get payment by booking", description = "Retrieves payment information for a specific booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment retrieved"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PaymentResponse> getPaymentByBookingId(
			@Parameter(description = "Booking ID", required = true) @PathVariable Long bookingId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Fetching payment for booking ID: {} for user ID: {}", bookingId, user.getId());
		PaymentResponse response = paymentService.getUserPaymentByBookingId(bookingId, user);
		return ResponseEntity.ok(response);
	}
}