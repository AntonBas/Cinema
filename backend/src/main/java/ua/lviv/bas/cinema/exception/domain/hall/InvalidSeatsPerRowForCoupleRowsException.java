package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class InvalidSeatsPerRowForCoupleRowsException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public InvalidSeatsPerRowForCoupleRowsException(String coupleRows) {
		super(String.format("Number of seats per row must be even because hall has COUPLE seats in rows: %s",
				coupleRows), "INVALID_SEATS_PER_ROW_FOR_COUPLE", HttpStatus.BAD_REQUEST,
				String.format("Seats per row must be even for couple rows: %s", coupleRows));
	}
}