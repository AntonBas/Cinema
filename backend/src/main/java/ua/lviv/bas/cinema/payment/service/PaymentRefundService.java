package ua.lviv.bas.cinema.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.service.integration.audit.AuditDetails;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.service.notification.EmailService;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;

    public void validateRefundEligibility(Payment payment, BigDecimal amount) {
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw PaymentProcessingException.refundFailed("Cannot refund payment with status: " + payment.getStatus());
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw PaymentProcessingException.refundFailed("Refund amount must be positive");
        }

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw PaymentProcessingException.refundFailed(
                    String.format("Refund amount %s exceeds payment amount %s", amount, payment.getAmount()));
        }

        if (payment.getLiqpayPaymentId() == null || payment.getLiqpayPaymentId().isEmpty()) {
            throw PaymentProcessingException.refundFailed("Missing LiqPay payment ID for refund");
        }

        if (payment.getLiqpayOrderId() == null || payment.getLiqpayOrderId().isEmpty()) {
            throw PaymentProcessingException.refundFailed("Missing LiqPay order ID for refund");
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void callLiqPayRefund(String liqpayPaymentId, String liqpayOrderId, BigDecimal amount,
                                 String description) {
        var refundData = paymentGatewayService.prepareRefundData(liqpayPaymentId, liqpayOrderId, amount, description);

        paymentGatewayService.processRefund(refundData);

        log.info("Refund initiated for liqpayOrderId={}: amount={}, description={}", liqpayOrderId, amount,
                description);
    }

    @Transactional
    public void applyRefundSuccess(Payment payment, BigDecimal amount, String description, Ticket ticket) {
        var newStatus = amount.compareTo(payment.getAmount()) == 0 ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED;

        if (payment.getStatus() == newStatus) {
            log.debug("Payment {} already marked as {}, skipping", payment.getId(), newStatus);
            return;
        }

        var oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        sendRefundEmail(payment, amount, description, ticket);
        auditRefund(payment, oldStatus, newStatus, amount, description);
    }

    private void sendRefundEmail(Payment payment, BigDecimal amount, String description, Ticket ticket) {
        emailService.sendSafely("send refund email", payment.getId(), () -> {
            var booking = payment.getBooking();
            var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
            var seat = ticket.getSeatReservation().getSeat();
            var seatsInfo = String.format("Row %d, Seat %d", seat.getRow(), seat.getNumber());
            var bookingNumber = numberGenerator.generateBookingNumber(booking);

            emailService.sendRefundEmail(booking.getUser().getEmail(), bookingNumber,
                    booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
                    amount, seatsInfo, description);

            log.debug("Sent refund email to {}", booking.getUser().getEmail());
        });
    }

    private void auditRefund(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus, BigDecimal amount,
                             String description) {
        var oldDetails = AuditDetails.of().put("status", oldStatus).build();
        var newDetails = AuditDetails.of().put("status", newStatus).put("refundAmount", amount)
                .put("description", description).build();
        auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.REFUND,
                oldDetails, newDetails);
    }
}
