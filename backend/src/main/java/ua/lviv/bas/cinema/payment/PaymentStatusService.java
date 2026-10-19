package ua.lviv.bas.cinema.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.booking.BookingStatus;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.payment.exception.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.payment.exception.PaymentProcessingException;
import ua.lviv.bas.cinema.payment.gateway.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.gateway.dto.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private static final String EXPECTED_CURRENCY = "UAH";

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional(readOnly = true)
    public PaymentLiqPayDataResponse preparePaymentData(Long paymentId, User user) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));

        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new EntityNotFoundException("Payment", paymentId);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException(payment.getStatus(), PaymentStatus.PENDING);
        }
        var booking = payment.getBooking();
        if (booking.getStatus() != BookingStatus.PENDING || booking.getExpiresAt().isBefore(Instant.now())) {
            throw PaymentProcessingException.bookingExpired();
        }

        return paymentGatewayService.prepareLiqPayPaymentData(payment);
    }

    @Transactional
    public void handleCallback(String data, String signature) {
        var decodedData = paymentGatewayService.processCallback(data, signature);

        var orderId = decodedData.get("order_id");
        var status = decodedData.get("status");

        log.info("Received LiqPay callback for order {} with status {}", orderId, status);

        var payment = paymentRepository.findByLiqpayOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", orderId));

        if (status == null || status.isBlank()) {
            log.warn("LiqPay callback for payment {} has no status, ignoring it", payment.getId());
            return;
        }

        switch (status.toLowerCase()) {
            case "success", "sandbox" -> {
                validateGatewayAmount(payment, decodedData);
                paymentService.processSuccess(payment, decodedData);
            }
            case "failure", "error" -> paymentService.processFailure(payment, decodedData);
            default -> {
                log.info("LiqPay reported intermediate status {} for payment {}, marking it PROCESSING", status,
                        payment.getId());
                paymentService.markProcessing(payment);
            }
        }
    }

    private void validateGatewayAmount(Payment payment, Map<String, String> callbackData) {
        var currency = callbackData.get("currency");
        if (currency != null && !EXPECTED_CURRENCY.equalsIgnoreCase(currency)) {
            log.error("LiqPay callback for payment {} reported currency {} instead of {}", payment.getId(), currency,
                    EXPECTED_CURRENCY);
            throw new PaymentProcessingException("LiqPay callback currency does not match the payment");
        }
        var amount = callbackData.get("amount");
        if (amount == null || amount.isBlank()) {
            return;
        }
        try {
            if (new BigDecimal(amount).compareTo(payment.getAmount()) != 0) {
                log.error("LiqPay callback for payment {} reported amount {} instead of {}", payment.getId(), amount,
                        payment.getAmount());
                throw new PaymentProcessingException("LiqPay callback amount does not match the payment");
            }
        } catch (NumberFormatException e) {
            throw new PaymentProcessingException("LiqPay callback amount is not a number");
        }
    }
}
