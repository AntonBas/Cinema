package ua.lviv.bas.cinema.repository.ticket.projection;

import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;

import java.math.BigDecimal;

public interface TicketTypeProjection {
    Long getId();

    String getDisplayName();

    BigDecimal getPriceMultiplier();

    Integer getMinAge();

    Integer getMaxAge();

    boolean isRequiresDocument();

    String getDocumentType();

    boolean isActive();

    TicketTypeCategory getCategory();
}