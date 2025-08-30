package ua.lviv.bas.cinema.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.service.MovieService;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MovieService movieService;

	@Test
	@WithMockUser
	void getMovie_ShouldReturn200() throws Exception {
		MovieDto movieDto = MovieDto.builder().id(1L).title("Test Movie").build();

		when(movieService.getMovieById(1L)).thenReturn(movieDto);

		mockMvc.perform(get("/api/movies/1")).andExpect(status().isOk());
	}

	@Test
	void getMovie_WithoutAuth_ShouldReturn401() throws Exception {
		mockMvc.perform(get("/api/movies/1")).andExpect(status().isUnauthorized());
	}
}