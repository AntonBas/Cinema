package ua.lviv.bas.cinema.promotion.repository.projection;

import java.time.LocalDate;

public interface PromotionListProjection {
    Long getId();

    String getTitle();

    Integer getBonusPoints();

    LocalDate getStartDate();

    LocalDate getEndDate();
}