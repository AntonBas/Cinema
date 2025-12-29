package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.filter.MovieFilter;
import ua.lviv.bas.cinema.dto.filter.MovieFilter.SortDirection;
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
	void getMoviesPaginated_ShouldReturnPage() {
		MovieDetailResponse movie = createMovieDto(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		Page<MovieDetailResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 12), 1);
		PageResponse<MovieDetailResponse> pageResponse = PageResponse.of(page, movieDto -> movieDto);

		when(movieService.getMoviesPaginated(any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieDetailResponse>> response = movieController
				.getMoviesPaginated(PageRequest.of(0, 12));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieDetailResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals("Test Movie", responseBody.getContent().get(0).getTitle());
		assertEquals(1, responseBody.getTotalElements());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(12, responseBody.getPageSize());
		verify(movieService).getMoviesPaginated(any(Pageable.class));
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
	void findCurrentlyShowingPaginated_ShouldReturnPage() {
		MovieCardResponse movie = createMovieCardResponse(1L, "Current Movie", "current-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 12), 1);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page, movieCard -> movieCard);

		when(movieService.findCurrentlyShowingPaginated(any(Pageable.class), eq(false))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController
				.findCurrentlyShowingPaginated(PageRequest.of(0, 12));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals("Current Movie", responseBody.getContent().get(0).getTitle());
		verify(movieService).findCurrentlyShowingPaginated(any(Pageable.class), eq(false));
	}

	@Test
	void findUpcomingPaginated_ShouldReturnPage() {
		MovieCardResponse movie = createMovieCardResponse(1L, "Upcoming Movie", "upcoming-1", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 12), 1);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page, movieCard -> movieCard);

		when(movieService.findUpcomingPaginated(any(Pageable.class), eq(false))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController
				.findUpcomingPaginated(PageRequest.of(0, 12));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals("Upcoming Movie", responseBody.getContent().get(0).getTitle());
		verify(movieService).findUpcomingPaginated(any(Pageable.class), eq(false));
	}

	@Test
	void getNewReleases_ShouldReturnMovies() {
		MovieCardResponse movie = createMovieCardResponse(1L, "New Release", "new-release", MovieStatus.CURRENT);
		List<MovieCardResponse> movies = List.of(movie);

		when(movieService.getNewReleases(5)).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getNewReleases(5);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.size());
		assertEquals("New Release", responseBody.get(0).getTitle());
		verify(movieService).getNewReleases(5);
	}

	@Test
	void getEndingSoon_ShouldReturnMovies() {
		MovieCardResponse movie = createMovieCardResponse(1L, "Ending Soon", "ending-soon", MovieStatus.CURRENT);
		List<MovieCardResponse> movies = List.of(movie);

		when(movieService.getEndingSoon(5)).thenReturn(movies);

		ResponseEntity<List<MovieCardResponse>> response = movieController.getEndingSoon(5);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.size());
		assertEquals("Ending Soon", responseBody.get(0).getTitle());
		verify(movieService).getEndingSoon(5);
	}

	@Test
	void findFilteredMovies_ShouldReturnFilteredMovies() {
		MovieCardResponse movie = createMovieCardResponse(1L, "Filtered Movie", "filtered-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 20), 1);
		PageResponse<MovieCardResponse> pageResponse = PageResponse.of(page, movieCard -> movieCard);

		when(movieService.findFilteredMovies(any(MovieFilter.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.findFilteredMovies("test",
				MovieStatus.CURRENT, AgeRating.PEGI_12, 90, 180, LocalDate.now().minusMonths(1), LocalDate.now(),
				"title", SortDirection.ASC, 0, 20);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals("Filtered Movie", responseBody.getContent().get(0).getTitle());
		verify(movieService).findFilteredMovies(any(MovieFilter.class));
	}
}