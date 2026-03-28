package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class GenreNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public GenreNotFoundException(Long genreId) {
		super(String.format("Genre with id '%d' not found", genreId), "GENRE_NOT_FOUND",
				String.format("Genre entity with id %d does not exist", genreId));
	}
}