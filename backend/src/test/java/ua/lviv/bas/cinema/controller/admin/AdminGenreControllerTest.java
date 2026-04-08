package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import ua.lviv.bas.cinema.dto.movie.response.GenreListResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@ExtendWith(MockitoExtension.class)
public class AdminGenreControllerTest {

	@Mock
	private GenreService genreService;

	@InjectMocks
	private AdminGenreController controller;

	private final Long GENRE_ID = 1L;
	private final String GENRE_NAME = "Action";

	@Test
	void createGenre_ReturnsCreatedGenre() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, null);

		when(genreService.createGenre(any(GenreRequest.class))).thenReturn(response);

		ResponseEntity<GenreListResponse> result = controller.createGenre(request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isEqualTo(response);
		verify(genreService).createGenre(request);
	}

	@Test
	void createGenre_ThrowsException_WhenDuplicateName() {
		GenreRequest request = new GenreRequest("Existing");

		when(genreService.createGenre(any(GenreRequest.class)))
				.thenThrow(new DuplicateEntityException("Genre", "Existing"));

		assertThatThrownBy(() -> controller.createGenre(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getGenreById_ReturnsGenre() {
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, null);

		when(genreService.getGenreById(GENRE_ID)).thenReturn(response);

		ResponseEntity<GenreListResponse> result = controller.getGenreById(GENRE_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo(response);
		verify(genreService).getGenreById(GENRE_ID);
	}

	@Test
	void getGenreById_ThrowsException_WhenNotFound() {
		when(genreService.getGenreById(999L)).thenThrow(new GenreNotFoundException(999L));

		assertThatThrownBy(() -> controller.getGenreById(999L)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_ReturnsUpdatedGenre() {
		GenreRequest request = new GenreRequest("Updated");
		GenreListResponse response = new GenreListResponse(GENRE_ID, "Updated", null);

		when(genreService.updateGenre(eq(GENRE_ID), any(GenreRequest.class))).thenReturn(response);

		ResponseEntity<GenreListResponse> result = controller.updateGenre(GENRE_ID, request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo(response);
		verify(genreService).updateGenre(GENRE_ID, request);
	}

	@Test
	void updateGenre_ThrowsException_WhenNotFound() {
		GenreRequest request = new GenreRequest("Updated");

		when(genreService.updateGenre(eq(999L), any(GenreRequest.class))).thenThrow(new GenreNotFoundException(999L));

		assertThatThrownBy(() -> controller.updateGenre(999L, request)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void deleteGenre_ReturnsNoContent() {
		ResponseEntity<Void> result = controller.deleteGenre(GENRE_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(genreService).deleteGenre(GENRE_ID);
	}

	@Test
	void deleteGenre_ThrowsException_WhenNotFound() {
		doThrow(new GenreNotFoundException(999L)).when(genreService).deleteGenre(999L);

		assertThatThrownBy(() -> controller.deleteGenre(999L)).isInstanceOf(GenreNotFoundException.class);
	}
}