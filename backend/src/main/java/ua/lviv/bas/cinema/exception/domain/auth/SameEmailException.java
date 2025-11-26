package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class SameEmailException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public SameEmailException() {
		super("New email must be different from current email", "SAME_EMAIL",
				"User attempted to change to the same email address");
	}

	public SameEmailException(String message) {
		super(message, "SAME_EMAIL");
	}
}