package ua.lviv.bas.cinema.exception;

public class InvalidCurrentPasswordException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidCurrentPasswordException(String message) {
		super(message);
	}
}