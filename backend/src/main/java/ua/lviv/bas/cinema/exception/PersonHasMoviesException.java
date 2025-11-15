package ua.lviv.bas.cinema.exception;

public class PersonHasMoviesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PersonHasMoviesException(String message) {
		super(message);
	}
}