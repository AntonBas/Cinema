package ua.lviv.bas.cinema.promotion.domain;

import java.time.LocalDate;

public enum PromotionStatus {
    UPCOMING,
    ACTIVE,
    EXPIRED;

    public static PromotionStatus of(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        if (startDate != null && now.isBefore(startDate)) {
            return UPCOMING;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return EXPIRED;
        }
        return ACTIVE;
    }
}
