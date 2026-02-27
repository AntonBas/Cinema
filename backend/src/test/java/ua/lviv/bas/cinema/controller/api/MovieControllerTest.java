package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
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
				.endShowingDate(LocalDate.now().plusDays(30)).status(status).build();
	}

	private MovieCardResponse createMovieCardResponse(Long id, String title, String slug, MovieStatus status) {
		return MovieCardResponse.builder().id(id).title(title).slug(slug).posterUrl("/api/movies/" + id + "/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).status(status).build();
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
		when(movieService.getMovieBySlug("non-existent")).thenThrow(new MovieNotFoundException("non-existent"));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMovieBySlug("non-existent"));
		verify(movieService).getMovieBySlug("non-existent");
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnPageOfCurrentMovies() {
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "releaseDate"));
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Current Movie 1", "current-1", MovieStatus.CURRENT);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Current Movie 2", "current-2", MovieStatus.CURRENT);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getFilteredMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(12, body.getSize());
		assertEquals(2, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable));
	}

	@Test
	void getCurrentlyShowingMovies_WhenNoResults_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 12);
		Page<MovieCardResponse> emptyPage = Page.empty(pageable);

		when(movieService.getFilteredMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable))).thenReturn(emptyPage);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable));
	}

	@Test
	void getUpcomingMovies_ShouldReturnPageOfUpcomingMovies() {
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "releaseDate"));
		MovieCardResponse movie1 = createMovieCardResponse(1L, "Upcoming Movie 1", "upcoming-1", MovieStatus.UPCOMING);
		MovieCardResponse movie2 = createMovieCardResponse(2L, "Upcoming Movie 2", "upcoming-2", MovieStatus.UPCOMING);
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getFilteredMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getUpcomingMovies(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(12, body.getSize());
		assertEquals(2, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable));
	}

	@Test
	void getUpcomingMovies_WhenNoResults_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 12);
		Page<MovieCardResponse> emptyPage = Page.empty(pageable);

		when(movieService.getFilteredMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable))).thenReturn(emptyPage);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getUpcomingMovies(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable));
	}

	@Test
	void getMoviePoster_ShouldReturnPoster() {
		Long movieId = 1L;
		byte[] posterData = new byte[] { 1, 2, 3, 4, 5 };
		ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(posterData);

		when(movieService.getMoviePoster(movieId)).thenReturn(expectedResponse);

		ResponseEntity<byte[]> response = movieController.getMoviePoster(movieId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		verify(movieService).getMoviePoster(movieId);
	}

	@Test
	void getMoviePoster_WhenNotFound_ShouldThrowException() {
		Long movieId = 999L;

		when(movieService.getMoviePoster(movieId)).thenThrow(new MovieNotFoundException(movieId));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMoviePoster(movieId));
		verify(movieService).getMoviePoster(movieId);
	}
}