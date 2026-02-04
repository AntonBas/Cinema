package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

public interface SessionAdminProjection {
	Long getId();

	String getTitle();

	LocalDateTime getStartTime();

	CinemaSessionStatus getStatus();

	BigDecimal getBasePrice();

	Long getHallId();

	String getHallName();

	Integer getHallCapacity();

	Integer getTicketsSold();

	BigDecimal getTotalRevenue();

	LocalDateTime getEndTime();
}