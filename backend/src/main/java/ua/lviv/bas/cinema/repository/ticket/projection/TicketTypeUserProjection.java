package ua.lviv.bas.cinema.repository.ticket.projection;

import java.math.BigDecimal;

import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;

public interface TicketTypeUserProjection {
	Long getId();

	String getDisplayName();

	BigDecimal getPriceMultiplier();

	boolean isRequiresDocument();

	String getDocumentType();

	TicketTypeCategory getCategory();
}