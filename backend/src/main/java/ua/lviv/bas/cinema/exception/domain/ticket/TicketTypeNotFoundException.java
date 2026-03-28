package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class TicketTypeNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "TICKET_TYPE_NOT_FOUND";

	public TicketTypeNotFoundException(Long id) {
		super(String.format("Ticket type not found with ID: %d", id), ERROR_CODE,
				String.format("TicketType with id=%d not found", id));
	}
}