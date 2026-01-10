package ua.lviv.bas.cinema.exception.domain.bonus;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class InvalidMinMaxPointsException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public InvalidMinMaxPointsException(int min, int max) {
		super(String.format("Min points (%d) cannot be greater than max points (%d)", min, max),
				"INVALID_MIN_MAX_POINTS", String.format("Validation failed: min=%d, max=%d", min, max));
	}
}
