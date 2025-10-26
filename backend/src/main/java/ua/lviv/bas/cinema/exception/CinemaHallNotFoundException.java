package ua.lviv.bas.cinema.exception;

public class CinemaHallNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CinemaHallNotFoundException(String message) {
		super(message);
	}

	public CinemaHallNotFoundException(Long id) {
		super("Cinema hall not found with id: " + id);
	}
}