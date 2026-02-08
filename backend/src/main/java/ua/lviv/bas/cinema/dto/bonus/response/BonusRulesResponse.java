package ua.lviv.bas.cinema.dto.bonus.response;

import java.math.BigDecimal;
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
@Schema(description = "Bonus rules configuration (admin only)")
public class BonusRulesResponse {

	@Schema(description = "Rule ID", example = "1")
	private Long id;

	@Schema(description = "Bonus type", example = "BOOKING_SPEND", allowableValues = { "WELCOME_BONUS",
			"BIRTHDAY_BONUS", "PROMOTION_BONUS", "BOOKING_SPEND", "PAYMENT_ACCRUAL", "REFUND_RETURN",
			"BOOKING_CANCEL" })
	private String bonusType;

	@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100", nullable = true)
	private Integer points;

	@Schema(description = "Points per currency unit (for PAYMENT_ACCRUAL)", example = "0.05", nullable = true)
	private BigDecimal moneyRatio;

	@Schema(description = "Minimum points that can be used in one transaction", example = "100")
	private Integer minPointsPerTransaction;

	@Schema(description = "Maximum points that can be used in one transaction", example = "1000")
	private Integer maxPointsPerTransaction;

	@Schema(description = "Whether this bonus rule is active", example = "true")
	private Boolean active;

	@Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
	private LocalDateTime updatedAt;
}