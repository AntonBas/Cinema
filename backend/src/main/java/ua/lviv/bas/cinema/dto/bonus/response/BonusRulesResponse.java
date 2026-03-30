package ua.lviv.bas.cinema.dto.bonus.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record BonusRulesResponse(@Schema(description = "Rule ID", example = "1") Long id,

		@Schema(description = "Bonus type", example = "BOOKING_SPEND", allowableValues = {
				"WELCOME_BONUS", "BIRTHDAY_BONUS", "PROMOTION_BONUS", "BOOKING_SPEND", "PAYMENT_ACCRUAL",
				"REFUND_RETURN", "BOOKING_CANCEL" }) String bonusType,

		@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100") Integer points,

		@Schema(description = "Points per currency unit (for PAYMENT_ACCRUAL)", example = "0.05") BigDecimal moneyRatio,

		@Schema(description = "Minimum points that can be used in one transaction", example = "100") Integer minPointsPerTransaction,

		@Schema(description = "Maximum points that can be used in one transaction", example = "1000") Integer maxPointsPerTransaction,

		@Schema(description = "Whether this bonus rule is active", example = "true") Boolean active) {
}