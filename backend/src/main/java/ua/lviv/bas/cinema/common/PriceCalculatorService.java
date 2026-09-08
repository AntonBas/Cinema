package ua.lviv.bas.cinema.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.ticket.domain.TicketType;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PriceCalculatorService {

    private final BonusProperties bonusProperties;

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
        return bonusProperties.getPointValue().multiply(BigDecimal.valueOf(bonusPoints));
    }
}