package ua.lviv.bas.cinema.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.audit.service.AuditDetails;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;

import java.util.List;

@Slf4j
@Service
public class LatePaymentRefundService {

    private static final String REFUND_DESCRIPTION = "Automatic refund: booking expired before payment completed";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentRefundService paymentRefundService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public LatePaymentRefundService(PaymentRepository paymentRepository, PaymentGatewayService paymentGatewayService,
            PaymentRefundService paymentRefundService, AuditService auditService, EmailService emailService,
            DateTimeFormatterService dateTimeFormatter, NumberGeneratorService numberGenerator,
            PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentRefundService = paymentRefundService;
        this.auditService = auditService;
        this.emailService = emailService;
        this.dateTimeFormatter = dateTimeFormatter;
        this.numberGenerator = numberGenerator;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void refund(Long paymentId) {
        var payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));
        if (payment.getStatus() != PaymentStatus.REFUND_REQUIRED) {
            return;
        }

        try {
            if (paymentGatewayService.checkRefundStatus(payment.getLiqpayOrderId(), payment.getAmount(),
                    payment.getAmount()) != RefundGatewayStatus.CONFIRMED) {
                paymentRefundService.callLiqPayRefund(payment.getLiqpayPaymentId(), payment.getLiqpayOrderId(),
                        payment.getAmount(), REFUND_DESCRIPTION);
            }
        } catch (RuntimeException e) {
            log.error("Automatic refund of payment {} failed, it stays REFUND_REQUIRED and will be retried",
                    paymentId, e);
            return;
        }

        boolean refunded = Boolean.TRUE.equals(requiresNewTransactionTemplate.execute(status -> paymentRepository
                .updateStatusIfCurrentIn(paymentId, List.of(PaymentStatus.REFUND_REQUIRED),
                        PaymentStatus.REFUNDED) == 1));
        if (!refunded) {
            return;
        }

        log.info("Payment {} automatically refunded in full ({} UAH)", paymentId, payment.getAmount());
        auditRefund(paymentId, payment);
        sendRefundEmail(payment);
    }

    private void sendRefundEmail(Payment payment) {
        var booking = payment.getBooking();
        emailService.sendSafely("send late payment refund email", booking.getId(),
                () -> emailService.sendLatePaymentRefundEmail(booking.getUser().getEmail(),
                        numberGenerator.generateBookingNumber(booking), booking.getSession().getMovie().getTitle(),
                        dateTimeFormatter.formatStandard(booking.getSession().getStartTime()), payment.getAmount()));
    }

    private void auditRefund(Long paymentId, Payment payment) {
        var oldDetails = AuditDetails.of().put("status", PaymentStatus.REFUND_REQUIRED).build();
        var newDetails = AuditDetails.of().put("status", PaymentStatus.REFUNDED)
                .put("refundAmount", payment.getAmount()).put("description", REFUND_DESCRIPTION).build();
        auditService.logChange("Payment", paymentId, "Payment #" + paymentId, AuditAction.REFUND, oldDetails,
                newDetails);
    }
}
