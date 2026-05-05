package ua.lviv.bas.cinema.dto.bonus.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;

import java.time.LocalDateTime;

@Schema(description = "Bonus points transaction history entry")
public record BonusTransactionResponse(
        @Schema(description = "Transaction ID", example = "1")
        Long id,

        @Schema(description = "Transaction type", example = "PAYMENT_ACCRUAL")
        BonusTransactionType type,

        @Schema(description = "Points change with sign", example = "+150")
        String pointsChange,

        @Schema(description = "Transaction creation timestamp", example = "2025-01-17T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Balance after this transaction", example = "350")
        Integer newBalance
) {
}