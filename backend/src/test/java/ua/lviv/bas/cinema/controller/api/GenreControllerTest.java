package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
	private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 12, Sort.by("name").ascending());

	private GenreResponse createGenreResponse(Long id, String name) {
		return GenreResponse.builder().id(id).name(name).build();
	}

	@Test
	void getGenreById_ShouldReturnGenre() {
		GenreResponse genreResponse = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.getGenreById(GENRE_ID)).thenReturn(genreResponse);

		ResponseEntity<GenreResponse> response = genreController.getGenreById(GENRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

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
	void getAllGenres_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Adventure");
		List<GenreResponse> content = List.of(genre1, genre2);
		Page<GenreResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 2);

		when(genreService.getGenresPage(any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<GenreResponse>> response = genreController.getAllGenres(DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(2, body.getTotalElements());

		verify(genreService).getGenresPage(DEFAULT_PAGEABLE);
	}

	@Test
	void searchGenres_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Adventure");
		List<GenreResponse> content = List.of(genre1, genre2);
		Page<GenreResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 2);

		when(genreService.searchGenres(eq("act"), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<GenreResponse>> response = genreController.searchGenres("act", DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());

		verify(genreService).searchGenres(eq("act"), any(Pageable.class));
	}

	@Test
	void searchGenres_WithNullQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		Page<GenreResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 1);

		when(genreService.searchGenres(any(), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<GenreResponse>> response = genreController.searchGenres(null, DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());

		verify(genreService).searchGenres(any(), any(Pageable.class));
	}

	@Test
	void searchGenres_WithEmptyQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		Page<GenreResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 1);

		when(genreService.searchGenres(eq(""), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<GenreResponse>> response = genreController.searchGenres("", DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());

		verify(genreService).searchGenres(eq(""), any(Pageable.class));
	}

	@Test
	void getAllGenresWithoutPagination_ShouldReturnListOfGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Drama");
		GenreResponse genre3 = createGenreResponse(3L, "Comedy");
		List<GenreResponse> genres = List.of(genre1, genre2, genre3);

		when(genreService.getGenres()).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenresWithoutPagination();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(3, body.size());

		verify(genreService).getGenres();
	}

	@Test
	void getAllGenresWithoutPagination_WhenNoGenres_ShouldReturnEmptyList() {
		List<GenreResponse> emptyList = List.of();

		when(genreService.getGenres()).thenReturn(emptyList);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenresWithoutPagination();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertTrue(body.isEmpty());

		verify(genreService).getGenres();
	}

	@Test
	void getGenresForSelect_ShouldReturnSortedListOfGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Drama");
		GenreResponse genre3 = createGenreResponse(3L, "Comedy");
		List<GenreResponse> genres = List.of(genre3, genre1, genre2);

		when(genreService.getGenresSorted()).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getGenresForSelect();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(3, body.size());

		verify(genreService).getGenresSorted();
	}

	@Test
	void searchGenres_WithDifferentPageable_ShouldWork() {
		Pageable customPageable = PageRequest.of(2, 20, Sort.by("name").descending());
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		Page<GenreResponse> page = new PageImpl<>(content, customPageable, 100);

		when(genreService.searchGenres(eq("test"), eq(customPageable))).thenReturn(page);

		ResponseEntity<Page<GenreResponse>> response = genreController.searchGenres("test", customPageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<GenreResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getNumber());
		assertEquals(20, body.getSize());

		verify(genreService).searchGenres(eq("test"), eq(customPageable));
	}
}