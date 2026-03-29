package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
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

	private final Long MOVIE_ID = 1L;
	private final String SLUG = "test-movie";
	private final String TITLE = "Test Movie";

	private MovieDetailResponse createMovieDetailResponse(MovieStatus status) {
		return new MovieDetailResponse(MOVIE_ID, TITLE, SLUG, null, null, 120, LocalDate.now().plusDays(1), null,
				AgeRating.PEGI_12, status, null, "/api/movies/" + MOVIE_ID + "/poster", null, null, null, null);
	}

	private MovieCardResponse createMovieCardResponse(Long id, String title, MovieStatus status) {
		return new MovieCardResponse(id, SLUG + id, title, null, 120, AgeRating.PEGI_12, status);
	}

	@Test
	void getMovieBySlug_WhenMovieNotArchived_ReturnsOk() {
		MovieDetailResponse movie = createMovieDetailResponse(MovieStatus.UPCOMING);

		when(movieService.getMovieBySlug(SLUG)).thenReturn(movie);

		ResponseEntity<MovieDetailResponse> response = movieController.getMovieBySlug(SLUG);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().slug()).isEqualTo(SLUG);
		verify(movieService).getMovieBySlug(SLUG);
	}

	@Test
	void getMovieBySlug_WhenArchived_ThrowsException() {
		MovieDetailResponse movie = createMovieDetailResponse(MovieStatus.ARCHIVED);

		when(movieService.getMovieBySlug(SLUG)).thenReturn(movie);

		assertThatThrownBy(() -> movieController.getMovieBySlug(SLUG)).isInstanceOf(MovieNotFoundException.class);
		verify(movieService).getMovieBySlug(SLUG);
	}

	@Test
	void getMovieBySlug_WhenNotFound_ThrowsException() {
		when(movieService.getMovieBySlug(SLUG)).thenThrow(new MovieNotFoundException(SLUG));

		assertThatThrownBy(() -> movieController.getMovieBySlug(SLUG)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getCurrentlyShowingMovies_ReturnsOk() {
		Pageable pageable = PageRequest.of(0, 12);
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getFilteredMovies(null, MovieStatus.CURRENT, pageable)).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies(pageable);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(2);
		verify(movieService).getFilteredMovies(null, MovieStatus.CURRENT, pageable);
	}

	@Test
	void getUpcomingMovies_ReturnsOk() {
		Pageable pageable = PageRequest.of(0, 12);
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.UPCOMING);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getFilteredMovies(null, MovieStatus.UPCOMING, pageable)).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getUpcomingMovies(pageable);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(2);
		verify(movieService).getFilteredMovies(null, MovieStatus.UPCOMING, pageable);
	}

	@Test
	void getMoviePoster_ReturnsPoster() {
		byte[] posterData = new byte[] { 1, 2, 3, 4, 5 };
		ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(posterData);

		when(movieService.getMoviePoster(MOVIE_ID)).thenReturn(expectedResponse);

		ResponseEntity<byte[]> response = movieController.getMoviePoster(MOVIE_ID);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(posterData);
		verify(movieService).getMoviePoster(MOVIE_ID);
	}

	@Test
	void getMoviePoster_WhenNotFound_ThrowsException() {
		when(movieService.getMoviePoster(MOVIE_ID)).thenThrow(new MovieNotFoundException(MOVIE_ID));

		assertThatThrownBy(() -> movieController.getMoviePoster(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
	}
}