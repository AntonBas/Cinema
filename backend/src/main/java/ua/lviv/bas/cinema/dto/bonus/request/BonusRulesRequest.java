package ua.lviv.bas.cinema.dto.bonus.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record BonusRulesRequest(
		@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100") Integer points,

		@Schema(description = "Points per currency unit (for PAYMENT_ACCRUAL)", example = "0.05") BigDecimal moneyRatio,

		@Schema(description = "Minimum points that can be used in one transaction", example = "100") Integer minPointsPerTransaction,

		@Schema(description = "Maximum points that can be used in one transaction", example = "1000") Integer maxPointsPerTransaction,

		@Schema(description = "Whether this bonus rule is active", example = "true") Boolean active) {
}