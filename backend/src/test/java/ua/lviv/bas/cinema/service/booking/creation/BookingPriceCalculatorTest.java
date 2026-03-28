package ua.lviv.bas.cinema.service.booking.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
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

import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

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

		verify(bonusService, never()).validatePointsForBooking(any(), anyInt(), any());
	}

	@Test
	void calculateFinalPrice_WithBonusPoints() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 100;
		BigDecimal bonusDiscount = new BigDecimal("100.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));

		verify(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WhenBonusValidationFails_ShouldThrowException() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 100;

		doThrow(new IllegalArgumentException("Invalid bonus points")).when(bonusService)
				.validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

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

		verify(bonusService, never()).validatePointsForBooking(any(), anyInt(), any());
		verify(priceCalculator, never()).calculateBonusDiscount(anyInt());
	}

	@Test
	void calculateFinalPrice_BonusDiscountExceedsTotalPrice() {
		BigDecimal totalPrice = new BigDecimal("50.00");
		Integer bonusPointsToUse = 100;
		BigDecimal bonusDiscount = new BigDecimal("100.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

		verify(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WithNegativeTotalPrice() {
		BigDecimal totalPrice = new BigDecimal("-100.00");
		Integer bonusPointsToUse = null;

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(0);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.finalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

		verify(bonusService, never()).validatePointsForBooking(any(), anyInt(), any());
	}

	@Test
	void calculateFinalPrice_WithExactBonusDiscount() {
		BigDecimal totalPrice = new BigDecimal("100.00");
		Integer bonusPointsToUse = 100;
		BigDecimal bonusDiscount = new BigDecimal("100.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

		verify(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WithPartialBonusDiscount() {
		BigDecimal totalPrice = new BigDecimal("300.00");
		Integer bonusPointsToUse = 150;
		BigDecimal bonusDiscount = new BigDecimal("150.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(new BigDecimal("150.00"));

		verify(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WithVerySmallTotalPrice() {
		BigDecimal totalPrice = new BigDecimal("0.01");
		Integer bonusPointsToUse = 10;
		BigDecimal bonusDiscount = new BigDecimal("10.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, USER_ID);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

		verify(bonusService).validatePointsForBooking(USER_ID, bonusPointsToUse, totalPrice);
	}

	@Test
	void calculateFinalPrice_WithNullUserId() {
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer bonusPointsToUse = 100;
		BigDecimal bonusDiscount = new BigDecimal("100.00");

		when(priceCalculator.calculateBonusDiscount(bonusPointsToUse)).thenReturn(bonusDiscount);
		doNothing().when(bonusService).validatePointsForBooking(null, bonusPointsToUse, totalPrice);

		BookingPriceCalculator.BookingPriceResult result = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				bonusPointsToUse, null);

		assertThat(result.totalPrice()).isEqualByComparingTo(totalPrice);
		assertThat(result.bonusPointsUsed()).isEqualTo(bonusPointsToUse);
		assertThat(result.bonusDiscount()).isEqualByComparingTo(bonusDiscount);
		assertThat(result.finalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));

		verify(bonusService).validatePointsForBooking(null, bonusPointsToUse, totalPrice);
	}
}