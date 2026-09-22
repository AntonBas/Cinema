package ua.lviv.bas.cinema.cinema.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionScheduleProjection {
    Long getId();

    UUID getPublicId();

    LocalDateTime getStartTime();

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