package ua.lviv.bas.cinema.exception.infrastructure;

import ua.lviv.bas.cinema.exception.core.BusinessException;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public ExternalServiceException(String serviceName, String message) {
		super(String.format("External service '%s' error: %s", serviceName, message), "EXTERNAL_SERVICE_ERROR",
				HttpStatus.SERVICE_UNAVAILABLE, String.format("Communication with %s service failed", serviceName));
	}

	public ExternalServiceException(String serviceName, Throwable cause) {
		super(String.format("External service '%s' unavailable", serviceName), "EXTERNAL_SERVICE_ERROR",
				HttpStatus.SERVICE_UNAVAILABLE, cause.getMessage());
	}
}