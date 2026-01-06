package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@ExtendWith(MockitoExtension.class)
class AdminMovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private AdminMovieController movieController;

	private MovieDetailResponse createMovieDto(Long id, String title, String slug, MovieStatus status) {
		GenreResponse genreResponse = GenreResponse.builder().id(1L).name("Action").build();
		PersonResponse actorResponse = PersonResponse.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		PersonResponse directorResponse = PersonResponse.builder().id(2L).name("Director One").role(PersonRole.DIRECTOR)
				.build();
		PersonResponse screenwriterResponse = PersonResponse.builder().id(3L).name("Writer One")
				.role(PersonRole.SCREENWRITER).build();

		return MovieDetailResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.trailerUrl("https://example.com/trailer").description("Description").durationMinutes(120)
				.ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(status).currentlyShowing(false)
				.upcoming(status == MovieStatus.UPCOMING).archived(status == MovieStatus.ARCHIVED)
				.genres(List.of(genreResponse)).actors(List.of(actorResponse)).directors(List.of(directorResponse))
				.screenwriters(List.of(screenwriterResponse)).build();
	}

	private MovieCreateRequest createMovieCreateRequest(String title) {
		return MovieCreateRequest.builder().title(title).trailerUrl("https://example.com/trailer")
				.description("Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L))
				.actorIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).build();
	}

	private MovieUpdateRequest createMovieUpdateRequest(String title) {
		return MovieUpdateRequest.builder().title(title).trailerUrl("https://example.com/trailer")
				.description("Updated Description").durationMinutes(130).releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(35)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L, 2L))
				.actorIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).removePoster(false)
				.build();
	}

	@Test
	void createMovie_ShouldReturnCreatedMovie() {
		MovieCreateRequest createRequest = createMovieCreateRequest("New Movie");
		MovieDetailResponse movieDto = createMovieDto(1L, "New Movie", "new-movie", MovieStatus.UPCOMING);
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		createRequest.setPosterFile(posterFile);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.createMovie(createRequest, posterFile);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		MovieDetailResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertEquals("New Movie", responseBody.getTitle());
		verify(movieService).createMovie(createRequest);
	}

	@Test
	void createMovie_WhenDuplicateTitle_ShouldThrowException() {
		MovieCreateRequest createRequest = createMovieCreateRequest("Existing Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.createMovie(any(MovieCreateRequest.class)))
				.thenThrow(new DuplicateEntityException("Movie", "Existing Movie"));

		assertThrows(DuplicateEntityException.class, () -> movieController.createMovie(createRequest, posterFile));
		verify(movieService).createMovie(createRequest);
	}

	@Test
	void updateMovie_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MovieDetailResponse movieDto = createMovieDto(1L, "Updated Movie", "updated-movie", MovieStatus.CURRENT);
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		updateRequest.setPosterFile(posterFile);
		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, updateRequest, posterFile);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		MovieDetailResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).updateMovie(1L, updateRequest);
	}

	@Test
	void updateMovie_WhenNotFound_ShouldThrowException() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(movieService.updateMovie(eq(999L), any(MovieUpdateRequest.class)))
				.thenThrow(new MovieNotFoundException(999L));

		assertThrows(MovieNotFoundException.class, () -> movieController.updateMovie(999L, updateRequest, posterFile));
		verify(movieService).updateMovie(999L, updateRequest);
	}

	@Test
	void updateMovie_WithoutPosterFile_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MovieDetailResponse movieDto = createMovieDto(1L, "Updated Movie", "updated-movie", MovieStatus.CURRENT);

		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, updateRequest, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		MovieDetailResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).updateMovie(1L, updateRequest);
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
		verify(movieService).deleteMovie(999L);
	}

	@Test
	void searchMoviesForSessionCreation_ShouldReturnMovies() {
		MovieSessionSearchResponse movie1 = MovieSessionSearchResponse.builder().id(1L).title("Session Movie 1")
				.releaseYear(2024).durationMinutes(120).build();
		MovieSessionSearchResponse movie2 = MovieSessionSearchResponse.builder().id(2L).title("Session Movie 2")
				.releaseYear(2024).durationMinutes(150).build();
		List<MovieSessionSearchResponse> movies = List.of(movie1, movie2);

		when(movieService.searchMoviesForSessionCreation("test", LocalDate.now())).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController
				.searchMoviesForSessionCreation(LocalDate.now(), "test");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieSessionSearchResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals("Session Movie 1", responseBody.get(0).getTitle());
		verify(movieService).searchMoviesForSessionCreation("test", LocalDate.now());
	}

	@Test
	void searchMoviesForSessionCreation_WithNullSearch_ShouldReturnMovies() {
		MovieSessionSearchResponse movie1 = MovieSessionSearchResponse.builder().id(1L).title("Movie 1")
				.releaseYear(2024).durationMinutes(120).build();
		List<MovieSessionSearchResponse> movies = List.of(movie1);

		when(movieService.searchMoviesForSessionCreation(isNull(), eq(LocalDate.now()))).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController
				.searchMoviesForSessionCreation(LocalDate.now(), null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieSessionSearchResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.size());
		verify(movieService).searchMoviesForSessionCreation(null, LocalDate.now());
	}
}