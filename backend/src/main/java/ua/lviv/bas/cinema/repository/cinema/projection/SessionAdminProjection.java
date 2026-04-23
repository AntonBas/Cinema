package ua.lviv.bas.cinema.repository.cinema.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SessionAdminProjection {
    Long getId();

    LocalDateTime getStartTime();

    BigDecimal getBasePrice();

    String getStatus();

    Long getMovieId();

    String getMovieTitle();

    Integer getMovieDuration();

    Long getHallId();

    String getHallName();

    Long getHallCapacity();

    Long getTicketsSold();

    BigDecimal getTotalRevenue();
}