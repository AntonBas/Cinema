package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class SamePasswordException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public SamePasswordException() {
		super("New password cannot be the same as current password", "SAME_PASSWORD",
				"The new password must be different from the current password");
	}
}