package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.service.booking.PaymentService;
import ua.lviv.bas.cinema.service.booking.PaymentStatusService;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for processing and managing payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentStatusService paymentStatusService;

    @RateLimit(duration = 5, key = "user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Payment already in progress")
    })
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse createPayment(@Valid @RequestBody PaymentCreateRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        log.info("POST /api/payments - user: {}, booking: {}", user.getId(), request.bookingId());
        return paymentService.createPayment(request, user);
    }

    @RateLimit(duration = 1, key = "user")
    @PostMapping("/{paymentId}/retry")
    @Operation(summary = "Retry failed payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retry initiated"),
            @ApiResponse(responseCode = "400", description = "Payment cannot be retried"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse retryPayment(@PathVariable Long paymentId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        log.info("POST /api/payments/{}/retry - user: {}", paymentId, user.getId());
        return paymentService.retryPayment(paymentId, user);
    }

    @RateLimit(value = 10, duration = 1, key = "user")
    @GetMapping("/{paymentId}/liqpay-data")
    @Operation(summary = "Get LiqPay data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment data retrieved"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public PaymentLiqPayDataResponse getLiqPayData(@PathVariable Long paymentId) {
        log.info("GET /api/payments/{}/liqpay-data", paymentId);
        return paymentStatusService.preparePaymentData(paymentId);
    }

    @RateLimit(value = 20, duration = 1, key = "user")
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse getPayment(@PathVariable Long paymentId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        log.info("GET /api/payments/{} - user: {}", paymentId, user.getId());
        return paymentService.getPayment(paymentId, user);
    }
}