package ua.lviv.bas.cinema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "bonus")
@Data
public class BonusProperties {
	private Integer welcomePoints = 100;
	private Integer birthdayPoints = 150;
	private Double purchasePercent = 5.0;
	private Double minPurchase = 100.00;

	public BigDecimal getPurchasePercentAsDecimal() {
		return BigDecimal.valueOf(purchasePercent);
	}

	public BigDecimal getMinPurchaseAsDecimal() {
		return BigDecimal.valueOf(minPurchase);
	}

	public BigDecimal getPurchasePercentDivided() {
		return getPurchasePercentAsDecimal().divide(BigDecimal.valueOf(100));
	}
}