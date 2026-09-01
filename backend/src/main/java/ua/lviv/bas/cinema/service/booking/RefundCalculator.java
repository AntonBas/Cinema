package ua.lviv.bas.cinema.service.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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
        return null;
    }

    public RefundCalculation calculate(Ticket ticket) {
        var sessionTime = ticket.getBooking().getSession().getStartTime();
        var percentage = refundRules.getRefundPercentage(sessionTime);
        var booking = ticket.getBooking();
        var totalSeats = booking.getSeatReservations().size();
        var cashAmount = calculateCashAmount(ticket);
        var refundAmount = calculateRefundAmount(cashAmount, percentage);
        var bonusPointsUsed = totalSeats > 0 ? booking.getBonusPointsUsed() / totalSeats : 0;
        var bonusPointsToRefund = calculateBonusRefund(bonusPointsUsed, percentage);
        return new RefundCalculation(percentage, cashAmount, refundAmount, bonusPointsUsed, bonusPointsToRefund);
    }

    public BigDecimal calculateCashAmount(Ticket ticket) {
        var paymentAmount = ticket.getPayment().getAmount();
        var totalBookingPrice = ticket.getBooking().getTotalPrice();
        if (totalBookingPrice.compareTo(BigDecimal.ZERO) > 0) {
            return ticket.getFinalPrice().multiply(paymentAmount)
                    .divide(totalBookingPrice, 2, RoundingMode.HALF_UP);
        }
        var totalSeats = ticket.getBooking().getSeatReservations().size();
        return totalSeats > 0
                ? paymentAmount.divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP)
                : paymentAmount;
    }

    public BigDecimal calculateRefundAmount(BigDecimal price, BigDecimal percentage) {
        return price.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Integer calculateBonusRefund(Integer bonusPointsUsed, BigDecimal percentage) {
        if (bonusPointsUsed == null || bonusPointsUsed == 0) {
            return 0;
        }
        return (int) (bonusPointsUsed * percentage.doubleValue() / 100);
    }

    public record RefundCalculation(BigDecimal percentage, BigDecimal cashAmount, BigDecimal refundAmount,
                                    Integer bonusPointsUsed, Integer bonusPointsToRefund) {
    }
}
