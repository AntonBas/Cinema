package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TokenExpiredException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public TokenExpiredException() {
		super("Token has expired", "TOKEN_EXPIRED", "The provided token has exceeded its validity period");
	}

	public TokenExpiredException(String tokenType) {
		super(String.format("%s token has expired", tokenType), "TOKEN_EXPIRED",
				String.format("The %s token has exceeded its validity period", tokenType));
	}
}