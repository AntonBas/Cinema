package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@ExtendWith(MockitoExtension.class)
class AdminGenreControllerTest {

	@Mock
	private GenreService genreService;

	@InjectMocks
	private AdminGenreController genreController;

	private static final Long GENRE_ID = 1L;
	private static final String GENRE_NAME = "Action";

	private GenreResponse createGenreResponse(Long id, String name) {
		return GenreResponse.builder().id(id).name(name).build();
	}

	private GenreRequest createGenreRequest(String name) {
		return new GenreRequest(name);
	}

	@Test
	void createGenre_ShouldReturnCreatedGenre() {
		GenreRequest request = createGenreRequest(GENRE_NAME);
		GenreResponse responseDto = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.createGenre(any(GenreRequest.class))).thenReturn(responseDto);

		ResponseEntity<GenreResponse> response = genreController.createGenre(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		GenreResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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
	void getGenreById_ShouldReturnGenre() {
		GenreResponse responseDto = createGenreResponse(GENRE_ID, GENRE_NAME);

		when(genreService.getGenreById(GENRE_ID)).thenReturn(responseDto);

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
	void updateGenre_ShouldReturnUpdatedGenre() {
		GenreRequest request = createGenreRequest("Updated Action");
		GenreResponse updatedDto = createGenreResponse(GENRE_ID, "Updated Action");

		when(genreService.updateGenre(eq(GENRE_ID), any(GenreRequest.class))).thenReturn(updatedDto);

		ResponseEntity<GenreResponse> response = genreController.updateGenre(GENRE_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		GenreResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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
}