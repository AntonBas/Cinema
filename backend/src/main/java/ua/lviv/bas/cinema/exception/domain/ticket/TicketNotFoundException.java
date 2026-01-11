package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class TicketNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public TicketNotFoundException(Long ticketId) {
		super(String.format("Ticket with ID %d not found", ticketId), "TICKET_NOT_FOUND",
				String.format("Ticket not found with ID: %d", ticketId));
	}

	public TicketNotFoundException(String message) {
		super(message, "TICKET_NOT_FOUND", message);
	}
}