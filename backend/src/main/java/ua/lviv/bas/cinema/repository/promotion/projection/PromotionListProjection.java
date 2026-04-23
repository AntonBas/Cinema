package ua.lviv.bas.cinema.repository.promotion.projection;

import java.time.LocalDate;

public interface PromotionListProjection {
    Long getId();

    String getTitle();

    Integer getBonusPoints();

    LocalDate getStartDate();

    LocalDate getEndDate();
}