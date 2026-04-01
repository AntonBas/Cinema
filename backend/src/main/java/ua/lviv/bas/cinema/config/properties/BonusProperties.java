package ua.lviv.bas.cinema.config.properties;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;

@Data
@Component
@ConfigurationProperties(prefix = "bonus")
public class BonusProperties {

	private BigDecimal pointValue = new BigDecimal("1.00");
	private BigDecimal maxDiscountPercentage = new BigDecimal("0.5");
	private Map<BonusTransactionType, RuleDefaults> defaults;

	@Data
	public static class RuleDefaults {
		private Integer points;
		private BigDecimal moneyRatio;
		private Integer minPoints;
		private Integer maxPoints;

		public boolean isEmpty() {
			return points == null && moneyRatio == null && minPoints == null && maxPoints == null;
		}
	}

	public RuleDefaults getDefaultsForType(BonusTransactionType type) {
		return defaults != null ? defaults.get(type) : null;
	}

	public boolean hasDefaultsForType(BonusTransactionType type) {
		return defaults != null && defaults.containsKey(type);
	}
}