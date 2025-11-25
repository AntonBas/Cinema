package ua.lviv.bas.cinema.exception.core;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public abstract class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final HttpStatus status;
	private final String debugMessage;

	public BusinessException(String message, String errorCode, HttpStatus status) {
		this(message, errorCode, status, null);
	}

	public BusinessException(String message, String errorCode, HttpStatus status, @Nullable String debugMessage) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
		this.debugMessage = debugMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	@Nullable
	public String getDebugMessage() {
		return debugMessage;
	}
}