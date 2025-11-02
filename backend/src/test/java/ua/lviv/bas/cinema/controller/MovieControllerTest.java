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
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.dto.MovieResponse;
import ua.lviv.bas.cinema.dto.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.service.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private MovieController movieController;

	private MovieDto createMovieDto(Long id, String title, String slug, MovieStatus status) {
		return MovieDto.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(status).currentlyShowing(false).actorIds(List.of(1L)).directorIds(List.of(2L))
				.screenwriterIds(List.of(3L)).genreIds(List.of(1L)).build();
	}

	private MovieResponse createMovieResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
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
	void getById_ShouldReturnMovie() {
		MovieDto movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getById(1L)).thenReturn(movieDto);

		ResponseEntity<MovieDto> response = movieController.getById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getTitle());
		verify(movieService).getById(1L);
	}

	@Test
	void getBySlug_ShouldReturnMovie() {
		MovieDto movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getBySlug("test-movie")).thenReturn(movieDto);

		ResponseEntity<MovieDto> response = movieController.getBySlug("test-movie");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals("test-movie", responseBody.getSlug());
		verify(movieService).getBySlug("test-movie");
	}

	@Test
	void getAll_ShouldReturnMovies() {
		MovieDto movie1 = createMovieDto(1L, "Movie 1", "movie-1", MovieStatus.UPCOMING);
		MovieDto movie2 = createMovieDto(2L, "Movie 2", "movie-2", MovieStatus.CURRENT);
		List<MovieDto> movies = List.of(movie1, movie2);

		when(movieService.getAll()).thenReturn(movies);

		ResponseEntity<List<MovieDto>> response = movieController.getAll();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Movie 1", responseBody.get(0).getTitle());
		assertEquals("Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getAll();
	}

	@Test
	void getPaginated_ShouldReturnPage() {
		MovieDto movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDto> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);

		when(movieService.getPaginated(any(PageRequest.class))).thenReturn(page);

		ResponseEntity<PageResponse<MovieDto>> response = movieController.getPaginated(0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<MovieDto> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Test Movie", responseBody.getContent().get(0).getTitle());
		assertEquals(1, responseBody.getTotalElements());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(10, responseBody.getPageSize());
		verify(movieService).getPaginated(PageRequest.of(0, 10));
	}

	@Test
	void create_ShouldReturnCreatedMovie() {
		MovieCreateRequest createRequest = createMovieCreateRequest("New Movie");
		MovieDto movieDto = createMovieDto(1L, "New Movie", "new-movie", MovieStatus.UPCOMING);
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		createRequest.setPosterFile(posterFile);
		when(movieService.create(any(MovieCreateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDto> response = movieController.create(createRequest, posterFile);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		MovieDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("New Movie", responseBody.getTitle());
		verify(movieService).create(createRequest);
	}

	@Test
	void update_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MovieDto movieDto = createMovieDto(1L, "Updated Movie", "updated-movie", MovieStatus.CURRENT);
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		updateRequest.setPosterFile(posterFile);
		when(movieService.update(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDto> response = movieController.update(1L, updateRequest, posterFile);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).update(1L, updateRequest);
	}

	@Test
	void update_WithoutPosterFile_ShouldReturnUpdatedMovie() {
		MovieUpdateRequest updateRequest = createMovieUpdateRequest("Updated Movie");
		MovieDto movieDto = createMovieDto(1L, "Updated Movie", "updated-movie", MovieStatus.CURRENT);

		when(movieService.update(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		ResponseEntity<MovieDto> response = movieController.update(1L, updateRequest, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		MovieDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).update(1L, updateRequest);
	}

	@Test
	void delete_ShouldReturnNoContent() {
		ResponseEntity<Void> response = movieController.delete(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(movieService).delete(1L);
	}

	@Test
	void getCurrentlyShowing_ShouldReturnMovies() {
		MovieResponse movie1 = createMovieResponse(1L, "Current Movie 1", "current-1", MovieStatus.CURRENT);
		MovieResponse movie2 = createMovieResponse(2L, "Current Movie 2", "current-2", MovieStatus.CURRENT);
		List<MovieResponse> movies = List.of(movie1, movie2);

		when(movieService.getCurrentlyShowing()).thenReturn(movies);

		ResponseEntity<List<MovieResponse>> response = movieController.getCurrentlyShowing();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Current Movie 1", responseBody.get(0).getTitle());
		assertEquals("Current Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getCurrentlyShowing();
	}

	@Test
	void getUpcoming_ShouldReturnMovies() {
		MovieResponse movie1 = createMovieResponse(1L, "Upcoming Movie 1", "upcoming-1", MovieStatus.UPCOMING);
		MovieResponse movie2 = createMovieResponse(2L, "Upcoming Movie 2", "upcoming-2", MovieStatus.UPCOMING);
		List<MovieResponse> movies = List.of(movie1, movie2);

		when(movieService.getUpcoming()).thenReturn(movies);

		ResponseEntity<List<MovieResponse>> response = movieController.getUpcoming();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Upcoming Movie 1", responseBody.get(0).getTitle());
		assertEquals("Upcoming Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getUpcoming();
	}

	@Test
	void getArchived_ShouldReturnMovies() {
		MovieResponse movie1 = createMovieResponse(1L, "Archived Movie 1", "archived-1", MovieStatus.ARCHIVED);
		MovieResponse movie2 = createMovieResponse(2L, "Archived Movie 2", "archived-2", MovieStatus.ARCHIVED);
		List<MovieResponse> movies = List.of(movie1, movie2);

		when(movieService.getArchived()).thenReturn(movies);

		ResponseEntity<List<MovieResponse>> response = movieController.getArchived();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Archived Movie 1", responseBody.get(0).getTitle());
		assertEquals("Archived Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getArchived();
	}

	@Test
	void getPoster_ShouldReturnImageBytes() {
		byte[] imageBytes = "fake image content".getBytes();
		ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.IMAGE_JPEG).body(imageBytes);

		when(movieService.getPoster(1L)).thenReturn(expectedResponse);

		ResponseEntity<byte[]> response = movieController.getPoster(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(imageBytes, response.getBody());
		verify(movieService).getPoster(1L);
	}

	@Test
	void getMoviesForSessions_ShouldReturnMovies() {
		MovieResponse movie1 = createMovieResponse(1L, "Session Movie 1", "session-1", MovieStatus.CURRENT);
		MovieResponse movie2 = createMovieResponse(2L, "Session Movie 2", "session-2", MovieStatus.UPCOMING);
		List<MovieResponse> movies = List.of(movie1, movie2);

		when(movieService.getMoviesForSessions()).thenReturn(movies);

		ResponseEntity<List<MovieResponse>> response = movieController.getMoviesForSessions();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieResponse> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals("Session Movie 1", responseBody.get(0).getTitle());
		assertEquals("Session Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getMoviesForSessions();
	}

	@Test
	void getPaginated_ShouldLimitSizeToMaxPageSize() {
		MovieDto movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDto> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 50), 1);

		when(movieService.getPaginated(any(PageRequest.class))).thenReturn(page);

		ResponseEntity<PageResponse<MovieDto>> response = movieController.getPaginated(0, 100);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<MovieDto> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(50, responseBody.getPageSize());
		verify(movieService).getPaginated(PageRequest.of(0, 50));
	}
}