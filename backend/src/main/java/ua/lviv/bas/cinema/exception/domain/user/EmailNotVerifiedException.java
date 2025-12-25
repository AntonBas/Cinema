package ua.lviv.bas.cinema.exception.domain.user;

public class EmailNotVerifiedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailNotVerifiedException() {
		super("Email not verified");
	}

	public EmailNotVerifiedException(String message) {
		super(message);
	}
}