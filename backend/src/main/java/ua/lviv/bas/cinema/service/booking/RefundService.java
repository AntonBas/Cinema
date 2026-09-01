package ua.lviv.bas.cinema.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.RefundProcessingException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.booking.RefundMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.common.NumberGeneratorService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private static final int MAX_APPLY_SUCCESS_ATTEMPTS = 2;

    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;
    private final RefundCalculator refundCalculator;
    private final RefundTransactionExecutor refundTransactionExecutor;
    private final RefundRules refundRules;
    private final RefundMapper refundMapper;
    private final RefundItemMapper refundItemMapper;
    private final NumberGeneratorService numberGenerator;

    @Transactional(readOnly = true)
    public RefundPreviewResponse getPreview(RefundPreviewRequest request, Long userId) {
        var ticket = findActiveTicket(request.ticketId(), userId);
        var validationError = refundCalculator.validate(ticket);

        if (validationError != null) {
            return createNonRefundablePreview(ticket, validationError);
        }
        if (ticket.getPayment().getStatus() != PaymentStatus.SUCCESS && ticket.getPayment().getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            return createNonRefundablePreview(ticket, "Payment cannot be refunded via API. Contact support.");
        }
        return createPreview(ticket);
    }

    public RefundResponse refund(RefundRequest request, Long userId) {
        var context = refundTransactionExecutor.markProcessing(request.ticketId(), userId, request.reason());
        var description = "Refund for ticket #" + context.ticketUniqueCode();

        try {
            paymentService.callLiqPayRefund(context.liqpayPaymentId(), context.liqpayOrderId(),
                    context.refundAmount(), description);
        } catch (PaymentProcessingException e) {
            refundTransactionExecutor.markFailed(context.refundId(), e);
            throw new RefundProcessingException("Refund was rejected by the payment provider", e);
        } catch (Exception e) {
            log.error("LiqPay refund outcome could not be confirmed for refund {} (ticket {}): {}",
                    context.refundId(), context.ticketId(), e.getMessage(), e);
            throw new RefundProcessingException(
                    "Refund could not be confirmed and is pending review, please contact support", e);
        }

        var refund = applySuccessWithRetry(context.refundId(), context.ticketId());
        return buildResponse(refund);
    }

    private Refund applySuccessWithRetry(Long refundId, Long ticketId) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_APPLY_SUCCESS_ATTEMPTS; attempt++) {
            try {
                return refundTransactionExecutor.applySuccess(refundId, ticketId);
            } catch (RuntimeException e) {
                lastError = e;
                log.error("Attempt {}/{} to finalize refund {} failed", attempt, MAX_APPLY_SUCCESS_ATTEMPTS,
                        refundId, e);
            }
        }
        log.error("Refund {} left in PROCESSING after {} failed attempts; RefundScheduler will retry", refundId,
                MAX_APPLY_SUCCESS_ATTEMPTS);
        throw new RefundProcessingException("Refund is still being finalized, please check back shortly", lastError);
    }

    private String formatRemainingTime(LocalDateTime sessionTime) {
        var hours = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);
        var minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), sessionTime) % 60;

        if (hours > 0 && minutes > 0)
            return String.format("%d hours %d minutes", hours, minutes);
        if (hours > 0)
            return String.format("%d hours", hours);
        if (minutes > 0)
            return String.format("%d minutes", minutes);
        return "Less than a minute";
    }

    private RefundPreviewResponse createPreview(Ticket ticket) {
        var sessionTime = ticket.getBooking().getSession().getStartTime();
        var booking = ticket.getBooking();
        var calculation = refundCalculator.calculate(ticket);
        var feeAmount = calculation.cashAmount().subtract(calculation.refundAmount());

        String seatInfo = "N/A";
        var seatReservations = booking.getSeatReservations();
        if (!seatReservations.isEmpty()) {
            var bookedSeat = seatReservations.getFirst();
            seatInfo = String.format("Row %d, Seat %d", bookedSeat.getSeat().getRow(),
                    bookedSeat.getSeat().getNumber());
        }

        return new RefundPreviewResponse(ticket.getId(), ticket.getUniqueCode(),
                booking.getSession().getMovie().getTitle(), sessionTime,
                booking.getSession().getHall().getName(), seatInfo, ticket.getOriginalPrice(),
                ticket.getFinalPrice(), calculation.refundAmount(), calculation.percentage(), feeAmount,
                BigDecimal.valueOf(100).subtract(calculation.percentage()), calculation.bonusPointsUsed(),
                calculation.bonusPointsToRefund(), refundRules.getPolicyName(sessionTime),
                refundRules.getPolicyDescription(sessionTime), true, null, sessionTime.minusMinutes(30),
                formatRemainingTime(sessionTime), ticket.getPurchaseTime().toString(),
                ticket.getTicketType().getDisplayName());
    }

    private Ticket findActiveTicket(Long ticketId, Long userId) {
        return ticketRepository.findByIdAndUserIdAndStatus(ticketId, userId, TicketStatus.ACTIVE).orElseThrow(
                () -> new TicketNotFoundException("Ticket not found or not active. Ticket ID: " + ticketId));
    }

    private RefundPreviewResponse createNonRefundablePreview(Ticket ticket, String reason) {
        return new RefundPreviewResponse(ticket.getId(), ticket.getUniqueCode(),
                ticket.getBooking().getSession().getMovie().getTitle(), ticket.getBooking().getSession().getStartTime(),
                null, null, null, null, null, null, null, null, null, null, null, null, false, reason, null, null, null,
                null);
    }

    private RefundResponse buildResponse(Refund refund) {
        var response = refundMapper.toResponse(refund);
        return new RefundResponse(response.id(), numberGenerator.generateRefundNumber(refund), response.status(),
                response.totalAmount(), response.totalBonusPointsToDeduct(), response.reason(), response.processedBy(),
                response.processedAt(), response.createdAt(), response.paymentId(), "CARD",
                refund.getItems() != null ? refund.getItems().stream().map(refundItemMapper::toResponse).toList()
                        : null,
                "Refund processed successfully", "3-5 business days");
    }
}
