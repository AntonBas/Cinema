package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class TicketNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public TicketNotFoundException(String message) {
		super(message, "TICKET_NOT_FOUND", message);
	}
}