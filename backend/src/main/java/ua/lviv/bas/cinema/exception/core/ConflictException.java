package ua.lviv.bas.cinema.exception.core;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public abstract class ConflictException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public ConflictException(String message, String errorCode) {
		this(message, errorCode, null);
	}

	public ConflictException(String message, String errorCode, @Nullable String debugMessage) {
		super(message, errorCode, HttpStatus.CONFLICT, debugMessage);
	}

	public ConflictException(String message, String errorCode, @Nullable String debugMessage,
			@Nullable Throwable cause) {
		super(message, errorCode, HttpStatus.CONFLICT, debugMessage);
		if (cause != null) {
			this.initCause(cause);
		}
	}
}