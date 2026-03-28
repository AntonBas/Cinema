package ua.lviv.bas.cinema.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;

public class CoupleRowSeatsValidator implements ConstraintValidator<CoupleRowSeatsConstraint, Integer> {

	private static final ThreadLocal<CinemaHallRequest> currentRequest = new ThreadLocal<>();

	public static void setCurrentRequest(CinemaHallRequest request) {
		currentRequest.set(request);
	}

	public static void clear() {
		currentRequest.remove();
	}

	@Override
	public boolean isValid(Integer seatsPerRow, ConstraintValidatorContext context) {
		if (seatsPerRow == null) {
			return true;
		}

		CinemaHallRequest request = currentRequest.get();
		if (request == null) {
			return true;
		}

		boolean hasCoupleRows = request.coupleRows() != null && !request.coupleRows().isEmpty();

		if (hasCoupleRows && seatsPerRow % 2 != 0) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					"Number of seats per row must be even because hall has COUPLE seats in rows: "
							+ request.coupleRows())
					.addConstraintViolation();
			return false;
		}

		return true;
	}
}