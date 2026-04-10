package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@ExtendWith(MockitoExtension.class)
public class AdminMovieControllerTest {

	@Mock
	private MovieService movieService;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private AdminMovieController movieController;

	private MovieAdminResponse createMovieAdminResponse(Long id, String title) {
		return new MovieAdminResponse(id, title, "trailer.mp4", "Description", 120, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING, "poster.jpg",
				"/api/movies/" + id + "/poster", List.of(), List.of(), List.of(), List.of());
	}

	private MovieCardResponse createMovieCardDto(Long id, String title) {
		return new MovieCardResponse(id, title.toLowerCase().replace(" ", "-"), title, "/api/movies/" + id + "/poster",
				120, AgeRating.PEGI_12, MovieStatus.UPCOMING);
	}

	@Test
	void createMovieShouldReturnCreatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"New Movie\",\"description\":\"Description\",\"durationMinutes\":120}";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());
		MovieAdminResponse responseDto = createMovieAdminResponse(1L, "New Movie");

		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
				.durationMinutes(120).build();

		when(objectMapper.readValue(movieDataJson, MovieCreateRequest.class)).thenReturn(request);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(responseDto);

		MovieAdminResponse response = movieController.createMovie(movieDataJson, posterFile);

		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo("New Movie");
		verify(movieService).createMovie(any(MovieCreateRequest.class));
	}

	@Test
	void createMovieWithoutPosterShouldCallServiceWithNullPoster() throws Exception {
		String movieDataJson = "{\"title\":\"New Movie\",\"description\":\"Description\",\"durationMinutes\":120}";
		MovieAdminResponse responseDto = createMovieAdminResponse(1L, "New Movie");

		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
				.durationMinutes(120).build();

		when(objectMapper.readValue(movieDataJson, MovieCreateRequest.class)).thenReturn(request);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(responseDto);

		MovieAdminResponse response = movieController.createMovie(movieDataJson, null);

		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo("New Movie");
		verify(movieService).createMovie(any(MovieCreateRequest.class));
	}

	@Test
	void createMovieWithInvalidJsonShouldThrowException() throws Exception {
		String invalidMovieDataJson = "invalid json";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(objectMapper.readValue(invalidMovieDataJson, MovieCreateRequest.class))
				.thenThrow(new JsonProcessingException("Invalid JSON") {
				});

		assertThrows(IllegalArgumentException.class,
				() -> movieController.createMovie(invalidMovieDataJson, posterFile));
	}

	@Test
	void getMovieShouldReturnMovie() {
		MovieAdminResponse responseDto = createMovieAdminResponse(1L, "Test Movie");

		when(movieService.getMovie(1L)).thenReturn(responseDto);

		MovieAdminResponse response = movieController.getMovie(1L);

		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo("Test Movie");
		verify(movieService).getMovie(1L);
	}

	@Test
	void getMovieWhenNotFoundShouldThrowException() {
		when(movieService.getMovie(999L)).thenThrow(new MovieNotFoundException(999L));

		assertThrows(MovieNotFoundException.class, () -> movieController.getMovie(999L));
		verify(movieService).getMovie(999L);
	}

	@Test
	void getMoviesWithoutFiltersShouldReturnPageOfMovies() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie1 = createMovieCardDto(1L, "Movie 1");
		MovieCardResponse movie2 = createMovieCardDto(2L, "Movie 2");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getMovies(isNull(), isNull(), eq(pageable))).thenReturn(page);

		PageResponse<MovieCardResponse> response = movieController.getMovies(null, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(2);
		assertThat(response.number()).isZero();
		assertThat(response.size()).isEqualTo(20);
		assertThat(response.totalElements()).isEqualTo(2);

		verify(movieService).getMovies(isNull(), isNull(), eq(pageable));
	}

	@Test
	void getMoviesWithTitleFilterShouldReturnFilteredMovies() {
		String titleFilter = "Movie";
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getMovies(eq(titleFilter), isNull(), eq(pageable))).thenReturn(page);

		PageResponse<MovieCardResponse> response = movieController.getMovies(titleFilter, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).title()).isEqualTo("Movie 1");

		verify(movieService).getMovies(eq(titleFilter), isNull(), eq(pageable));
	}

	@Test
	void getMoviesWithStatusFilterShouldReturnFilteredMovies() {
		MovieStatus statusFilter = MovieStatus.UPCOMING;
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getMovies(isNull(), eq(statusFilter), eq(pageable))).thenReturn(page);

		PageResponse<MovieCardResponse> response = movieController.getMovies(null, statusFilter, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).status()).isEqualTo(MovieStatus.UPCOMING);

		verify(movieService).getMovies(isNull(), eq(statusFilter), eq(pageable));
	}

	@Test
	void searchMoviesShouldReturnMovies() {
		String searchTerm = "movie";
		List<MovieSessionSearchResponse> movies = List.of(new MovieSessionSearchResponse(1L, "Movie 1", 120),
				new MovieSessionSearchResponse(2L, "Movie 2", 130));

		when(movieService.searchMovies(searchTerm)).thenReturn(movies);

		List<MovieSessionSearchResponse> response = movieController.searchMovies(searchTerm);

		assertThat(response).hasSize(2);
		verify(movieService).searchMovies(searchTerm);
	}

	@Test
	void searchMoviesWithoutSearchTermShouldReturnAllMovies() {
		List<MovieSessionSearchResponse> movies = List.of(new MovieSessionSearchResponse(1L, "Movie 1", 120),
				new MovieSessionSearchResponse(2L, "Movie 2", 130));

		when(movieService.searchMovies(null)).thenReturn(movies);

		List<MovieSessionSearchResponse> response = movieController.searchMovies(null);

		assertThat(response).hasSize(2);
		verify(movieService).searchMovies(null);
	}

	@Test
	void searchMoviesWhenNoResultsShouldReturnEmptyList() {
		when(movieService.searchMovies("nonexistent")).thenReturn(List.of());

		List<MovieSessionSearchResponse> response = movieController.searchMovies("nonexistent");

		assertThat(response).isEmpty();
		verify(movieService).searchMovies("nonexistent");
	}

	@Test
	void updateMovieShouldReturnUpdatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":false}";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());
		MovieAdminResponse responseDto = createMovieAdminResponse(1L, "Updated Movie");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
				.description("Updated Description").durationMinutes(130).removePoster(false).build();

		when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

		MovieAdminResponse response = movieController.updateMovie(1L, movieDataJson, posterFile);

		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo("Updated Movie");
		verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
	}

	@Test
	void updateMovieWithoutPosterShouldReturnUpdatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":true}";
		MovieAdminResponse responseDto = createMovieAdminResponse(1L, "Updated Movie");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
				.description("Updated Description").durationMinutes(130).removePoster(true).build();

		when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

		MovieAdminResponse response = movieController.updateMovie(1L, movieDataJson, null);

		assertThat(response).isNotNull();
		assertThat(response.title()).isEqualTo("Updated Movie");
		verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
	}

	@Test
	void deleteMovieShouldCallService() {
		movieController.deleteMovie(1L);

		verify(movieService).deleteMovie(1L);
	}

	@Test
	void deleteMovieWhenNotFoundShouldThrowException() {
		doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);

		assertThrows(MovieNotFoundException.class, () -> movieController.deleteMovie(999L));
	}
}