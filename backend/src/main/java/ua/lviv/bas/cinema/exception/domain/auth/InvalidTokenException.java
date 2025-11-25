package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class InvalidTokenException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public InvalidTokenException() {
		super("Invalid token", "INVALID_TOKEN", "The provided token is malformed or invalid");
	}

	public InvalidTokenException(String tokenType) {
		super(String.format("Invalid %s token", tokenType), "INVALID_TOKEN",
				String.format("The provided %s token is malformed", tokenType));
	}
}