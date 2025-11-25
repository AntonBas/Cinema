package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class TokenAlreadyConfirmedException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public TokenAlreadyConfirmedException() {
		super("Token has already been confirmed", "TOKEN_ALREADY_CONFIRMED",
				"The provided token was already used for confirmation");
	}

	public TokenAlreadyConfirmedException(String tokenType) {
		super(String.format("%s token has already been confirmed", tokenType), "TOKEN_ALREADY_CONFIRMED",
				String.format("The %s token was already used", tokenType));
	}
}