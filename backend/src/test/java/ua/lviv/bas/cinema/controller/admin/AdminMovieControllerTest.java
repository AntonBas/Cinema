package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@ExtendWith(MockitoExtension.class)
public class AdminMovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private AdminMovieController movieController;

	private MovieDetailResponse createMovieDetailDto(Long id, String title) {
		return MovieDetailResponse.builder().id(id).title(title).slug(title.toLowerCase().replace(" ", "-")).build();
	}

	private MovieCardResponse createMovieCardDto(Long id, String title) {
		return MovieCardResponse.builder().id(id).title(title).slug(title.toLowerCase().replace(" ", "-")).build();
	}

	private MovieCreateRequest createMovieCreateRequest(String title) {
		return MovieCreateRequest.builder().title(title).description("Description").durationMinutes(120).build();
	}

	private MovieUpdateRequest createMovieUpdateRequest(String title) {
		return MovieUpdateRequest.builder().title(title).description("Updated Description").durationMinutes(130)
				.removePoster(false).build();
	}

	@Test
	void createMovie_ShouldReturnCreatedMovie() {
		MovieCreateRequest request = createMovieCreateRequest("New Movie");
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "New Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.createMovie(any())).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.createMovie(request, posterFile);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("New Movie", response.getBody().getTitle());
		verify(movieService).createMovie(any());
	}

	@Test
	void createMovie_WhenDuplicateTitle_ShouldThrowException() {
		MovieCreateRequest request = createMovieCreateRequest("Existing Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.createMovie(any())).thenThrow(new DuplicateEntityException("Movie", "Existing Movie"));

		assertThrows(DuplicateEntityException.class, () -> movieController.createMovie(request, posterFile));
	}

	@Test
	void updateMovie_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest request = createMovieUpdateRequest("Updated Movie");
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Updated Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.updateMovie(eq(1L), any())).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, request, posterFile);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Updated Movie", response.getBody().getTitle());
		verify(movieService).updateMovie(eq(1L), any());
	}

	@Test
	void updateMovie_WhenNotFound_ShouldThrowException() {
		MovieUpdateRequest request = createMovieUpdateRequest("Updated Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.updateMovie(eq(999L), any())).thenThrow(new MovieNotFoundException(999L));

		assertThrows(MovieNotFoundException.class, () -> movieController.updateMovie(999L, request, posterFile));
	}

	@Test
	void deleteMovie_ShouldReturnNoContent() {
		ResponseEntity<Void> response = movieController.deleteMovie(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertNull(response.getBody());
		verify(movieService).deleteMovie(1L);
	}

	@Test
	void deleteMovie_WhenNotFound_ShouldThrowException() {
		doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);

		assertThrows(MovieNotFoundException.class, () -> movieController.deleteMovie(999L));
	}

	@Test
	void getMovies_ShouldReturnPageResponse() {
		MovieFilterRequest filter = new MovieFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);
		Page<MovieCardResponse> page = new PageImpl<>(
				List.of(createMovieCardDto(1L, "Movie 1"), createMovieCardDto(2L, "Movie 2")));

		when(movieService.getFilteredMovies(any(), any())).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(filter, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getContent().size());
		verify(movieService).getFilteredMovies(any(), any());
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Test Movie");

		when(movieService.getMovieById(1L)).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Test Movie", response.getBody().getTitle());
		verify(movieService).getMovieById(1L);
	}

	@Test
	void searchMoviesForSession_ShouldReturnMovies() {
		List<MovieSessionSearchResponse> movies = List.of(
				MovieSessionSearchResponse.builder().id(1L).title("Movie 1").build(),
				MovieSessionSearchResponse.builder().id(2L).title("Movie 2").build());

		when(movieService.searchMoviesForSession("search")).thenReturn(movies);

		ResponseEntity<?> response = movieController.searchMoviesForSession("search");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(movies, response.getBody());
		verify(movieService).searchMoviesForSession("search");
	}
}