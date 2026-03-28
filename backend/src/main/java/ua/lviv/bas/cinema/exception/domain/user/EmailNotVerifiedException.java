package ua.lviv.bas.cinema.exception.domain.user;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class EmailNotVerifiedException extends BusinessException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "EMAIL_NOT_VERIFIED";
	private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

	public EmailNotVerifiedException(String action, String email) {
		super(String.format("Cannot %s: email '%s' is not verified", action, email), ERROR_CODE, STATUS,
				String.format("Email verification required for %s action", action));
	}
}