package ua.lviv.bas.cinema.exception;

public class DuplicateEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateEntityException(String message) {
		super(message);
	}

	public DuplicateEntityException(String message, Throwable cause) {
		super(message, cause);
	}
}
