package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private MovieController movieController;

	private MovieDetailResponse createMovieDto(Long id, String title, String slug, MovieStatus status) {
		return MovieDetailResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(status).currentlyShowing(false).actorIds(List.of(1L)).directorIds(List.of(2L))
				.screenwriterIds(List.of(3L)).genreIds(List.of(1L)).build();
	}

	private MovieCardResponse createMovieResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieCardResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(status).currentlyShowing(false).build();
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
	void getMovieById_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieById(1L)).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDetailResponse responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getTitle());
		verify(movieService).getMovieById(1L);
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieBySlug("test-movie")).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieBySlug("test-movie");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDetailResponse responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals("test-movie", responseBody.getSlug());
		verify(movieService).getMovieBySlug("test-movie");
	}

	@Test
	void getAllMovies_ShouldReturnMovies() {
		MovieDetailResponse movie1 = createMovieDto(1L, "Movie 1", "movie-1", MovieStatus.UPCOMING);
		MovieDetailResponse movie2 = createMovieDto(2L, "Movie 2", "movie-2", MovieStatus.CURRENT);
		List<MovieDetailResponse> movies = List.of(movie1, movie2);

		when(movieService.getAllMovies()).thenReturn(movies);

		ResponseEntity<List<MovieDetailResponse>> response = movieController.getAllMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieDetailResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Movie 1", responseBody.get(0).getTitle());
		assertEquals("Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getAllMovies();
	}

	@Test
	void getMoviesPaginated_ShouldReturnPage() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);

		when(movieService.getMoviesPaginated(any(PageRequest.class))).thenReturn(page);

		ResponseEntity<PageResponse<MovieDetailResponse>> response = movieController.getMoviesPaginated(0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<MovieDetailResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Test Movie", responseBody.getContent().get(0).getTitle());
		assertEquals(1, responseBody.getTotalElements());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(10, responseBody.getPageSize());
		verify(movieService).getMoviesPaginated(PageRequest.of(0, 10));
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
		MovieDetailResponse responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("New Movie", responseBody.getTitle());
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
		MovieDetailResponse responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).updateMovie(1L, updateRequest);
	}

	@Test
	void updateMovie_WithoutPosterFile_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MovieDetailResponse movieDto = createMovieDto(1L, "Updated Movie", "updated-movie", MovieStatus.CURRENT);

		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, updateRequest, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDetailResponse responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).updateMovie(1L, updateRequest);
	}

	@Test
	void deleteMovie_ShouldReturnNoContent() {
		ResponseEntity<Void> response = movieController.deleteMovie(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(movieService).deleteMovie(1L);
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieResponse(1L, "Current Movie 1", "current-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieResponse(2L, "Current Movie 2", "current-2", MovieStatus.CURRENT);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getCurrentlyShowingMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieCardResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Current Movie 1", responseBody.get(0).getTitle());
		assertEquals("Current Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getCurrentlyShowingMovies();
	}

	@Test
	void getUpcomingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieResponse(1L, "Upcoming Movie 1", "upcoming-1", MovieStatus.UPCOMING);
		MovieCardResponse movie2 = createMovieResponse(2L, "Upcoming Movie 2", "upcoming-2", MovieStatus.UPCOMING);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getUpcomingMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getUpcomingMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieCardResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Upcoming Movie 1", responseBody.get(0).getTitle());
		assertEquals("Upcoming Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getUpcomingMovies();
	}

	@Test
	void getArchivedMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieResponse(1L, "Archived Movie 1", "archived-1", MovieStatus.ARCHIVED);
		MovieCardResponse movie2 = createMovieResponse(2L, "Archived Movie 2", "archived-2", MovieStatus.ARCHIVED);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getArchivedMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getArchivedMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieCardResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Archived Movie 1", responseBody.get(0).getTitle());
		assertEquals("Archived Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getArchivedMovies();
	}

	@Test
	void getMoviePoster_ShouldReturnImageBytes() {
		byte[] imageBytes = "fake image content".getBytes();
		ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.IMAGE_JPEG).body(imageBytes);

		when(movieService.getMoviePoster(1L)).thenReturn(expectedResponse);

		ResponseEntity<byte[]> response = movieController.getMoviePoster(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(imageBytes, response.getBody());
		verify(movieService).getMoviePoster(1L);
	}

	@Test
	void searchMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieResponse(1L, "Search Movie 1", "search-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieResponse(2L, "Search Movie 2", "search-2", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), PageRequest.of(0, 10), 2);

		when(movieService.searchMovies(eq("test"), any(PageRequest.class))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.searchMovies("test", 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<MovieCardResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.getContent().size());
		assertEquals("Search Movie 1", responseBody.getContent().get(0).getTitle());
		verify(movieService).searchMovies("test", PageRequest.of(0, 10));
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
		List<MovieSessionSearchResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Session Movie 1", responseBody.get(0).getTitle());
		verify(movieService).searchMoviesForSessionCreation("test", LocalDate.now());
	}

	@Test
	void getMoviesPaginated_ShouldLimitSizeToMaxPageSize() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 50), 1);

		when(movieService.getMoviesPaginated(any(PageRequest.class))).thenReturn(page);

		ResponseEntity<PageResponse<MovieDetailResponse>> response = movieController.getMoviesPaginated(0, 100);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<MovieDetailResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(50, responseBody.getPageSize());
		verify(movieService).getMoviesPaginated(PageRequest.of(0, 50));
	}
}