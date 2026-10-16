package ua.lviv.bas.cinema.promotion;

import java.time.LocalDate;

public interface PromotionResponseProjection {
    Long getId();

    String getTitle();

    String getDescription();

    Integer getBonusPoints();

    LocalDate getStartDate();

    LocalDate getEndDate();

    boolean getActive();
}