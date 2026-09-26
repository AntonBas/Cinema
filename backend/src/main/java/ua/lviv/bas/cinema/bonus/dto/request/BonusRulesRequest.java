package ua.lviv.bas.cinema.bonus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Full configuration of a bonus rule; fields not used by the rule type must be null")
public record BonusRulesRequest(
        @Min(value = 0, message = "Points must be at least 0")
        @Max(value = 1_000_000, message = "Points must be at most 1,000,000")
        @Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100")
        Integer points,

        @DecimalMin(value = "0.0", message = "Money ratio must be at least 0")
        @DecimalMax(value = "10.0", message = "Money ratio must be at most 10")
        @Schema(description = "Points per currency unit (for PAYMENT_ACCRUAL)", example = "0.05")
        BigDecimal moneyRatio,

        @Min(value = 0, message = "Minimum points per transaction must be at least 0")
        @Max(value = 1_000_000, message = "Minimum points per transaction must be at most 1,000,000")
        @Schema(description = "BOOKING_SPEND: minimum points redeemable per booking; PAYMENT_ACCRUAL: minimum points accrued per payment", example = "100")
        Integer minPointsPerTransaction,

        @Min(value = 0, message = "Maximum points per transaction must be at least 0")
        @Max(value = 1_000_000, message = "Maximum points per transaction must be at most 1,000,000")
        @Schema(description = "BOOKING_SPEND: maximum points redeemable per booking; PAYMENT_ACCRUAL: maximum points accrued per payment", example = "1000")
        Integer maxPointsPerTransaction,

        @NotNull(message = "Active flag is required")
        @Schema(description = "Whether this bonus rule is active", example = "true")
        Boolean active
) {
}