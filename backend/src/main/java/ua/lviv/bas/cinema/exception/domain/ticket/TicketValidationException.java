package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class TicketValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketValidationException(String message) {
        super(message, "TICKET_VALIDATION_ERROR");
    }

    public TicketValidationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static TicketValidationException notFound() {
        return new TicketValidationException("Ticket not found", "TICKET_NOT_FOUND");
    }

    public static TicketValidationException alreadyUsed() {
        return new TicketValidationException("Ticket has already been used", "TICKET_ALREADY_USED");
    }
}