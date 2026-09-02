package ua.lviv.bas.cinema.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.payment.service.PaymentRefundService;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.ticket.service.TicketService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundTransactionExecutor {

    private final TicketRepository ticketRepository;
    private final RefundRepository refundRepository;
    private final PaymentRefundService paymentRefundService;
    private final BonusLedgerService bonusLedgerService;
    private final TicketService ticketService;
    private final RefundCalculator refundCalculator;
    private final AuditService auditService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefundProcessingContext createProcessingRefund(Long ticketId, Long userId, String reason) {
        var ticket = ticketService.findActiveTicketForUser(ticketId, userId);

        validateRefundable(ticket);

        var calculation = refundCalculator.calculate(ticket);
        paymentRefundService.validateRefundEligibility(ticket.getPayment(), calculation.refundAmount());

        var refund = buildRefund(ticket, calculation, reason);
        var saved = refundRepository.save(refund);

        auditCreated(saved, ticket, calculation);

        return new RefundProcessingContext(saved.getId(), ticket.getId(), ticket.getUniqueCode(),
                ticket.getPayment().getLiqpayPaymentId(), ticket.getPayment().getLiqpayOrderId(),
                calculation.refundAmount());
    }

    private void validateRefundable(Ticket ticket) {
        var validationError = refundCalculator.validate(ticket);
        if (validationError != null) {
            throw new TicketNotRefundableException(validationError);
        }
        if (ticket.getPayment().getStatus() != PaymentStatus.SUCCESS
                && ticket.getPayment().getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new TicketNotRefundableException("Payment cannot be refunded via API. Contact support.");
        }
        if (refundRepository.existsByItemsTicketIdAndStatus(ticket.getId(), RefundStatus.PROCESSING)) {
            throw new TicketNotRefundableException("A refund for this ticket is already being processed");
        }
    }

    private Refund buildRefund(Ticket ticket, RefundCalculator.RefundCalculation calculation, String reason) {
        var refund = Refund.builder().payment(ticket.getPayment()).user(ticket.getUser())
                .totalAmount(calculation.refundAmount()).totalBonusPointsToDeduct(calculation.bonusPointsToRefund())
                .reason(reason).status(RefundStatus.PROCESSING).build();

        var refundItem = RefundItem.builder().refund(refund).ticket(ticket).ticketPrice(ticket.getFinalPrice())
                .refundPercentage(calculation.percentage().setScale(2, RoundingMode.HALF_UP))
                .refundAmount(calculation.refundAmount()).bonusPointsToDeduct(calculation.bonusPointsToRefund())
                .status(RefundItemStatus.PENDING).build();

        refund.getItems().add(refundItem);
        return refund;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Refund applySuccess(Long refundId, Long ticketId) {
        var refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Refund", refundId));

        if (refund.getStatus() == RefundStatus.PROCESSED) {
            log.debug("Refund {} already processed, skipping", refundId);
            return refund;
        }

        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found. Ticket ID: " + ticketId));

        var refundItem = refund.getItems().getFirst();
        var amount = refund.getTotalAmount();
        var percentage = refundItem.getRefundPercentage();
        var bonusPointsToRefund = refund.getTotalBonusPointsToDeduct();
        var description = "Refund for ticket #" + ticket.getUniqueCode();

        paymentRefundService.applyRefundSuccess(refund.getPayment(), amount, description, ticket);

        if (bonusPointsToRefund != null && bonusPointsToRefund > 0) {
            bonusLedgerService.refundPointsForTicket(refund.getUser().getId(), bonusPointsToRefund,
                    "REFUND_TICKET_" + ticket.getId());
        }

        ticketService.markAsRefunded(ticket, refund);

        refund.setStatus(RefundStatus.PROCESSED);
        refundRepository.save(refund);

        auditProcessed(refund, ticket, amount, percentage, bonusPointsToRefund);
        return refund;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long refundId, Exception cause) {
        var refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Refund", refundId));
        refund.setStatus(RefundStatus.REJECTED);
        refundRepository.save(refund);
        auditRejected(refund, cause);
    }

    private void auditCreated(Refund refund, Ticket ticket, RefundCalculator.RefundCalculation calculation) {
        Map<String, Object> details = new HashMap<>();
        details.put("ticketId", ticket.getId());
        details.put("refundAmount", calculation.refundAmount());
        details.put("percentage", calculation.percentage());
        details.put("bonusPointsToRefund", calculation.bonusPointsToRefund());
        auditService.logChange("Refund", refund.getId(), "Refund #" + refund.getId(), AuditAction.CREATED, null,
                details);
    }

    private void auditProcessed(Refund refund, Ticket ticket, BigDecimal refundAmount, BigDecimal percentage,
                                Integer bonusPointsToRefund) {
        Map<String, Object> details = new HashMap<>();
        details.put("ticketId", ticket.getId());
        details.put("refundAmount", refundAmount);
        details.put("percentage", percentage);
        details.put("bonusPointsToRefund", bonusPointsToRefund);
        auditService.logChange("Refund", refund.getId(), "Refund #" + refund.getId(), AuditAction.SUCCESS, null,
                details);
    }

    private void auditRejected(Refund refund, Exception e) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", e.getMessage());
        auditService.logChange("Refund", refund.getId(), "Refund #" + refund.getId(), AuditAction.REJECTED, null,
                errorDetails);
    }

    public record RefundProcessingContext(Long refundId, Long ticketId, String ticketUniqueCode,
                                          String liqpayPaymentId, String liqpayOrderId, BigDecimal refundAmount) {
    }
}
