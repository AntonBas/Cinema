package ua.lviv.bas.cinema.dto.bonus.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;

import java.math.BigDecimal;

@Schema(description = "Bonus rule configuration")
public record BonusRulesResponse(
        @Schema(description = "Rule ID", example = "1")
        Long id,

        @Schema(description = "Bonus type", example = "BOOKING_SPEND")
        BonusTransactionType bonusType,

        @Schema(description = "Fixed points amount", example = "100")
        Integer points,

        @Schema(description = "Points per currency unit", example = "0.05")
        BigDecimal moneyRatio,

        @Schema(description = "Minimum points per transaction", example = "100")
        Integer minPointsPerTransaction,

        @Schema(description = "Maximum points per transaction", example = "1000")
        Integer maxPointsPerTransaction,

        @Schema(description = "Whether this bonus rule is active", example = "true")
        Boolean active
) {
}