package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
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
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.ApiResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private MovieController movieController;

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

	private MovieCardResponse createMovieCardResponse(Long id, String title, String slug, MovieStatus status) {
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

		ResponseEntity<ApiResponse<MovieDetailResponse>> response = movieController.getMovieById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<MovieDetailResponse> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		MovieDetailResponse responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getTitle());
		verify(movieService).getMovieById(1L);
	}

	@Test
	void getMovieById_WhenNotFound_ShouldThrowException() {
		when(movieService.getMovieById(999L)).thenThrow(new MovieNotFoundException(999L));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMovieById(999L));
		verify(movieService).getMovieById(999L);
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieBySlug("test-movie")).thenReturn(movieDto);

		ResponseEntity<ApiResponse<MovieDetailResponse>> response = movieController.getMovieBySlug("test-movie");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<MovieDetailResponse> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		MovieDetailResponse responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals("test-movie", responseBody.getSlug());
		verify(movieService).getMovieBySlug("test-movie");
	}

	@Test
	void getMovieBySlug_WhenNotFound_ShouldThrowException() {
		when(movieService.getMovieBySlug("non-existent")).thenThrow(new MovieNotFoundException("Movie not found"));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMovieBySlug("non-existent"));
		verify(movieService).getMovieBySlug("non-existent");
	}

	@Test
	void getAllMovies_ShouldReturnMovies() {
		MovieDetailResponse movie1 = createMovieDto(1L, "Movie 1", "movie-1", MovieStatus.UPCOMING);
		MovieDetailResponse movie2 = createMovieDto(2L, "Movie 2", "movie-2", MovieStatus.CURRENT);
		List<MovieDetailResponse> movies = List.of(movie1, movie2);

		when(movieService.getAllMovies()).thenReturn(movies);

		ResponseEntity<ApiResponse<List<MovieDetailResponse>>> response = movieController.getAllMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<List<MovieDetailResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		List<MovieDetailResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(2, responseBody.size());
		assertEquals("Movie 1", responseBody.get(0).getTitle());
		assertEquals("Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getAllMovies();
	}

	@Test
	void getAllMovies_WhenNoMovies_ShouldReturnEmptyList() {
		when(movieService.getAllMovies()).thenReturn(List.of());

		ResponseEntity<ApiResponse<List<MovieDetailResponse>>> response = movieController.getAllMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<List<MovieDetailResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		List<MovieDetailResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(0, responseBody.size());
		verify(movieService).getAllMovies();
	}

	@Test
	void getMoviesPaginated_ShouldReturnPage() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);
		PageResponse<MovieDetailResponse> pageResponse = PageResponse.of(page);

		when(movieService.getMoviesPaginatedResponse(0, 10)).thenReturn(pageResponse);

		ResponseEntity<ApiResponse<PageResponse<MovieDetailResponse>>> response = movieController.getMoviesPaginated(0,
				10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<PageResponse<MovieDetailResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		PageResponse<MovieDetailResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Test Movie", responseBody.getContent().get(0).getTitle());
		assertEquals(1, responseBody.getTotalElements());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(10, responseBody.getPageSize());
		verify(movieService).getMoviesPaginatedResponse(0, 10);
	}

	@Test
	void getMoviesPaginated_ShouldLimitSizeToMaxPageSize() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 50), 1);
		PageResponse<MovieDetailResponse> pageResponse = PageResponse.of(page);

		when(movieService.getMoviesPaginatedResponse(0, 50)).thenReturn(pageResponse);

		ResponseEntity<ApiResponse<PageResponse<MovieDetailResponse>>> response = movieController.getMoviesPaginated(0,
				100);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<PageResponse<MovieDetailResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		PageResponse<MovieDetailResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(50, responseBody.getPageSize());
		verify(movieService).getMoviesPaginatedResponse(0, 50);
	}

	@Test
	void createMovie_ShouldReturnCreatedMovie() {
		MovieCreateRequest createRequest = createMovieCreateRequest("New Movie");
		MovieDetailResponse movieDto = createMovieDto(1L, "New Movie", "new-movie", MovieStatus.UPCOMING);
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		createRequest.setPosterFile(posterFile);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(movieDto);

		ResponseEntity<ApiResponse<MovieDetailResponse>> response = movieController.createMovie(createRequest,
				posterFile);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		ApiResponse<MovieDetailResponse> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		assertEquals("Movie created successfully", apiResponse.getMessage());
		MovieDetailResponse responseBody = apiResponse.getData();
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

		ResponseEntity<ApiResponse<MovieDetailResponse>> response = movieController.updateMovie(1L, updateRequest,
				posterFile);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<MovieDetailResponse> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		assertEquals("Movie updated successfully", apiResponse.getMessage());
		MovieDetailResponse responseBody = apiResponse.getData();
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

		ResponseEntity<ApiResponse<MovieDetailResponse>> response = movieController.updateMovie(1L, updateRequest,
				null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<MovieDetailResponse> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		assertEquals("Movie updated successfully", apiResponse.getMessage());
		MovieDetailResponse responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals("Updated Movie", responseBody.getTitle());
		verify(movieService).updateMovie(1L, updateRequest);
	}

	@Test
	void deleteMovie_ShouldReturnSuccess() {
		ResponseEntity<ApiResponse<Void>> response = movieController.deleteMovie(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<Void> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		assertEquals("Movie deleted successfully", apiResponse.getMessage());
		verify(movieService).deleteMovie(1L);
	}

	@Test
	void deleteMovie_WhenNotFound_ShouldThrowException() {
		doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);

		assertThrows(MovieNotFoundException.class, () -> movieController.deleteMovie(999L));
		verify(movieService).deleteMovie(999L);
	}

	@Test
	void searchMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Search Movie 1", "search-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Search Movie 2", "search-2", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), PageRequest.of(0, 10), 2);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page);

		when(movieService.searchMoviesResponse("test", 0, 10)).thenReturn(pageResponse);

		ResponseEntity<ApiResponse<PageResponse<MovieCardResponse>>> response = movieController.searchMovies("test", 0,
				10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<PageResponse<MovieCardResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		PageResponse<MovieCardResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(2, responseBody.getContent().size());
		assertEquals("Search Movie 1", responseBody.getContent().get(0).getTitle());
		verify(movieService).searchMoviesResponse("test", 0, 10);
	}

	@Test
	void searchMovies_WithNullQuery_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", "movie-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1), PageRequest.of(0, 10), 1);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page);

		when(movieService.searchMoviesResponse(isNull(), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<ApiResponse<PageResponse<MovieCardResponse>>> response = movieController.searchMovies(null, 0,
				10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<PageResponse<MovieCardResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		PageResponse<MovieCardResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		verify(movieService).searchMoviesResponse(null, 0, 10);
	}

	@Test
	void searchMoviesForSessionCreation_ShouldReturnMovies() {
		MovieSessionSearchResponse movie1 = MovieSessionSearchResponse.builder().id(1L).title("Session Movie 1")
				.releaseYear(2024).durationMinutes(120).build();
		MovieSessionSearchResponse movie2 = MovieSessionSearchResponse.builder().id(2L).title("Session Movie 2")
				.releaseYear(2024).durationMinutes(150).build();
		List<MovieSessionSearchResponse> movies = List.of(movie1, movie2);

		when(movieService.searchMoviesForSessionCreation("test", LocalDate.now())).thenReturn(movies);

		ResponseEntity<ApiResponse<List<MovieSessionSearchResponse>>> response = movieController
				.searchMoviesForSessionCreation(LocalDate.now(), "test");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<List<MovieSessionSearchResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		List<MovieSessionSearchResponse> responseBody = apiResponse.getData();
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

		ResponseEntity<ApiResponse<List<MovieSessionSearchResponse>>> response = movieController
				.searchMoviesForSessionCreation(LocalDate.now(), null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ApiResponse<List<MovieSessionSearchResponse>> apiResponse = Objects.requireNonNull(response.getBody());
		assertTrue(apiResponse.isSuccess());
		List<MovieSessionSearchResponse> responseBody = apiResponse.getData();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.size());
		verify(movieService).searchMoviesForSessionCreation(null, LocalDate.now());
	}
}