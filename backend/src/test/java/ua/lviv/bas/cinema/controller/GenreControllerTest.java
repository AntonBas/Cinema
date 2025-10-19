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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.dto.GenreRequest;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.service.GenreService;

@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GenreControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GenreService genreService;

	@Test
	void getGenreById_WhenExists_ShouldReturnGenre() throws Exception {
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();
		when(genreService.getGenreById(1L)).thenReturn(dto);

		mockMvc.perform(get("/api/genres/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Action"));
	}

	@Test
	void createGenre_ShouldReturnCreatedGenre() throws Exception {
		GenreRequest request = new GenreRequest("Action");
		GenreDto response = GenreDto.builder().id(1L).name("Action").build();

		when(genreService.createGenre(request)).thenReturn(response);

		mockMvc.perform(post("/api/genres").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Action"));
	}

	@Test
	void updateGenre_ShouldReturnUpdatedGenre() throws Exception {
		GenreRequest request = new GenreRequest("Updated Action");
		GenreDto response = GenreDto.builder().id(1L).name("Updated Action").build();

		when(genreService.updateGenre(1L, request)).thenReturn(response);

		mockMvc.perform(put("/api/genres/1").with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Updated Action"));
	}

	@Test
	void deleteGenre_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/genres/1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	void searchGenres_ShouldReturnPagedResponse() throws Exception {
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();
		PageResponse<GenreDto> response = new PageResponse<>(List.of(dto), 0, 1, 1, 10);

		when(genreService.searchGenres(null, 0, 10)).thenReturn(response);

		mockMvc.perform(get("/api/genres").param("page", "0").param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Action")).andExpect(jsonPath("$.currentPage").value(0))
				.andExpect(jsonPath("$.totalPages").value(1)).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.pageSize").value(10));
	}
}
