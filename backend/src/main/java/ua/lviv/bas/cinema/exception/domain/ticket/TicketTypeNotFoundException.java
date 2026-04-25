package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class TicketTypeNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketTypeNotFoundException(Long id) {
        super("Ticket type not found", "TICKET_TYPE_NOT_FOUND",
                String.format("TicketType with id=%d not found", id));
    }
}