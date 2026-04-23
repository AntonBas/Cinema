package ua.lviv.bas.cinema.domain.cinema.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
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
}