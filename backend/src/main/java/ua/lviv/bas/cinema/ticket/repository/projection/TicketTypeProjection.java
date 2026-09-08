package ua.lviv.bas.cinema.ticket.repository.projection;

import ua.lviv.bas.cinema.ticket.domain.TicketTypeCategory;

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