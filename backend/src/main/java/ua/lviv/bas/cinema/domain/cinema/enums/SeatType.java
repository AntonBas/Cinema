package ua.lviv.bas.cinema.domain.cinema.enums;

import java.math.BigDecimal;

public enum SeatType {
	STANDARD("Standard", BigDecimal.ONE, 1), VIP("VIP", new BigDecimal("1.50"), 1),
	COUPLE("Couple", new BigDecimal("1.80"), 2);

	private final String displayName;
	private final BigDecimal priceMultiplier;
	private final int seatsCount;

	SeatType(String displayName, BigDecimal priceMultiplier, int seatsCount) {
		this.displayName = displayName;
		this.priceMultiplier = priceMultiplier;
		this.seatsCount = seatsCount;
	}

	public String getDisplayName() {
		return displayName;
	}

	public BigDecimal getPriceMultiplier() {
		return priceMultiplier;
	}

	public int getSeatsCount() {
		return seatsCount;
	}

	public boolean isPremium() {
		return this == VIP || this == COUPLE;
	}

	public boolean requiresSpecialHandling() {
		return this == COUPLE;
	}
}