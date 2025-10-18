package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.dto.MovieResponse;
import ua.lviv.bas.cinema.dto.MovieUpdateRequest;
import ua.lviv.bas.cinema.service.MovieService;

@WebMvcTest(MovieController.class)
@Import(TestSecurityConfig.class)
class MovieControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MovieService movieService;

	private MovieDto movieDto;
	private MovieResponse movieResponse;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;

	@BeforeEach
	void setUp() {
		movieDto = MovieDto.builder().id(1L).title("Test Movie").slug("test-movie").posterUrl("/api/movies/1/poster")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(MovieStatus.UPCOMING).currentlyShowing(false).build();

		movieResponse = MovieResponse.builder().id(1L).title("Test Movie").slug("test-movie")
				.posterUrl("/api/movies/1/poster").durationMinutes(120).ageRating(AgeRating.PEGI_12)
				.releaseDate(LocalDate.now().plusDays(1)).status(MovieStatus.UPCOMING).currentlyShowing(false).build();

		createRequest = MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/trailer")
				.description("Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L))
				.castIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Movie").trailerUrl("https://example.com/trailer")
				.description("Updated Description").durationMinutes(130).releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(35)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L, 2L))
				.castIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).build();
	}

	@Test
	void getById_ShouldReturnMovie() throws Exception {
		when(movieService.getById(1L)).thenReturn(movieDto);

		mockMvc.perform(get("/api/movies/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void getBySlug_ShouldReturnMovie() throws Exception {
		when(movieService.getBySlug("test-movie")).thenReturn(movieDto);

		mockMvc.perform(get("/api/movies/slug/test-movie")).andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("test-movie"));
	}

	@Test
	void getAll_ShouldReturnMovies() throws Exception {
		when(movieService.getAll()).thenReturn(List.of(movieDto));

		mockMvc.perform(get("/api/movies")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getPaginated_ShouldReturnPage() throws Exception {
		Page<MovieDto> page = new PageImpl<>(List.of(movieDto), PageRequest.of(0, 10), 1);
		when(movieService.getPaginated(any(PageRequest.class))).thenReturn(page);

		mockMvc.perform(get("/api/movies/paginated").param("page", "0").param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].title").value("Test Movie"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void create_ShouldReturnCreatedMovie() throws Exception {
		when(movieService.create(any(MovieCreateRequest.class))).thenReturn(movieDto);

		mockMvc.perform(post("/api/movies").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void update_ShouldReturnUpdatedMovie() throws Exception {
		when(movieService.update(eq(1L), any(MovieUpdateRequest.class))).thenReturn(movieDto);

		mockMvc.perform(put("/api/movies/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void delete_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/movies/1")).andExpect(status().isNoContent());

		verify(movieService).delete(1L);
	}

	@Test
	void getCurrentlyShowing_ShouldReturnMovies() throws Exception {
		when(movieService.getCurrentlyShowing()).thenReturn(List.of(movieResponse));

		mockMvc.perform(get("/api/movies/status/current")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getUpcoming_ShouldReturnMovies() throws Exception {
		when(movieService.getUpcoming()).thenReturn(List.of(movieResponse));

		mockMvc.perform(get("/api/movies/status/upcoming")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getArchived_ShouldReturnMovies() throws Exception {
		when(movieService.getArchived()).thenReturn(List.of(movieResponse));

		mockMvc.perform(get("/api/movies/status/archived")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getPoster_ShouldReturnImageBytes() throws Exception {
		byte[] image = "poster".getBytes();
		when(movieService.getPoster(1L)).thenReturn(ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image));

		mockMvc.perform(get("/api/movies/1/poster")).andExpect(status().isOk()).andExpect(content().bytes(image));
	}

	@Test
	void getMoviesForSessions_ShouldReturnMergedMovies() throws Exception {
		when(movieService.getCurrentlyShowing()).thenReturn(List.of(movieResponse));
		when(movieService.getUpcoming()).thenReturn(List.of(movieResponse));

		mockMvc.perform(get("/api/movies/for-sessions")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"))
				.andExpect(jsonPath("$[1].title").value("Test Movie"));
	}

	@Test
	void getPaginated_ShouldLimitSizeTo50() throws Exception {
		Page<MovieDto> page = new PageImpl<>(List.of(movieDto), PageRequest.of(0, 50), 1);
		when(movieService.getPaginated(any(PageRequest.class))).thenReturn(page);

		mockMvc.perform(get("/api/movies/paginated").param("page", "0").param("size", "100")).andExpect(status().isOk())
				.andExpect(jsonPath("$.pageSize").value(50));
	}
}