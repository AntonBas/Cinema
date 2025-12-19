package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Bonus transaction record")
public class BonusTransactionResponse {

	@Schema(description = "Transaction ID", example = "1")
	private Long id;

	@Schema(description = "Transaction type", example = "PURCHASE_BONUS", allowableValues = { "WELCOME_BONUS",
			"BIRTHDAY_BONUS", "PURCHASE_BONUS", "PURCHASE_WRITE_OFF" })
	private String type;

	@Schema(description = "Points change (positive for credit, negative for debit)", example = "25")
	private Integer pointsChange;

	@Schema(description = "Reference ID (e.g., PAYMENT_123, USER_456)", example = "PAYMENT_42", nullable = true)
	private String referenceId;

	@Schema(description = "Transaction creation timestamp", example = "2025-01-17T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "New balance after this transaction", example = "125")
	private Integer newBalance;
}
