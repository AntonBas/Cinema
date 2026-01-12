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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
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
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(status).currentlyShowing(false).build();
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		MovieDetailResponse movieDto = createMovieDetailResponse(1L, "Test Movie", "test-movie", MovieStatus.UPCOMING);
		when(movieService.getMovieById(1L)).thenReturn(movieDto);
		ResponseEntity<MovieDetailResponse> response = movieController.getMovieById(1L);
		assertEquals(HttpStatus.OK, response.getStatusCode());
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
		assertEquals(HttpStatus.OK, response.getStatusCode());
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
	void getMoviesPaginated_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 12);
		MovieCardResponse movie = createMovieCardResponse(1L, "Test Movie", "test-movie", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.getCurrentlyShowingPage(pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.getMoviesPaginated(pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Test Movie", body.getContent().get(0).getTitle());
		verify(movieService).getCurrentlyShowingPage(pageable);
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Current Movie 1", "current-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Current Movie 2", "current-2", MovieStatus.CURRENT);
		List<MovieCardResponse> movies = List.of(movie1, movie2);
		when(movieService.getCurrentlyShowing(10)).thenReturn(movies);
		ResponseEntity<List<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.size());
		assertEquals("Current Movie 1", body.get(0).getTitle());
		verify(movieService).getCurrentlyShowing(10);
	}

	@Test
	void getUpcomingMovies_ShouldReturnMovies() {
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Upcoming Movie 1", "upcoming-1", MovieStatus.UPCOMING);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Upcoming Movie 2", "upcoming-2", MovieStatus.UPCOMING);
		List<MovieCardResponse> movies = List.of(movie1, movie2);
		when(movieService.getUpcoming(10)).thenReturn(movies);
		ResponseEntity<List<MovieCardResponse>> response = movieController.getUpcomingMovies();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.size());
		assertEquals("Upcoming Movie 1", body.get(0).getTitle());
		verify(movieService).getUpcoming(10);
	}

	@Test
	void getCurrentlyShowingPage_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 12);
		MovieCardResponse movie = createMovieCardResponse(1L, "Current Movie", "current-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.getCurrentlyShowingPage(pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.getCurrentlyShowingPage(pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Current Movie", body.getContent().get(0).getTitle());
		verify(movieService).getCurrentlyShowingPage(pageable);
	}

	@Test
	void getUpcomingPage_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 12);
		MovieCardResponse movie = createMovieCardResponse(1L, "Upcoming Movie", "upcoming-1", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.getUpcomingPage(pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.getUpcomingPage(pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Upcoming Movie", body.getContent().get(0).getTitle());
		verify(movieService).getUpcomingPage(pageable);
	}

	@Test
	void findFilteredMovies_ShouldCallServiceWithSearchAndStatus() {
		Pageable pageable = PageRequest.of(0, 20);
		String search = "Test";
		MovieStatus status = MovieStatus.CURRENT;
		MovieCardResponse movie = createMovieCardResponse(1L, "Test Movie", "test-movie", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.findFilteredMovies(search, status, pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.findFilteredMovies(search, status, pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(movieService).findFilteredMovies(search, status, pageable);
	}

	@Test
	void findFilteredMovies_ShouldCallServiceWithSearchOnly() {
		Pageable pageable = PageRequest.of(0, 20);
		String search = "Test";
		MovieCardResponse movie = createMovieCardResponse(1L, "Test Movie", "test-movie", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.findFilteredMovies(search, null, pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.findFilteredMovies(search, null, pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(movieService).findFilteredMovies(search, null, pageable);
	}

	@Test
	void findFilteredMovies_ShouldCallServiceWithStatusOnly() {
		Pageable pageable = PageRequest.of(0, 20);
		MovieStatus status = MovieStatus.UPCOMING;
		MovieCardResponse movie = createMovieCardResponse(1L, "Upcoming Movie", "upcoming-1", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.findFilteredMovies(null, status, pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.findFilteredMovies(null, status, pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(movieService).findFilteredMovies(null, status, pageable);
	}

	@Test
	void findFilteredMovies_ShouldCallServiceWithNoFilters() {
		Pageable pageable = PageRequest.of(0, 20);
		MovieCardResponse movie = createMovieCardResponse(1L, "Current Movie", "current-1", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);
		when(movieService.findFilteredMovies(null, null, pageable)).thenReturn(page);
		ResponseEntity<Page<MovieCardResponse>> response = movieController.findFilteredMovies(null, null, pageable);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(movieService).findFilteredMovies(null, null, pageable);
	}

	@Test
	void getMoviePoster_ShouldReturnPoster() {
		byte[] posterData = new byte[] { 1, 2, 3, 4, 5 };
		ResponseEntity<byte[]> posterResponse = ResponseEntity.ok().body(posterData);
		when(movieService.getMoviePoster(1L)).thenReturn(posterResponse);
		ResponseEntity<byte[]> response = movieController.getMoviePoster(1L);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(5, response.getBody().length);
		verify(movieService).getMoviePoster(1L);
	}
}