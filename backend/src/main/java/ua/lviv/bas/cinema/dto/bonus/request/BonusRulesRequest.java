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

	@Schema(description = "Points per currency unit (for PURCHASE_BONUS). Example: 0.1 = 1 point per 10 UAH", example = "0.1", nullable = true)
	private BigDecimal moneyRatio;

	@Schema(description = "Currency value per point (for PURCHASE_WRITE_OFF). Example: 1.00 = 1 point = 1.00 UAH", example = "1.00", nullable = true)
	private BigDecimal pointValue;

	@Schema(description = "Minimum points that can be used in one transaction", example = "50", nullable = true)
	private Integer minPointsPerTransaction;

	@Schema(description = "Maximum points that can be used in one transaction", example = "300", nullable = true)
	private Integer maxPointsPerTransaction;

	@Schema(description = "Whether this bonus rule is active", example = "true", nullable = true)
	private Boolean active;
}