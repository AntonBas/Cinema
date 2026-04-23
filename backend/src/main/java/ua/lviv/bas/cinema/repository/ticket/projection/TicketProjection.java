package ua.lviv.bas.cinema.repository.ticket.projection;

import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TicketProjection {
    Long getId();

    String getUniqueCode();

    TicketStatus getStatus();

    LocalDateTime getPurchaseTime();

    BigDecimal getFinalPrice();

    String getTicketTypeName();

    String getMovieTitle();

    LocalDateTime getSessionStartTime();

    String getHallName();

    Integer getRow();

    Integer getSeatNumber();

    Long getUserId();

    Long getMovieId();
}