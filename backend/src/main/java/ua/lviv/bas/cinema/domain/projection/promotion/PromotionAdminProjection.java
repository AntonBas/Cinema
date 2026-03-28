package ua.lviv.bas.cinema.domain.projection.promotion;

import java.time.LocalDate;

public interface PromotionAdminProjection {
	Long getId();

	String getTitle();

	Integer getBonusPoints();

	LocalDate getStartDate();

	LocalDate getEndDate();
}