package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private MovieController movieController;

	private MovieDetailResponse createMovieDetailResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieDetailResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.trailerUrl("https://example.com/trailer").description("Description").durationMinutes(120)
				.ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(status).currentlyShowing(false)
				.upcoming(status == MovieStatus.UPCOMING).archived(status == MovieStatus.ARCHIVED).build();
	}

	private MovieCardResponse createMovieCardResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieCardResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).status(status).currentlyShowing(false).build();
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDetailResponse(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieById(1L)).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieById(1L);

		assertEquals(200, response.getStatusCode().value());

		MovieDetailResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		assertEquals("Test Movie", body.getTitle());

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
		MovieDetailResponse movieDto = createMovieDetailResponse(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);

		when(movieService.getMovieBySlug("test-movie")).thenReturn(movieDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieBySlug("test-movie");

		assertEquals(200, response.getStatusCode().value());

		MovieDetailResponse body = response.getBody();
		assertNotNull(body);
		assertEquals("test-movie", body.getSlug());

		verify(movieService).getMovieBySlug("test-movie");
	}

	@Test
	void getMovieBySlug_WhenNotFound_ShouldThrowException() {
		when(movieService.getMovieBySlug("non-existent")).thenThrow(new MovieNotFoundException("Movie not found"));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMovieBySlug("non-existent"));

		verify(movieService).getMovieBySlug("non-existent");
	}

	@Test
	void getMovies_ShouldReturnPageResponse() {
		MovieFilterRequest filter = new MovieFilterRequest();
		Pageable pageable = PageRequest.of(0, 12);

		MovieCardResponse movie = createMovieCardResponse(1L, "Test Movie", "test-movie", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getMovieCards(filter, pageable)).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(filter, pageable);

		assertEquals(200, response.getStatusCode().value());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Test Movie", body.getContent().get(0).getTitle());

		verify(movieService).getMovieCards(filter, pageable);
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnPageResponse() {
		Pageable pageable = PageRequest.of(0, 12);

		MovieCardResponse movie = createMovieCardResponse(1L, "Current Movie", "current-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getMovieCards(MovieFilterRequest.builder().currentlyShowing(true).build(), pageable))
				.thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies(pageable);

		assertEquals(200, response.getStatusCode().value());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Current Movie", body.getContent().get(0).getTitle());

		verify(movieService).getMovieCards(MovieFilterRequest.builder().currentlyShowing(true).build(), pageable);
	}

	@Test
	void getUpcomingMovies_ShouldReturnPageResponse() {
		Pageable pageable = PageRequest.of(0, 12);

		MovieCardResponse movie = createMovieCardResponse(1L, "Upcoming Movie", "upcoming-1", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getMovieCards(MovieFilterRequest.builder().upcoming(true).build(), pageable))
				.thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getUpcomingMovies(pageable);

		assertEquals(200, response.getStatusCode().value());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Upcoming Movie", body.getContent().get(0).getTitle());

		verify(movieService).getMovieCards(MovieFilterRequest.builder().upcoming(true).build(), pageable);
	}

	@Test
	void searchMoviesForSession_ShouldReturnMovies() {
		MovieSessionSearchResponse movie1 = MovieSessionSearchResponse.builder().id(1L).title("Movie 1").build();

		MovieSessionSearchResponse movie2 = MovieSessionSearchResponse.builder().id(2L).title("Movie 2").build();

		List<MovieSessionSearchResponse> movies = List.of(movie1, movie2);

		when(movieService.searchMoviesForSession("search")).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController.searchMoviesForSession("search");

		assertEquals(200, response.getStatusCode().value());

		List<MovieSessionSearchResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.size());
		assertEquals("Movie 1", body.get(0).getTitle());

		verify(movieService).searchMoviesForSession("search");
	}

	@Test
	void searchMoviesForSession_WithNullSearch_ShouldReturnMovies() {
		MovieSessionSearchResponse movie = MovieSessionSearchResponse.builder().id(1L).title("Movie 1").build();

		List<MovieSessionSearchResponse> movies = List.of(movie);

		when(movieService.searchMoviesForSession(null)).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController.searchMoviesForSession(null);

		assertEquals(200, response.getStatusCode().value());

		List<MovieSessionSearchResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());

		verify(movieService).searchMoviesForSession(null);
	}
}