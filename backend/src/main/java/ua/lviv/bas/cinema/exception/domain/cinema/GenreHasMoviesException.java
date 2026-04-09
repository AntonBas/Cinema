package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class GenreHasMoviesException extends BusinessException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "PERSON_HAS_MOVIES";
	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	public GenreHasMoviesException(Long genreId, String genreName, long totalMovieCount) {
		super(String.format("Genre '%s' (id: %d) cannot be deleted", genreName, genreId), ERROR_CODE, STATUS, String
				.format("Genre '%s' is associated with %d movie(s) and cannot be deleted", genreName, totalMovieCount));
	}
}
