package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.GenreService;

@ExtendWith(MockitoExtension.class)
class GenreControllerTest {

	@Mock
	private GenreService genreService;

	@InjectMocks
	private GenreController genreController;

	private static final Long GENRE_ID = 1L;
	private static final String GENRE_NAME = "Action";

	private GenreResponse createGenreResponse(Long id, String name) {
		return GenreResponse.builder().id(id).name(name).build();
	}

	private GenreRequest createGenreRequest(String name) {
		return new GenreRequest(name);
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
		GenreResponse responseBody = Objects.requireNonNull(response.getBody());
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
	void createGenre_ShouldReturnCreatedGenre() {
		GenreRequest request = createGenreRequest(GENRE_NAME);
		GenreResponse responseDto = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.createGenre(any(GenreRequest.class))).thenReturn(responseDto);

		ResponseEntity<GenreResponse> response = genreController.createGenre(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		GenreResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(GENRE_ID, responseBody.getId());
		assertEquals(GENRE_NAME, responseBody.getName());
		verify(genreService).createGenre(request);
	}

	@Test
	void createGenre_WhenDuplicateName_ShouldThrowException() {
		GenreRequest request = createGenreRequest("Existing Genre");

		when(genreService.createGenre(any(GenreRequest.class)))
				.thenThrow(new DuplicateEntityException("Genre", "Existing Genre"));

		assertThrows(DuplicateEntityException.class, () -> genreController.createGenre(request));
	}

	@Test
	void updateGenre_ShouldReturnUpdatedGenre() {
		GenreRequest request = createGenreRequest("Updated Action");
		GenreResponse updatedDto = createGenreResponse(GENRE_ID, "Updated Action");

		when(genreService.updateGenre(eq(GENRE_ID), any(GenreRequest.class))).thenReturn(updatedDto);

		ResponseEntity<GenreResponse> response = genreController.updateGenre(GENRE_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		GenreResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(GENRE_ID, responseBody.getId());
		assertEquals("Updated Action", responseBody.getName());
		verify(genreService).updateGenre(GENRE_ID, request);
	}

	@Test
	void updateGenre_WhenNotFound_ShouldThrowException() {
		GenreRequest request = createGenreRequest("Updated Genre");

		when(genreService.updateGenre(eq(999L), any(GenreRequest.class))).thenThrow(new GenreNotFoundException(999L));

		assertThrows(GenreNotFoundException.class, () -> genreController.updateGenre(999L, request));
	}

	@Test
	void deleteGenre_ShouldReturnNoContent() {
		ResponseEntity<Void> response = genreController.deleteGenre(GENRE_ID);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(genreService).deleteGenre(GENRE_ID);
	}

	@Test
	void deleteGenre_WhenNotFound_ShouldThrowException() {
		Long nonExistentId = 999L;

		doThrow(new GenreNotFoundException(nonExistentId)).when(genreService).deleteGenre(nonExistentId);

		assertThrows(GenreNotFoundException.class, () -> genreController.deleteGenre(nonExistentId));
	}

	@Test
	void searchGenres_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Adventure");
		List<GenreResponse> content = List.of(genre1, genre2);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 2, 10);

		when(genreService.searchGenres(eq("act"), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres("act", 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(2, responseBody.getContent().size());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(2, responseBody.getTotalElements());
		assertEquals(10, responseBody.getPageSize());
		verify(genreService).searchGenres("act", 0, 10);
	}

	@Test
	void searchGenres_WithNullQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 10);

		when(genreService.searchGenres(isNull(), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(1, responseBody.getContent().size());
		verify(genreService).searchGenres(null, 0, 10);
	}

	@Test
	void searchGenres_WithNegativePage_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, -1, 1, 1, 10);

		when(genreService.searchGenres(isNull(), eq(-1), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, -1, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(-1, responseBody.getCurrentPage());
		verify(genreService).searchGenres(null, -1, 10);
	}

	@Test
	void searchGenres_WithZeroSize_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 0);

		when(genreService.searchGenres(isNull(), eq(0), eq(0))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, 0, 0);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(0, responseBody.getPageSize());
		verify(genreService).searchGenres(null, 0, 0);
	}

	@Test
	void searchGenres_WithLargeSize_ShouldLimitToMaxPageSize() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 50);

		when(genreService.searchGenres(isNull(), eq(0), eq(50))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, 0, 100);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(50, responseBody.getPageSize());
		verify(genreService).searchGenres(null, 0, 50);
	}

	@Test
	void getAllGenres_ShouldReturnListOfGenres() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		GenreResponse genre2 = createGenreResponse(2L, "Drama");
		GenreResponse genre3 = createGenreResponse(3L, "Comedy");
		List<GenreResponse> genres = List.of(genre1, genre2, genre3);

		when(genreService.getAllGenres()).thenReturn(genres);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenres();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(3, responseBody.size());
		assertEquals("Action", responseBody.get(0).getName());
		assertEquals("Drama", responseBody.get(1).getName());
		assertEquals("Comedy", responseBody.get(2).getName());
		verify(genreService).getAllGenres();
	}

	@Test
	void getAllGenres_WhenNoGenres_ShouldReturnEmptyList() {
		List<GenreResponse> emptyList = List.of();

		when(genreService.getAllGenres()).thenReturn(emptyList);

		ResponseEntity<List<GenreResponse>> response = genreController.getAllGenres();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(0, responseBody.size());
		verify(genreService).getAllGenres();
	}

	@Test
	void searchGenres_WithEmptyStringQuery_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 10);

		when(genreService.searchGenres(eq(""), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres("", 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(1, responseBody.getContent().size());
		verify(genreService).searchGenres("", 0, 10);
	}

	@Test
	void searchGenres_WithMaxPageSize_ShouldReturnPagedResponse() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 50);

		when(genreService.searchGenres(isNull(), eq(0), eq(50))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, 0, 50);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(50, responseBody.getPageSize());
		verify(genreService).searchGenres(null, 0, 50);
	}

	@Test
	void searchGenres_DefaultParameters_ShouldWork() {
		GenreResponse genre1 = createGenreResponse(1L, "Action");
		List<GenreResponse> content = List.of(genre1);
		PageResponse<GenreResponse> pageResponse = createPageResponse(content, 0, 1, 1, 12);

		when(genreService.searchGenres(isNull(), eq(0), eq(12))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<GenreResponse>> response = genreController.searchGenres(null, 0, 12);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<GenreResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(12, responseBody.getPageSize());
		verify(genreService).searchGenres(null, 0, 12);
	}
}