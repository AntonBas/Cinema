package ua.lviv.bas.cinema.service.booking.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class BookingPriceCalculatorTest {

	@Mock
	private BonusService bonusService;

	@Mock
	private PriceCalculatorService priceCalculator;

	@InjectMocks
	private BookingPriceCalculator bookingPriceCalculator;

	private static final Long USER_ID = 1L;

	@BeforeEach
	void setUp() {
		bookingPriceCalculator = new BookingPriceCalculator(bonusService, priceCalculator);
	}

	@Test
	void calculateFinalPrice_WithoutBonusPoints() {
		BigDecimal totalPrice = new BigDecimal("200.00");

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice, null,
				USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(0);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.finalPrice()).isEqualByComparingTo(totalPrice);

		verify(bonusService, never()).validateBonusPointsForBooking(anyLong(), anyInt(), any(BigDecimal.class));
	}

	@Test
	void calculateFinalPrice_WithBonusPoints() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 100;
		BigDecimal bonusDiscount = new BigDecimal("100.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));

		verify(bonusService).validateBonusPointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WhenBonusValidationFails_ShouldThrowException() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 100;

		doThrow(new IllegalArgumentException("Invalid bonus points")).when(bonusService)
				.validateBonusPointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		assertThatThrownBy(() -> bookingPriceCalculator.calculateFinalPrice(totalPrice, bonusPointsToUse, USER_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid bonus points");
	}

	@Test
	void calculateFinalPrice_WithZeroBonusPoints() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 0;

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.bonusPointsUsed()).isEqualTo(0);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.finalPrice()).isEqualByComparingTo(totalPrice);
	}
}