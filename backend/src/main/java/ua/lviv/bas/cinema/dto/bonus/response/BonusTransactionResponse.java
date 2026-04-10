package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;

public record BonusTransactionResponse(@Schema(description = "Transaction ID", example = "1") Long id,
		@Schema(description = "Transaction type", example = "PAYMENT_ACCRUAL") BonusTransactionType type,
		@Schema(description = "Points change with sign", example = "+150") String pointsChange,
		@Schema(description = "Transaction creation timestamp", example = "2025-01-17T14:30:00") LocalDateTime createdAt,
		@Schema(description = "Balance after this transaction", example = "350") Integer newBalance) {
}