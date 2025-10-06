package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.MovieDto;
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

	private MovieDto testMovieDto;

	@BeforeEach
	void setUp() {
		testMovieDto = createTestMovieDto();
	}

	@Test
	void getAllMovies_ShouldReturnMovies() throws Exception {
		when(movieService.getAllMovies()).thenReturn(List.of(testMovieDto));

		mockMvc.perform(get("/api/movies")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].title").value("Test Movie"))
				.andExpect(jsonPath("$[0].slug").value("test-movie"));
	}

	@Test
	void getMovieById_ShouldReturnMovie() throws Exception {
		when(movieService.getMovieById(1L)).thenReturn(testMovieDto);

		mockMvc.perform(get("/api/movies/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() throws Exception {
		when(movieService.getMovieBySlug("test-movie")).thenReturn(testMovieDto);

		mockMvc.perform(get("/api/movies/slug/test-movie")).andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("test-movie"));
	}

	@Test
	void getPaginatedMovies_ShouldReturnPage() throws Exception {
		Page<MovieDto> page = new PageImpl<>(List.of(testMovieDto), PageRequest.of(0, 10), 1);
		when(movieService.getPaginatedMovies(any())).thenReturn(page);

		mockMvc.perform(get("/api/movies/page").param("page", "0").param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].title").value("Test Movie"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void getMoviesByStatus_ShouldReturnMovies() throws Exception {
		when(movieService.getMoviesByStatus("UPCOMING")).thenReturn(List.of(testMovieDto));

		mockMvc.perform(get("/api/movies/status/UPCOMING")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("UPCOMING"));
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnMovies() throws Exception {
		when(movieService.getCurrentlyShowingMovies()).thenReturn(List.of(testMovieDto));

		mockMvc.perform(get("/api/movies/current")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getUpcomingMovies_ShouldReturnMovies() throws Exception {
		when(movieService.getUpcomingMovies()).thenReturn(List.of(testMovieDto));

		mockMvc.perform(get("/api/movies/upcoming")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void getMoviesByGenre_ShouldReturnMovies() throws Exception {
		when(movieService.getMoviesByGenre(1L)).thenReturn(List.of(testMovieDto));

		mockMvc.perform(get("/api/movies/genre/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Movie"));
	}

	@Test
	void createMovie_ShouldCreateMovie() throws Exception {
		when(movieService.createMovie(any())).thenReturn(testMovieDto);

		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes());

		mockMvc.perform(multipart("/api/movies").file(posterFile).param("title", "New Movie").param("slug", "new-movie")
				.param("trailerUrl", "https://example.com/trailer").param("description", "Test Description")
				.param("durationMinutes", "120").param("releaseDate", LocalDate.now().plusDays(1).toString())
				.param("endShowingDate", LocalDate.now().plusDays(30).toString()).param("status", "UPCOMING")
				.param("ageRating", "PEGI_12").param("castIds", "1,2").param("directorIds", "3")
				.param("screenwriterIds", "4").param("genreIds", "1,2").contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void updateMovie_ShouldUpdateMovie() throws Exception {
		when(movieService.updateMovie(anyLong(), any(MovieDto.class))).thenReturn(testMovieDto);

		mockMvc.perform(put("/api/movies/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testMovieDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void updateMovieWithPoster_ShouldUpdateMovieWithPoster() throws Exception {
		when(movieService.updateMovieWithPoster(anyLong(), any(MovieDto.class), any())).thenReturn(testMovieDto);

		MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes());

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/movies/1/poster").file(posterFile).param("id", "1")
				.param("title", "Updated Movie").param("slug", "updated-movie")
				.param("trailerUrl", "https://example.com/trailer").param("description", "Updated Description")
				.param("durationMinutes", "130").param("releaseDate", LocalDate.now().plusDays(2).toString())
				.param("endShowingDate", LocalDate.now().plusDays(35).toString()).param("status", "UPCOMING")
				.param("ageRating", "PEGI_12").param("castIds", "1,2").param("directorIds", "3")
				.param("screenwriterIds", "4").param("genreIds", "1,2").with(request -> {
					request.setMethod("PUT");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Test Movie"));
	}

	@Test
	void deleteMovie_ShouldDeleteMovie() throws Exception {
		mockMvc.perform(delete("/api/movies/1")).andExpect(status().isNoContent());
	}

	@Test
	void getMoviePoster_ShouldReturnPoster() throws Exception {
		byte[] posterBytes = "test image content".getBytes();
		when(movieService.getMoviePoster(1L)).thenReturn(org.springframework.http.ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.IMAGE_JPEG).body(posterBytes));

		mockMvc.perform(get("/api/movies/1/poster")).andExpect(status().isOk()).andExpect(content().bytes(posterBytes));
	}

	@Test
	void getMoviePoster_ShouldReturnNotFound() throws Exception {
		when(movieService.getMoviePoster(1L)).thenReturn(org.springframework.http.ResponseEntity.notFound().build());

		mockMvc.perform(get("/api/movies/1/poster")).andExpect(status().isNotFound());
	}

	private MovieDto createTestMovieDto() {
		return MovieDto.builder().id(1L).title("Test Movie").slug("test-movie")
				.trailerUrl("https://example.com/trailer").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12).castIds(List.of(1L, 2L))
				.directorIds(List.of(3L)).screenwriterIds(List.of(4L)).genreIds(List.of(1L, 2L)).build();
	}
}