package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TicketValidationException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public TicketValidationException(String message) {
		super(message, "TICKET_ERROR", message);
	}

	public static TicketValidationException notFound() {
		return new TicketValidationException("Ticket not found");
	}

	public static TicketValidationException alreadyUsed() {
		return new TicketValidationException("Ticket already used");
	}

	public static TicketValidationException sessionStarted() {
		return new TicketValidationException("Session has already started");
	}
}