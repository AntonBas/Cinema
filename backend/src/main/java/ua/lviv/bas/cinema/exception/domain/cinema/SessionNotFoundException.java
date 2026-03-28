package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class SessionNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public SessionNotFoundException(Long sessionId) {
		super(String.format("Session with id '%d' not found", sessionId), "SESSION_NOT_FOUND",
				String.format("Session entity with id %d does not exist", sessionId));
	}
}