package ua.lviv.bas.cinema.domain.enums;

import java.math.BigDecimal;

public enum SeatType {
	STANDARD("Standard", BigDecimal.ONE), VIP("VIP", new BigDecimal("1.50")), COUPLE("Couple", new BigDecimal("1.80"));

	private final String displayName;
	private final BigDecimal priceMultiplier;

	SeatType(String displayName, BigDecimal priceMultiplier) {
		this.displayName = displayName;
		this.priceMultiplier = priceMultiplier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public BigDecimal getPriceMultiplier() {
		return priceMultiplier;
	}

	public boolean isPremium() {
		return this == VIP || this == COUPLE;
	}

	public boolean requiresSpecialHandling() {
		return this == COUPLE;
	}
}