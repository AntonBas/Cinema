package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TicketOperationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public TicketOperationException(String message) {
		super(message, "TICKET_OPERATION_ERROR");
	}

	public static TicketOperationException cannotVoidUsed() {
		return new TicketOperationException("Cannot void an already used ticket");
	}
}
