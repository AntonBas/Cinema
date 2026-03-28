package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class TicketTypeInUseException extends ConflictException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "TICKET_TYPE_IN_USE";

	public TicketTypeInUseException(Long id, String customMessage) {
		super(customMessage, ERROR_CODE, String.format("TicketType id=%d is in use", id));
	}
}