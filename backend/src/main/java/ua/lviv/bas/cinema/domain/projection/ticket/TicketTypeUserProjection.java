package ua.lviv.bas.cinema.domain.projection.ticket;

import java.math.BigDecimal;

import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;

public interface TicketTypeUserProjection {
	Long getId();

	String getDisplayName();

	BigDecimal getPriceMultiplier();

	boolean isRequiresDocument();

	String getDocumentType();

	TicketTypeCategory getCategory();
}