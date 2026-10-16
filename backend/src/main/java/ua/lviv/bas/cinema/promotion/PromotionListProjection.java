package ua.lviv.bas.cinema.promotion;

import java.time.LocalDate;

public interface PromotionListProjection {
    Long getId();

    String getTitle();

    Integer getBonusPoints();

    LocalDate getStartDate();

    LocalDate getEndDate();

    boolean getActive();
}