package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.enums.TicketStatus;

public interface TicketInfoProjection {
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