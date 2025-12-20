package ua.lviv.bas.cinema.dto.bonus.response;

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
@Schema(description = "User's bonus points balance with limits")
public class BonusBalanceResponse {

	@Schema(description = "Current points balance", example = "250")
	private Integer pointsBalance;

	@Schema(description = "Conversion rate: 1 point = X UAH", example = "1.00")
	private BigDecimal pointValue;

	@Schema(description = "Total monetary value of points balance", example = "250.00")
	private BigDecimal balanceValue;

	@Schema(description = "Minimum points that can be used in one transaction", example = "50")
	private Integer minUsablePoints;

	@Schema(description = "Maximum points that can be used in one transaction", example = "300")
	private Integer maxUsablePoints;

	@Schema(description = "Minimum monetary value for redemption", example = "50.00")
	private BigDecimal minRedemptionValue;

	@Schema(description = "Maximum monetary value for redemption", example = "300.00")
	private BigDecimal maxRedemptionValue;
}