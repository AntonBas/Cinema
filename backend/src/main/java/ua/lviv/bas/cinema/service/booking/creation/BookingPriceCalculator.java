package ua.lviv.bas.cinema.service.booking.creation;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@Service
@RequiredArgsConstructor
public class BookingPriceCalculator {
	private final BonusService bonusService;
	private final PriceCalculatorService priceCalculator;

	@Value("${booking.max-bonus-points-percentage:30}")
	private int maxBonusPointsPercentage;

	public record BookingPriceResult(BigDecimal totalPrice, Integer bonusPointsUsed, BigDecimal bonusDiscount,
			BigDecimal finalPrice) {
	}

	public BookingPriceResult calculateFinalPrice(BigDecimal totalPrice, Integer bonusPointsToUse, Long userId) {
		BigDecimal bonusDiscount = BigDecimal.ZERO;
		Integer bonusPointsUsed = 0;

		if (bonusPointsToUse != null && bonusPointsToUse > 0) {
			bonusService.validateBonusPointsForBooking(userId, bonusPointsToUse, totalPrice);
			bonusDiscount = priceCalculator.calculateBonusDiscount(bonusPointsToUse);
			bonusPointsUsed = bonusPointsToUse;
		}

		BigDecimal finalPrice = totalPrice.subtract(bonusDiscount).max(BigDecimal.ZERO);

		return new BookingPriceResult(totalPrice, bonusPointsUsed, bonusDiscount, finalPrice);
	}
}