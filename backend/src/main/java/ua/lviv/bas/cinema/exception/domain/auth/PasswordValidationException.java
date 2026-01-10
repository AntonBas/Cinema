package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class PasswordValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public PasswordValidationException(String message, String errorCode) {
		super(message, errorCode);
	}

	public static PasswordValidationException tooShort(int minLength) {
		return new PasswordValidationException(String.format("Password must be at least %d characters long", minLength),
				"PASSWORD_TOO_SHORT");
	}
}