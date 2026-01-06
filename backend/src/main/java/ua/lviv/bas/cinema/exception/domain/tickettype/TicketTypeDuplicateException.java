package ua.lviv.bas.cinema.exception.domain.tickettype;

import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

public class TicketTypeDuplicateException extends DuplicateEntityException {

	private static final long serialVersionUID = 1L;

	public TicketTypeDuplicateException(String code) {
		super("TicketType", code);
	}

	public TicketTypeDuplicateException(String code, @Nullable String debugMessage) {
		super("TicketType", code, debugMessage);
	}

	public TicketTypeDuplicateException(String message, Throwable cause) {
		super(message, cause);
	}
}