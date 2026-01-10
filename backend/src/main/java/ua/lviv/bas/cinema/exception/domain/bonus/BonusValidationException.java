package ua.lviv.bas.cinema.exception.domain.bonus;

import java.math.BigDecimal;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class BonusValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public BonusValidationException(String message, String errorCode) {
		super(message, errorCode);
	}

	public static BonusValidationException invalidPoints(Integer points) {
		return new BonusValidationException(String.format("Points must be a positive number, got: %d", points),
				"INVALID_POINTS");
	}

	public static BonusValidationException discountExceedsMax(BigDecimal discount, BigDecimal max) {
		return new BonusValidationException(
				String.format("Bonus discount %.2f exceeds maximum allowed %.2f", discount, max),
				"DISCOUNT_EXCEEDS_MAX");
	}
}