package ua.lviv.bas.cinema.dto.bonus.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update bonus rules (admin only)")
public class BonusRulesRequest {

	@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100", nullable = true)
	private Integer points;

	@Schema(description = "Points per currency unit (for PAYMENT_ACCRUAL). Example: 0.05 = 5 points per 100 UAH", example = "0.05", nullable = true)
	private BigDecimal moneyRatio;

	@Schema(description = "Minimum points that can be used in one transaction", example = "100", nullable = true)
	private Integer minPointsPerTransaction;

	@Schema(description = "Maximum points that can be used in one transaction", example = "1000", nullable = true)
	private Integer maxPointsPerTransaction;

	@Schema(description = "Whether this bonus rule is active", example = "true", nullable = true)
	private Boolean active;
}