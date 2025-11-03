package ua.lviv.bas.cinema.exception;

public class SessionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SessionNotFoundException(String message) {
		super(message);
	}

	public SessionNotFoundException(Long id) {
		super("Session not found with id: " + id);
	}
}
