package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bonus transaction information")
public class BonusTransactionResponse {

	@Schema(description = "Transaction ID", example = "1")
	private Long id;

	@Schema(description = "Transaction type", example = "PAYMENT_ACCRUAL")
	private String type;

	@Schema(description = "Points change with sign (+ for credit, - for debit)", example = "+150")
	private String pointsChange;

	@Schema(description = "Transaction creation timestamp", example = "2025-01-17T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Balance after this transaction", example = "350")
	private Integer newBalance;
}