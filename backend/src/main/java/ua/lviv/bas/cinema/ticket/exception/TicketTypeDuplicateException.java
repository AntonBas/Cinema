package ua.lviv.bas.cinema.ticket.exception;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

import java.io.Serial;

public class TicketTypeDuplicateException extends DuplicateEntityException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketTypeDuplicateException(String code) {
        super("TicketType", code);
    }
}