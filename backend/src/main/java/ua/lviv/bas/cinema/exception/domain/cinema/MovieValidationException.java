package ua.lviv.bas.cinema.exception.domain.cinema;

import java.time.LocalDate;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class MovieValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public MovieValidationException(String message, String errorCode) {
		super(message, errorCode);
	}

	public static MovieValidationException invalidDates(LocalDate releaseDate, LocalDate endShowingDate) {
		return new MovieValidationException(
				String.format("End showing date (%s) cannot be before release date (%s)", endShowingDate, releaseDate),
				"INVALID_MOVIE_DATES");
	}
}