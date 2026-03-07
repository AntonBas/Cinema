package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
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

	private MovieDetailResponse createMovieDetailDto(Long id, String title) {
		return MovieDetailResponse.builder().id(id).title(title).slug(title.toLowerCase().replace(" ", "-"))
				.status(MovieStatus.UPCOMING).build();
	}

	private MovieCardResponse createMovieCardDto(Long id, String title) {
		return MovieCardResponse.builder().id(id).title(title).slug(title.toLowerCase().replace(" ", "-"))
				.status(MovieStatus.UPCOMING).build();
	}

	@Test
	void createMovie_ShouldReturnCreatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"New Movie\",\"description\":\"Description\",\"durationMinutes\":120}";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "New Movie");

		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
				.durationMinutes(120).build();

		when(objectMapper.readValue(movieDataJson, MovieCreateRequest.class)).thenReturn(request);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.createMovie(movieDataJson, posterFile);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("New Movie", response.getBody().getTitle());
		verify(movieService).createMovie(any(MovieCreateRequest.class));
	}

	@Test
	void createMovie_WithoutPoster_ShouldReturnCreatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"New Movie\",\"description\":\"Description\",\"durationMinutes\":120}";
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "New Movie");

		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
				.durationMinutes(120).build();

		when(objectMapper.readValue(movieDataJson, MovieCreateRequest.class)).thenReturn(request);
		when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.createMovie(movieDataJson, null);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("New Movie", response.getBody().getTitle());
		verify(movieService).createMovie(any(MovieCreateRequest.class));
	}

	@Test
	void createMovie_WithInvalidJson_ShouldThrowException() throws Exception {
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
	void updateMovie_ShouldReturnUpdatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":false}";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Updated Movie");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
				.description("Updated Description").durationMinutes(130).removePoster(false).build();

		when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, movieDataJson, posterFile);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Updated Movie", response.getBody().getTitle());
		verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
	}

	@Test
	void updateMovie_WithoutPoster_ShouldReturnUpdatedMovie() throws Exception {
		String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":true}";
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Updated Movie");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
				.description("Updated Description").durationMinutes(130).removePoster(true).build();

		when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
		when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.updateMovie(1L, movieDataJson, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Updated Movie", response.getBody().getTitle());
		verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
	}

	@Test
	void updateMovie_WithInvalidJson_ShouldThrowException() throws Exception {
		String invalidMovieDataJson = "invalid json";
		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
				"content".getBytes());

		when(objectMapper.readValue(invalidMovieDataJson, MovieUpdateRequest.class))
				.thenThrow(new JsonProcessingException("Invalid JSON") {
				});

		assertThrows(IllegalArgumentException.class,
				() -> movieController.updateMovie(1L, invalidMovieDataJson, posterFile));
	}

	@Test
	void deleteMovie_ShouldReturnNoContent() {
		ResponseEntity<Void> response = movieController.deleteMovie(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertNull(response.getBody());
		verify(movieService).deleteMovie(1L);
	}

	@Test
	void deleteMovie_WhenNotFound_ShouldThrowException() {
		doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);

		assertThrows(MovieNotFoundException.class, () -> movieController.deleteMovie(999L));
	}

	@Test
	void getMovies_WithoutFilters_ShouldReturnPageOfMovies() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie1 = createMovieCardDto(1L, "Movie 1");
		MovieCardResponse movie2 = createMovieCardDto(2L, "Movie 2");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

		when(movieService.getFilteredMovies(isNull(), isNull(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(null, null, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(20, body.getSize());
		assertEquals(2, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), isNull(), eq(pageable));
	}

	@Test
	void getMovies_WithTitleFilter_ShouldReturnFilteredMovies() {
		String titleFilter = "Movie";
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getFilteredMovies(eq(titleFilter), isNull(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(titleFilter, null,
				pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Movie 1", body.getContent().get(0).getTitle());

		verify(movieService).getFilteredMovies(eq(titleFilter), isNull(), eq(pageable));
	}

	@Test
	void getMovies_WithStatusFilter_ShouldReturnFilteredMovies() {
		MovieStatus statusFilter = MovieStatus.UPCOMING;
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getFilteredMovies(isNull(), eq(statusFilter), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(null, statusFilter,
				pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(MovieStatus.UPCOMING, body.getContent().get(0).getStatus());

		verify(movieService).getFilteredMovies(isNull(), eq(statusFilter), eq(pageable));
	}

	@Test
	void getMovies_WithAllFilters_ShouldReturnFilteredMovies() {
		String titleFilter = "Movie";
		MovieStatus statusFilter = MovieStatus.UPCOMING;
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
		MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
		Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

		when(movieService.getFilteredMovies(eq(titleFilter), eq(statusFilter), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(titleFilter, statusFilter,
				pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Movie 1", body.getContent().get(0).getTitle());
		assertEquals(MovieStatus.UPCOMING, body.getContent().get(0).getStatus());

		verify(movieService).getFilteredMovies(eq(titleFilter), eq(statusFilter), eq(pageable));
	}

	@Test
	void getMovies_WhenNoResults_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<MovieCardResponse> emptyPage = Page.empty(pageable);

		when(movieService.getFilteredMovies(isNull(), isNull(), eq(pageable))).thenReturn(emptyPage);

		ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getMovies(null, null, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<MovieCardResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());

		verify(movieService).getFilteredMovies(isNull(), isNull(), eq(pageable));
	}

	@Test
	void getAdminMovieById_ShouldReturnMovie() {
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Test Movie");

		when(movieService.getAdminMovieById(1L)).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getAdminMovieById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Test Movie", response.getBody().getTitle());
		verify(movieService).getAdminMovieById(1L);
	}

	@Test
	void getAdminMovieById_WhenNotFound_ShouldThrowException() {
		when(movieService.getAdminMovieById(999L)).thenThrow(new MovieNotFoundException(999L));

		assertThrows(MovieNotFoundException.class, () -> movieController.getAdminMovieById(999L));
		verify(movieService).getAdminMovieById(999L);
	}

	@Test
	void getAdminMovieBySlug_ShouldReturnMovie() {
		String slug = "test-movie";
		MovieDetailResponse responseDto = createMovieDetailDto(1L, "Test Movie");

		when(movieService.getAdminMovieBySlug(slug)).thenReturn(responseDto);

		ResponseEntity<MovieDetailResponse> response = movieController.getAdminMovieBySlug(slug);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Test Movie", response.getBody().getTitle());
		verify(movieService).getAdminMovieBySlug(slug);
	}

	@Test
	void getAdminMovieBySlug_WhenNotFound_ShouldThrowException() {
		String slug = "non-existent-movie";

		when(movieService.getAdminMovieBySlug(slug)).thenThrow(new MovieNotFoundException(slug));

		assertThrows(MovieNotFoundException.class, () -> movieController.getAdminMovieBySlug(slug));
		verify(movieService).getAdminMovieBySlug(slug);
	}

	@Test
	void searchMoviesForSession_WithSearchTerm_ShouldReturnMovies() {
		String searchTerm = "movie";
		List<MovieSessionSearchResponse> movies = List.of(
				MovieSessionSearchResponse.builder().id(1L).title("Movie 1").build(),
				MovieSessionSearchResponse.builder().id(2L).title("Movie 2").build());

		when(movieService.searchMoviesForSession(searchTerm)).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController.searchMoviesForSession(searchTerm);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		verify(movieService).searchMoviesForSession(searchTerm);
	}

	@Test
	void searchMoviesForSession_WithoutSearchTerm_ShouldReturnAllMovies() {
		List<MovieSessionSearchResponse> movies = List.of(
				MovieSessionSearchResponse.builder().id(1L).title("Movie 1").build(),
				MovieSessionSearchResponse.builder().id(2L).title("Movie 2").build());

		when(movieService.searchMoviesForSession(null)).thenReturn(movies);

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController.searchMoviesForSession(null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		verify(movieService).searchMoviesForSession(null);
	}

	@Test
	void searchMoviesForSession_WhenNoResults_ShouldReturnEmptyList() {
		when(movieService.searchMoviesForSession("nonexistent")).thenReturn(List.of());

		ResponseEntity<List<MovieSessionSearchResponse>> response = movieController
				.searchMoviesForSession("nonexistent");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(movieService).searchMoviesForSession("nonexistent");
	}
}