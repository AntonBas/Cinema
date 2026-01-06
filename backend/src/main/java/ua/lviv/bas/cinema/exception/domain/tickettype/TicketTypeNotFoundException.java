package ua.lviv.bas.cinema.exception.domain.tickettype;

import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class TicketTypeNotFoundException extends NotFoundException {
    
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "TICKET_TYPE_NOT_FOUND";
    
    public TicketTypeNotFoundException(Long id) {
        super(
            String.format("Ticket type not found with ID: %d", id),
            ERROR_CODE,
            String.format("TicketType with id=%d not found", id)
        );
    }
    
    public TicketTypeNotFoundException(String code) {
        super(
            String.format("Ticket type not found with code: %s", code),
            ERROR_CODE,
            String.format("TicketType with code='%s' not found", code)
        );
    }
    
    public TicketTypeNotFoundException(String message, @Nullable String debugMessage) {
        super(message, ERROR_CODE, debugMessage);
    }
    
    public TicketTypeNotFoundException(String message, @Nullable String debugMessage, @Nullable Throwable cause) {
        super(message, ERROR_CODE, debugMessage, cause);
    }
}