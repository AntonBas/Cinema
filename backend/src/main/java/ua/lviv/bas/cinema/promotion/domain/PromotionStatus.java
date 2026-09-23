package ua.lviv.bas.cinema.promotion.domain;

import ua.lviv.bas.cinema.common.CinemaTime;
import java.time.LocalDate;

public enum PromotionStatus {
    UPCOMING,
    ACTIVE,
    EXPIRED;

    public static PromotionStatus of(LocalDate startDate, LocalDate endDate) {
        LocalDate now = CinemaTime.today();
        if (startDate != null && now.isBefore(startDate)) {
            return UPCOMING;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return EXPIRED;
        }
        return ACTIVE;
    }
}
