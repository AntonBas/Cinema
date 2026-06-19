package ua.lviv.bas.cinema.service.common;

import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;

import java.math.BigDecimal;

@Service
public class PriceCalculatorService {

    public BigDecimal calculateSeatPrice(Session session, Seat seat, TicketType ticketType) {
        var basePrice = session.getBasePrice();
        var seatMultiplier = seat.getSeatType().getPriceMultiplier();
        var ticketMultiplier = ticketType != null ? ticketType.getPriceMultiplier() : BigDecimal.ONE;
        return basePrice.multiply(seatMultiplier).multiply(ticketMultiplier);
    }

    public BigDecimal calculateBonusDiscount(Integer bonusPoints) {
        if (bonusPoints == null || bonusPoints == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("1.00").multiply(BigDecimal.valueOf(bonusPoints));
    }
}