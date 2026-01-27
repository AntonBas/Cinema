package ua.lviv.bas.cinema.service.booking.creation;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingPriceDetails {
	private final BigDecimal totalPrice;
	private final Integer bonusPointsUsed;
	private final BigDecimal bonusDiscount;
	private final BigDecimal finalPrice;

	@Override
	public String toString() {
		return "BookingPriceDetails{" + "totalPrice=" + totalPrice + ", bonusPointsUsed=" + bonusPointsUsed
				+ ", bonusDiscount=" + bonusDiscount + ", finalPrice=" + finalPrice + '}';
	}

	public boolean hasBonusDiscount() {
		return bonusPointsUsed != null && bonusPointsUsed > 0;
	}

	public BigDecimal getDiscountPercentage() {
		if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		return bonusDiscount.multiply(BigDecimal.valueOf(100)).divide(totalPrice, 2, java.math.RoundingMode.HALF_UP);
	}
}