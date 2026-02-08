package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

public interface PromotionResponseProjection {
	Long getId();

	String getTitle();

	String getDescription();

	Integer getBonusPoints();

	LocalDate getStartDate();

	LocalDate getEndDate();
}