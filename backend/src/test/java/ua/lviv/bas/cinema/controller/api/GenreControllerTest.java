package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.common.GenreService;

@ExtendWith(MockitoExtension.class)
class GenreControllerTest {

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

	private PageResponse<GenreResponse> createPageResponse(List<GenreResponse> content, int currentPage, int totalPages,
			long totalElements, int pageSize) {
		return PageResponse.<GenreResponse>builder().content(content).currentPage(currentPage).totalPages(totalPages)
				.totalElements(totalElements).pageSize(pageSize).first(currentPage == 0)
				.last(currentPage == totalPages - 1 || totalPages == 0).empty(content == null || content.isEmpty())
				.build();
	}

	@Test
	void getGenreById_ShouldReturnGenre() {
		GenreResponse genreResponse = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.getGenreById(GENRE_ID)).thenReturn(genreResponse);

		ResponseEntity<GenreResponse> response = genreController.getGenreById(GENRE_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		GenreResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(GENRE_ID, responseBody.getId());
		assertEquals(GENRE_NAME, responseBody.getName());
		verify(genreService).getGenreById(GENRE_ID);
	}

	@Test
	void getGenreById_WhenNotFound_ShouldThrowException() {
		when(genreService.getGenreById(999L)).thenThrow(new GenreNotFoundException(999L));

		assertThrows(GenreNotFoundException.class, () -> genreController.getGenreById(999L));
	}

	@Test
	void getGenreById_WhenIdIsZero_ShouldThrowException() {
		when(genreService.getGenreById(0L)).thenThrow(new GenreNotFoundException(0L));

		assertThrows(GenreNotFoundException.class, () -> genreController.getGenreById(0L));
	}

	@Test
	void getAllGenres_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Adventure");
		List<GenreResponse> content = List.of(genre1, genre2);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 2, 12);

		when(genreService.getAllGenresPaginated(any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.getAllGenres(DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.getContent().size());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(2, responseBody.getTotalElements());
		assertEquals(12, responseBody.getPageSize());
		verify(genreService).getAllGenresPaginated(DEFAULT_PAGEABLE);
	}

	@Test
	void searchGenres_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Adventure");
		List<GenreResponse> content = List.of(genre1, genre2);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 2, 12);

		when(genreService.searchGenres(eq("act"), any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres("act", DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.getContent().size());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(2, responseBody.getTotalElements());
		assertEquals(12, responseBody.getPageSize());
		verify(genreService).searchGenres(eq("act"), any(Pageable.class));
	}

	@Test
	void searchGenres_WithNullQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 12);

		when(genreService.searchGenres(isNull(), any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		verify(genreService).searchGenres(isNull(), any(Pageable.class));
	}

	@Test
	void searchGenres_WithEmptyQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 12);

		when(genreService.searchGenres(eq(""), any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres("", DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		verify(genreService).searchGenres(eq(""), any(Pageable.class));
	}

	@Test
	void getAllGenresWithoutPagination_ShouldReturnListOfGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Drama");
		GenreResponse genre3 = createGenreResponse(3L, "Comedy");
		List<GenreResponse> genres = List.of(genre1, genre2, genre3);

		when(genreService.getAllGenres()).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenresWithoutPagination();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(3, responseBody.size());
		assertEquals("Action", responseBody.get(0).getName());
		assertEquals("Drama", responseBody.get(1).getName());
		assertEquals("Comedy", responseBody.get(2).getName());
		verify(genreService).getAllGenres();
	}

	@Test
	void getAllGenresWithoutPagination_WhenNoGenres_ShouldReturnEmptyList() {
		List<GenreResponse> emptyList = List.of();

		when(genreService.getAllGenres()).thenReturn(emptyList);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenresWithoutPagination();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(0, responseBody.size());
		verify(genreService).getAllGenres();
	}

	@Test
	void searchGenres_WithDifferentPageable_ShouldWork() {
		Pageable customPageable = PageRequest.of(2, 20, Sort.by("name").descending());
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 2, 5, 100, 20);

		when(genreService.searchGenres(eq("test"), eq(customPageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres("test", customPageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<GenreResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.getCurrentPage());
		assertEquals(20, responseBody.getPageSize());
		verify(genreService).searchGenres(eq("test"), eq(customPageable));
	}
}