package ua.lviv.bas.cinema.repository.ticket.projection;

import java.math.BigDecimal;

public interface TicketTypeUserProjection {
	Long getId();

	String getDisplayName();

	BigDecimal getPriceMultiplier();

	Integer getMinAge();

	Integer getMaxAge();

	boolean isRequiresDocument();

	String getDocumentType();
}