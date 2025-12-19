package ua.lviv.bas.cinema.dto.bonus.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Request to update bonus rules (admin only)")
public class BonusRulesRequest {

	@Min(value = 0, message = "Points cannot be negative")
	@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100", nullable = true)
	private Integer points;

	@DecimalMin(value = "0.0", inclusive = false, message = "Money ratio must be positive")
	@Schema(description = "Points per currency unit (for PURCHASE_BONUS). Example: 0.1 = 1 point per 10 UAH", example = "0.1", nullable = true)
	private BigDecimal moneyRatio;

	@DecimalMin(value = "0.0", inclusive = false, message = "Point value must be positive")
	@Schema(description = "Currency value per point (for PURCHASE_WRITE_OFF). Example: 1.00 = 1 point = 1.00 UAH", example = "1.00", nullable = true)
	private BigDecimal pointValue;

	@Min(value = 1, message = "Minimum points must be at least 1")
	@Schema(description = "Minimum points that can be used in one transaction", example = "50", defaultValue = "50")
	private Integer minPointsPerTransaction = 50;

	@Min(value = 1, message = "Maximum points must be at least 1")
	@Schema(description = "Maximum points that can be used in one transaction", example = "300", defaultValue = "300")
	private Integer maxPointsPerTransaction = 300;

	@Schema(description = "Whether this bonus rule is active", example = "true")
	private Boolean isActive = true;
}