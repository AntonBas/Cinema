package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

public class TicketTypeDuplicateException extends DuplicateEntityException {

	private static final long serialVersionUID = 1L;

	public TicketTypeDuplicateException(String code) {
		super("TicketType", code);
	}
}