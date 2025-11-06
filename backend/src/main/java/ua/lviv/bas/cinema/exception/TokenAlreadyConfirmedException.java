package ua.lviv.bas.cinema.exception;

public class TokenAlreadyConfirmedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TokenAlreadyConfirmedException(String message) {
		super(message);
	}
}