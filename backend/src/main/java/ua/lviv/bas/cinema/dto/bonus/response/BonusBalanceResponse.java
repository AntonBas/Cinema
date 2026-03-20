package ua.lviv.bas.cinema.dto.bonus.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record BonusBalanceResponse(
		@Schema(description = "Current points balance", example = "250") Integer pointsBalance,

		@Schema(description = "Conversion rate: 1 point = X UAH", example = "1.00") BigDecimal pointValue,

		@Schema(description = "Total monetary value of points balance", example = "250.00") BigDecimal balanceValue,

		@Schema(description = "Minimum points that can be used in one transaction", example = "100") Integer minUsablePoints,

		@Schema(description = "Maximum points that can be used in one transaction", example = "1000") Integer maxUsablePoints,

		@Schema(description = "Minimum monetary value for redemption", example = "100.00") BigDecimal minRedemptionValue,

		@Schema(description = "Maximum monetary value for redemption", example = "1000.00") BigDecimal maxRedemptionValue) {
}