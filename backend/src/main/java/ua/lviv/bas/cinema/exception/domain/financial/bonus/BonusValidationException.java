package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;
import java.math.BigDecimal;

public class BonusValidationException extends ValidationException {

    @Serial
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

    public static BonusValidationException minPointsRequired(int minPoints) {
        return new BonusValidationException(
                String.format("Minimum %d points required for redemption", minPoints),
                "MIN_POINTS_REQUIRED");
    }

    public static BonusValidationException maxPointsExceeded(int maxPoints) {
        return new BonusValidationException(
                String.format("Maximum %d points allowed per transaction", maxPoints),
                "MAX_POINTS_EXCEEDED");
    }
}