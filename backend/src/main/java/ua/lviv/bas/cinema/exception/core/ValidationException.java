package ua.lviv.bas.cinema.exception.core;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public abstract class ValidationException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message, String errorCode, @Nullable String debugMessage) {
		super(message, errorCode, HttpStatus.BAD_REQUEST, debugMessage);
	}

	public ValidationException(String message, String errorCode, @Nullable String debugMessage,
			@Nullable Throwable cause) {
		super(message, errorCode, HttpStatus.BAD_REQUEST, debugMessage, cause);
	}
}