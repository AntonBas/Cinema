package ua.lviv.bas.cinema.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.service.GenreService;

@WebMvcTest(GenreController.class)
@Import(TestSecurityConfig.class)
public class GenreControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GenreService genreService;

	@Test
	void getAllGenres_ShouldReturnGenres() throws Exception {
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();
		when(genreService.getAllGenres()).thenReturn(List.of(dto));

		mockMvc.perform(get("/api/genres")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Action"));
	}

	@Test
	void getGenreById_WhenExists_ShouldReturnGenre() throws Exception {
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();
		when(genreService.readGenre(1L)).thenReturn(dto);

		mockMvc.perform(get("/api/genres/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Action"));
	}

	@Test
	void getGenreById_WhenNotExists_ShouldReturn404() throws Exception {
		when(genreService.readGenre(999L)).thenReturn(null);

		mockMvc.perform(get("/api/genres/999")).andExpect(status().isNotFound());
	}

	@Test
	void createGenre_ShouldReturnCreatedGenre() throws Exception {
		GenreDto requestDto = GenreDto.builder().name("Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Action").build();

		when(genreService.createGenre(requestDto)).thenReturn(responseDto);

		mockMvc.perform(post("/api/genres").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Action"));
	}

	@Test
	void updateGenre_ShouldReturnUpdatedGenre() throws Exception {
		GenreDto requestDto = GenreDto.builder().name("Updated Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Updated Action").build();

		when(genreService.updateGenre(1L, requestDto)).thenReturn(responseDto);

		mockMvc.perform(put("/api/genres/1").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Action"));
	}

	@Test
	void deleteGenre_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/genres/1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isNoContent());
	}
}