package ua.lviv.bas.cinema.exception;

public class SeatNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SeatNotFoundException(String message) {
		super(message);
	}

	public SeatNotFoundException(Long id) {
		super("Seat not found with id: " + id);
	}

	public SeatNotFoundException(Long hallId, int row, int number) {
		super(String.format("Seat not found at position: hall=%d, row=%d, number=%d", hallId, row, number));
	}
}