package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class HallLayoutHasTicketsException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public HallLayoutHasTicketsException(Long hallId) {
		super(String.format("Cannot update hall layout for hall %d because seats have booked tickets", hallId),
				"HALL_LAYOUT_HAS_TICKETS", HttpStatus.CONFLICT, String.format("Hall %d has booked tickets", hallId));
	}
}