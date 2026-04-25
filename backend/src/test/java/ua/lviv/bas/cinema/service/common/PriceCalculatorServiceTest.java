package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.ticket.TicketType;

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
		ReflectionTestUtils.setField(priceCalculatorService, "maxBonusPointsPercentage", 70);
	}

	@Test
	void calculateSeatPrice_WithAllMultipliers_ShouldReturnCorrectPrice() {
		when(session.getBasePrice()).thenReturn(new BigDecimal("100.00"));
		when(seat.getSeatType()).thenReturn(seatType);
		when(seatType.getPriceMultiplier()).thenReturn(new BigDecimal("1.5"));
		when(ticketType.getPriceMultiplier()).thenReturn(new BigDecimal("0.8"));

		BigDecimal result = priceCalculatorService.calculateSeatPrice(session, seat, ticketType);

		assertThat(result).isEqualByComparingTo("120.00");
	}

	@Test
	void calculateSeatPrice_WithoutTicketType_ShouldUseDefaultMultiplier() {
		when(session.getBasePrice()).thenReturn(new BigDecimal("100.00"));
		when(seat.getSeatType()).thenReturn(seatType);
		when(seatType.getPriceMultiplier()).thenReturn(new BigDecimal("1.5"));

		BigDecimal result = priceCalculatorService.calculateSeatPrice(session, seat, null);

		assertThat(result).isEqualByComparingTo("150.00");
	}

	@Test
	void calculateBonusDiscount_WhenBonusPointsNotNull_ShouldReturnCorrectDiscount() {
		BigDecimal result = priceCalculatorService.calculateBonusDiscount(10);

		assertThat(result).isEqualByComparingTo("10.00");
	}

	@Test
	void calculateBonusDiscount_WhenBonusPointsZero_ShouldReturnZero() {
		BigDecimal result = priceCalculatorService.calculateBonusDiscount(0);

		assertThat(result).isEqualByComparingTo("0.00");
	}

	@Test
	void calculateBonusDiscount_WhenBonusPointsNull_ShouldReturnZero() {
		BigDecimal result = priceCalculatorService.calculateBonusDiscount(null);

		assertThat(result).isEqualByComparingTo("0.00");
	}

	@Test
	void calculateMaximumBonusDiscount_ShouldReturnCorrectPercentage() {
		BigDecimal totalPrice = new BigDecimal("1000.00");
		BigDecimal result = priceCalculatorService.calculateMaximumBonusDiscount(totalPrice);

		assertThat(result).isEqualByComparingTo("700.00");
	}
}