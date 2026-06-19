package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class TicketNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(String message) {
        super("Ticket not found", "TICKET_NOT_FOUND", message);
    }
}