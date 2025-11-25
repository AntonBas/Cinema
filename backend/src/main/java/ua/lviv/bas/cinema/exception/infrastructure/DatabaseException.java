package ua.lviv.bas.cinema.exception.infrastructure;

import ua.lviv.bas.cinema.exception.core.BusinessException;
import org.springframework.http.HttpStatus;

public class DatabaseException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public DatabaseException(String operation, String message) {
		super(String.format("Database error during %s: %s", operation, message), "DATABASE_ERROR",
				HttpStatus.INTERNAL_SERVER_ERROR, String.format("Database operation '%s' failed", operation));
	}

	public DatabaseException(String operation, Throwable cause) {
		super(String.format("Database error during %s", operation), "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
				cause.getMessage());
	}
}