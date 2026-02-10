package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@ExtendWith(MockitoExtension.class)
public class GenreControllerTest {

	@Mock
	private GenreService genreService;

	@InjectMocks
	private GenreController genreController;

	private static final Long GENRE_ID = 1L;
	private static final String GENRE_NAME = "Action";

	private GenreResponse createGenreResponse(Long id, String name) {
		return GenreResponse.builder().id(id).name(name).build();
	}

	@Test
	void getGenreById_ShouldReturnGenre() {
		GenreResponse genreResponse = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.getGenreById(GENRE_ID)).thenReturn(genreResponse);

		ResponseEntity<GenreResponse> response = genreController.getGenreById(GENRE_ID);

		assertEquals(200, response.getStatusCode().value());

		GenreResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(GENRE_ID, body.getId());
		assertEquals(GENRE_NAME, body.getName());

		verify(genreService).getGenreById(GENRE_ID);
	}

	@Test
	void getGenreById_WhenNotFound_ShouldThrowException() {
		when(genreService.getGenreById(999L)).thenThrow(new GenreNotFoundException(999L));

		assertThrows(GenreNotFoundException.class, () -> genreController.getGenreById(999L));
	}

	@Test
	void getPopularGenres_ShouldReturnListOfGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Comedy");
		List<GenreResponse> genres = List.of(genre1, genre2);

		when(genreService.getPopularGenres(null, 10)).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getPopularGenres(null, 10);

		assertEquals(200, response.getStatusCode().value());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.size());

		verify(genreService).getPopularGenres(null, 10);
	}

	@Test
	void getPopularGenres_WithQuery_ShouldReturnFilteredGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> genres = List.of(genre1);

		when(genreService.getPopularGenres("act", 5)).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getPopularGenres("act", 5);

		assertEquals(200, response.getStatusCode().value());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());

		verify(genreService).getPopularGenres("act", 5);
	}

	@Test
	void getGenresByIds_ShouldReturnListOfGenres() {
		List<Long> ids = List.of(1L, 2L, 3L);
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Comedy");
		GenreResponse genre3 = createGenreResponse(3L, "Drama");
		List<GenreResponse> genres = List.of(genre1, genre2, genre3);

		when(genreService.getGenresByIds(ids)).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getGenresByIds(ids);

		assertEquals(200, response.getStatusCode().value());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(3, body.size());

		verify(genreService).getGenresByIds(ids);
	}

	@Test
	void getGenresByIds_WithEmptyList_ShouldReturnEmptyList() {
		List<Long> ids = List.of();
		List<GenreResponse> emptyList = List.of();

		when(genreService.getGenresByIds(ids)).thenReturn(emptyList);

		ResponseEntity<List<GenreResponse>> response = genreController.getGenresByIds(ids);

		assertEquals(200, response.getStatusCode().value());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.size());

		verify(genreService).getGenresByIds(ids);
	}
}