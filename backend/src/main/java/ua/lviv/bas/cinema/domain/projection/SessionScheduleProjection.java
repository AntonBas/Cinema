package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SessionScheduleProjection {
	Long getId();

	LocalDateTime getStartTime();

	LocalDateTime getEndTime();

	BigDecimal getBasePrice();

	Long getMovieId();

	String getMovieTitle();

	String getMoviePosterFileName();

	String getMovieAgeRating();

	Integer getMovieDuration();

	Long getHallId();

	String getHallName();

	Integer getHallCapacity();

	Integer getAvailableSeats();
}