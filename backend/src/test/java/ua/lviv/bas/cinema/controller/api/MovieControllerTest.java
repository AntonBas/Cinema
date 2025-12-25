package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

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

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.common.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private MovieController movieController;

	private MovieDetailResponse createMovieDto(Long id, String title, String slug, MovieStatus status) {
		return MovieDetailResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.trailerUrl("https://example.com/trailer").description("Description").durationMinutes(120)
				.ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(status).currentlyShowing(false)
				.upcoming(status == MovieStatus.UPCOMING).archived(status == MovieStatus.ARCHIVED).build();
	}

	private MovieCardResponse createMovieCardResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieCardResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(status).currentlyShowing(false).build();
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieById(1L)).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		MovieDetailResponse responseBody = response.getBody();
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

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieBySlug("test-movie");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		MovieDetailResponse responseBody = response.getBody();
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

		ResponseEntity<List<MovieDetailResponse>> response = movieController.getAllMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieDetailResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals("Movie 1", responseBody.get(0).getTitle());
		assertEquals("Movie 2", responseBody.get(1).getTitle());
		verify(movieService).getAllMovies();
	}

	@Test
	void getAllMovies_WhenNoMovies_ShouldReturnEmptyList() {
		when(movieService.getAllMovies()).thenReturn(List.of());

		ResponseEntity<List<MovieDetailResponse>> response = movieController.getAllMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieDetailResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertTrue(responseBody.isEmpty());
		verify(movieService).getAllMovies();
	}

	@Test
	void getMoviesPaginated_ShouldReturnPage() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);
		PageResponse<MovieDetailResponse> pageResponse = PageResponse.of(page);

		when(movieService.getMoviesPaginatedResponse(0, 10)).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieDetailResponse>> response = movieController.getMoviesPaginated(0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieDetailResponse> responseBody = response.getBody();
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

		ResponseEntity<PageResponse<MovieDetailResponse>> response = movieController.getMoviesPaginated(0, 100);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieDetailResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(50, responseBody.getPageSize());
		verify(movieService).getMoviesPaginatedResponse(0, 50);
	}

	@Test
	void searchMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Search Movie 1", "search-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Search Movie 2", "search-2", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), PageRequest.of(0, 10), 2);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page);

		when(movieService.searchMoviesResponse("test", 0, 10)).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.searchMovies("test", 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> responseBody = response.getBody();
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

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.searchMovies(null, 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		verify(movieService).searchMoviesResponse(null, 0, 10);
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Current Movie 1", "current-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Current Movie 2", "current-2", MovieStatus.CURRENT);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getCurrentlyShowingMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals("Current Movie 1", responseBody.get(0).getTitle());
		verify(movieService).getCurrentlyShowingMovies();
	}

	@Test
	void getUpcomingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Upcoming Movie 1", "upcoming-1", MovieStatus.UPCOMING);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Upcoming Movie 2", "upcoming-2", MovieStatus.UPCOMING);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getUpcomingMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getUpcomingMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals("Upcoming Movie 1", responseBody.get(0).getTitle());
		verify(movieService).getUpcomingMovies();
	}

	@Test
	void getArchivedMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Archived Movie 1", "archived-1", MovieStatus.ARCHIVED);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Archived Movie 2", "archived-2", MovieStatus.ARCHIVED);
		List<MovieCardResponse> movies = List.of(movie1, movie2);

		when(movieService.getArchivedMovies()).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getArchivedMovies();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals("Archived Movie 1", responseBody.get(0).getTitle());
		verify(movieService).getArchivedMovies();
	}
}