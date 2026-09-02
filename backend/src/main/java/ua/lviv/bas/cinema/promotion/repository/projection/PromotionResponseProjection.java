package ua.lviv.bas.cinema.promotion.repository.projection;

import java.time.LocalDate;

public interface PromotionResponseProjection {
    Long getId();

    String getTitle();

    String getDescription();

    Integer getBonusPoints();

    LocalDate getStartDate();

    LocalDate getEndDate();
}