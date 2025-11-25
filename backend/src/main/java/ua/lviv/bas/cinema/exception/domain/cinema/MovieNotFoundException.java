package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class MovieNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public MovieNotFoundException(Long movieId) {
		super(String.format("Movie with id '%d' not found", movieId), "MOVIE_NOT_FOUND",
				String.format("Movie entity with id %d does not exist", movieId));
	}

	public MovieNotFoundException(String title) {
		super(String.format("Movie with title '%s' not found", title), "MOVIE_NOT_FOUND",
				String.format("Movie entity with title %s does not exist", title));
	}
}