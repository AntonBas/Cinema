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

	@Schema(description = "Bonus type", example = "PURCHASE_WRITE_OFF", allowableValues = { "WELCOME_BONUS",
			"BIRTHDAY_BONUS", "PURCHASE_BONUS", "PURCHASE_WRITE_OFF" })
	private String bonusType;

	@Schema(description = "Fixed points amount (for WELCOME_BONUS, BIRTHDAY_BONUS)", example = "100", nullable = true)
	private Integer points;

	@Schema(description = "Points per currency unit (for PURCHASE_BONUS)", example = "0.1", nullable = true)
	private BigDecimal moneyRatio;

	@Schema(description = "Currency value per point (for PURCHASE_WRITE_OFF)", example = "1.00")
	private BigDecimal pointValue;

	@Schema(description = "Minimum points that can be used in one transaction", example = "50")
	private Integer minPointsPerTransaction;

	@Schema(description = "Maximum points that can be used in one transaction", example = "300")
	private Integer maxPointsPerTransaction;

	@Schema(description = "Whether this bonus rule is active", example = "true")
	private Boolean isActive;

	@Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
	private LocalDateTime updatedAt;
}