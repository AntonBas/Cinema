package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class EmailValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public EmailValidationException(String message, String errorCode) {
		super(message, errorCode);
	}

	public static EmailValidationException sameEmail() {
		return new EmailValidationException("New email cannot be the same as current email", "SAME_EMAIL");
	}
}