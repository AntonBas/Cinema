package ua.lviv.bas.cinema.exception.domain.ticket;

import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class TicketTypeInUseException extends ConflictException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "TICKET_TYPE_IN_USE";

	public TicketTypeInUseException(Long id) {
		super(String.format("Ticket type with ID %d is in use and cannot be modified", id), ERROR_CODE,
				String.format("TicketType id=%d is referenced by existing tickets", id));
	}

	public TicketTypeInUseException(Long id, String customMessage) {
		super(customMessage, ERROR_CODE, String.format("TicketType id=%d is in use", id));
	}

	public TicketTypeInUseException(String message, @Nullable String debugMessage) {
		super(message, ERROR_CODE, debugMessage);
	}
}