package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;

public interface TicketTypeUserProjection {
	Long getId();

	String getDisplayName();

	BigDecimal getPriceMultiplier();

	boolean isRequiresDocument();

	String getDocumentType();
}