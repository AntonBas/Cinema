package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SessionScheduleProjection {
	Long getId();

	String getMovieTitle();

	String getMoviePosterUrl();

	Integer getMovieDuration();

	LocalDateTime getStartTime();

	BigDecimal getBasePrice();

	Long getHallId();

	String getHallName();

	Integer getHallCapacity();

	Integer getAvailableSeats();

	LocalDateTime getEndTime();
}