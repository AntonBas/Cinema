package ua.lviv.bas.cinema.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.ticket.TicketType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceCalculatorServiceTest {

    @Mock
    private Session session;

    @Mock
    private Seat seat;

    @Mock
    private SeatType seatType;

    @Mock
    private TicketType ticketType;

    private PriceCalculatorService priceCalculatorService;

    @BeforeEach
    void setUp() {
        priceCalculatorService = new PriceCalculatorService();
    }

    @Test
    void calculateSeatPriceWithAllMultipliersShouldReturnCorrectPrice() {
        when(session.getBasePrice()).thenReturn(new BigDecimal("100.00"));
        when(seat.getSeatType()).thenReturn(seatType);
        when(seatType.getPriceMultiplier()).thenReturn(new BigDecimal("1.5"));
        when(ticketType.getPriceMultiplier()).thenReturn(new BigDecimal("0.8"));

        BigDecimal result = priceCalculatorService.calculateSeatPrice(session, seat, ticketType);

        assertThat(result).isEqualByComparingTo("120.00");
    }

    @Test
    void calculateSeatPriceWithoutTicketTypeShouldUseDefaultMultiplier() {
        when(session.getBasePrice()).thenReturn(new BigDecimal("100.00"));
        when(seat.getSeatType()).thenReturn(seatType);
        when(seatType.getPriceMultiplier()).thenReturn(new BigDecimal("1.5"));

        BigDecimal result = priceCalculatorService.calculateSeatPrice(session, seat, null);

        assertThat(result).isEqualByComparingTo("150.00");
    }

    @Test
    void calculateBonusDiscountWhenBonusPointsNotNullShouldReturnCorrectDiscount() {
        BigDecimal result = priceCalculatorService.calculateBonusDiscount(10);

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void calculateBonusDiscountWhenBonusPointsZeroShouldReturnZero() {
        BigDecimal result = priceCalculatorService.calculateBonusDiscount(0);

        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void calculateBonusDiscountWhenBonusPointsNullShouldReturnZero() {
        BigDecimal result = priceCalculatorService.calculateBonusDiscount(null);

        assertThat(result).isEqualByComparingTo("0.00");
    }
}