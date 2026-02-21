package ua.lviv.bas.cinema.exception.domain.cinema;

import java.util.List;
import java.util.stream.Collectors;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class GenreNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public GenreNotFoundException(Long genreId) {
		super(String.format("Genre with id '%d' not found", genreId), "GENRE_NOT_FOUND",
				String.format("Genre entity with id %d does not exist", genreId));
	}

	public GenreNotFoundException(String genreName) {
		super(String.format("Genre with name '%s' not found", genreName), "GENRE_NOT_FOUND",
				String.format("Genre entity with name %s does not exist", genreName));
	}

	public GenreNotFoundException(List<Long> genreIds) {
		super(formatMessage(genreIds), "GENRES_NOT_FOUND", formatDebugMessage(genreIds));
	}

	private static String formatMessage(List<Long> genreIds) {
		if (genreIds.size() == 1) {
			return String.format("Genre with id '%d' not found", genreIds.get(0));
		}
		String ids = genreIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
		return String.format("Genres with ids [%s] not found", ids);
	}

	private static String formatDebugMessage(List<Long> genreIds) {
		String ids = genreIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
		return String.format("Genre entities with ids [%s] do not exist", ids);
	}
}