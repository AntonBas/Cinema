package ua.lviv.bas.cinema.refund.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class RefundCalculator {

    private final RefundRules refundRules;

    public String validate(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            return "Ticket is not active. Current status: " + ticket.getStatus();
        }
        var sessionTime = ticket.getBooking().getSession().getStartTime();
        if (!refundRules.isRefundable(sessionTime)) {
            return "Refund is not available for this session";
        }
        if (ticket.getRefund() != null) {
            return "Ticket has already been refunded";
        }
        if (sessionTime.isBefore(LocalDateTime.now())) {
            return "Session has already started or finished";
        }
        var paymentStatus = ticket.getPayment().getStatus();
        if (paymentStatus != PaymentStatus.SUCCESS && paymentStatus != PaymentStatus.PARTIALLY_REFUNDED) {
            return "Payment cannot be refunded via API. Contact support.";
        }
        return null;
    }

    public RefundCalculation calculate(Ticket ticket) {
        var sessionTime = ticket.getBooking().getSession().getStartTime();
        var percentage = refundRules.getRefundPercentage(sessionTime);
        var booking = ticket.getBooking();
        var totalSeats = booking.getSeatReservations().size();
        var cashAmount = calculateCashAmount(ticket);
        var refundAmount = calculateRefundAmount(cashAmount, percentage);
        var bonusPointsUsed = totalSeats > 0 ? distributeBonusPointsShare(ticket, booking, totalSeats) : 0;
        var bonusPointsToRefund = calculateBonusRefund(bonusPointsUsed, percentage);
        return new RefundCalculation(percentage, cashAmount, refundAmount, bonusPointsUsed, bonusPointsToRefund);
    }

    private int distributeBonusPointsShare(Ticket ticket, Booking booking, int totalSeats) {
        var totalBonusPointsUsed = booking.getBonusPointsUsed();
        var baseShare = totalBonusPointsUsed / totalSeats;
        var remainder = totalBonusPointsUsed % totalSeats;
        var orderedTickets = booking.getTickets().stream()
                .sorted(Comparator.comparing(Ticket::getId))
                .toList();
        var ticketIndex = orderedTickets.indexOf(ticket);
        return ticketIndex >= 0 && ticketIndex < remainder ? baseShare + 1 : baseShare;
    }

    public BigDecimal calculateCashAmount(Ticket ticket) {
        var paymentAmount = ticket.getPayment().getAmount();
        var totalBookingPrice = ticket.getBooking().getTotalPrice();
        if (totalBookingPrice.compareTo(BigDecimal.ZERO) > 0) {
            var lastTicket = lastTicketInBooking(ticket.getBooking());
            if (lastTicket != null && lastTicket.equals(ticket)) {
                return paymentAmount.subtract(sumOtherTicketsCashAmount(ticket, paymentAmount, totalBookingPrice));
            }
            return proportionalCashAmount(ticket, paymentAmount, totalBookingPrice);
        }
        var totalSeats = ticket.getBooking().getSeatReservations().size();
        return totalSeats > 0
                ? paymentAmount.divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP)
                : paymentAmount;
    }

    private Ticket lastTicketInBooking(Booking booking) {
        var tickets = booking.getTickets();
        if (tickets == null || tickets.isEmpty()) {
            return null;
        }
        return tickets.stream().max(Comparator.comparing(Ticket::getId)).orElse(null);
    }

    private BigDecimal sumOtherTicketsCashAmount(Ticket excluded, BigDecimal paymentAmount,
                                                 BigDecimal totalBookingPrice) {
        return excluded.getBooking().getTickets().stream().filter(t -> !t.equals(excluded))
                .map(t -> proportionalCashAmount(t, paymentAmount, totalBookingPrice))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal proportionalCashAmount(Ticket ticket, BigDecimal paymentAmount, BigDecimal totalBookingPrice) {
        return ticket.getFinalPrice().multiply(paymentAmount).divide(totalBookingPrice, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRefundAmount(BigDecimal price, BigDecimal percentage) {
        return price.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Integer calculateBonusRefund(Integer bonusPointsUsed, BigDecimal percentage) {
        if (bonusPointsUsed == null || bonusPointsUsed == 0) {
            return 0;
        }
        return BigDecimal.valueOf(bonusPointsUsed)
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .intValue();
    }

    public record RefundCalculation(BigDecimal percentage, BigDecimal cashAmount, BigDecimal refundAmount,
                                    Integer bonusPointsUsed, Integer bonusPointsToRefund) {

        public BigDecimal feeAmount() {
            return cashAmount.subtract(refundAmount);
        }

        public BigDecimal feePercentage() {
            return BigDecimal.valueOf(100).subtract(percentage);
        }
    }
}
