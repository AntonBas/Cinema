package ua.lviv.bas.cinema.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.service.booking.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Admin Payments", description = "Administrative APIs for managing payments")
@SecurityRequirement(name = "bearerAuth")
public class AdminPaymentController {

	private final PaymentService paymentService;

	@GetMapping
	@Operation(summary = "Get all payments", description = "Retrieves a paginated list of all payments in the system with filtering options")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Page<PaymentResponse>> getAllPayments(@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(required = false) PaymentStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

		log.info("Admin fetching payments with filters - status: {}, dateFrom: {}, dateTo: {}", status, dateFrom,
				dateTo);
		Page<PaymentResponse> payments = paymentService.getAllPayments(pageable, status, dateFrom, dateTo);
		return ResponseEntity.ok(payments);
	}

	@GetMapping("/list")
	@Operation(summary = "Get all payments list", description = "Retrieves a list of all payments without pagination")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<PaymentResponse>> getAllPaymentsList() {
		log.info("Admin fetching all payments list");
		List<PaymentResponse> payments = paymentService.getAllPayments();
		return ResponseEntity.ok(payments);
	}

	@GetMapping("/{paymentId}")
	@Operation(summary = "Get payment details", description = "Retrieves detailed information about a specific payment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
		log.info("Admin fetching payment ID: {}", paymentId);
		PaymentResponse response = paymentService.getPaymentById(paymentId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/statistics/daily")
	@Operation(summary = "Get daily payment statistics", description = "Retrieves daily payment statistics for a date range")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid date range"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Map<LocalDate, BigDecimal>> getDailyPaymentStatistics(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.info("Admin fetching daily payment statistics from {} to {}", startDate, endDate);
		Map<LocalDate, BigDecimal> statistics = paymentService.getDailyPaymentStatistics(startDate, endDate);
		return ResponseEntity.ok(statistics);
	}
}