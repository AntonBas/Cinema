package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class InvalidCurrentPasswordException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public InvalidCurrentPasswordException() {
		super("Current password is incorrect", "INVALID_CURRENT_PASSWORD",
				"The provided current password does not match the user's actual password");
	}
}