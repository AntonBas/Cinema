package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class TicketTypeInUseException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketTypeInUseException(Long id, String customMessage) {
        super(customMessage, "TICKET_TYPE_IN_USE",
                String.format("TicketType id=%d is in use", id));
    }
}