package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

public interface SessionAdminProjection {
	Long getId();

	LocalDateTime getStartTime();

	LocalDateTime getEndTime();

	BigDecimal getBasePrice();

	CinemaSessionStatus getStatus();

	Long getMovieId();

	String getMovieTitle();

	Integer getMovieDuration();

	Long getHallId();

	String getHallName();

	Integer getHallCapacity();

	Integer getTicketsSold();

	BigDecimal getTotalRevenue();
}