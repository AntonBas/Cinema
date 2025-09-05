package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;
import ua.lviv.bas.cinema.service.CinemaHallService;

@WebMvcTest(CinemaHallController.class)
@Import(TestSecurityConfig.class)
public class CinemaHallControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CinemaHallService cinemaHallService;

	@Test
	void createHall_ShouldReturnCreated() throws Exception {
		CinemaHallCreateDto createDto = CinemaHallCreateDto.builder().name("Test Hall").rows(5).seatsPerRow(10).build();

		CinemaHallResponseDto responseDto = CinemaHallResponseDto.builder().id(1L).name("Test Hall").capacity(50)
				.build();

		when(cinemaHallService.createHall(any())).thenReturn(responseDto);

		mockMvc.perform(post("/api/halls").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Test Hall"));
	}

	@Test
	void createHall_WithInvalidData_ShouldReturnBadRequest() throws Exception {
		CinemaHallCreateDto invalidDto = CinemaHallCreateDto.builder().name("").rows(0).seatsPerRow(31).build();

		mockMvc.perform(post("/api/halls").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto))).andExpect(status().isBadRequest());
	}

	@Test
	void getHallById_WhenExists_ShouldReturnOk() throws Exception {
		CinemaHallResponseDto responseDto = CinemaHallResponseDto.builder().id(1L).name("Test Hall").capacity(50)
				.build();

		when(cinemaHallService.getHallById(1L)).thenReturn(responseDto);

		mockMvc.perform(get("/api/halls/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Test Hall"));
	}

	@Test
	void getHallById_WhenNotExists_ShouldReturnNotFound() throws Exception {
		when(cinemaHallService.getHallById(1L)).thenThrow(new EntityNotFoundException());

		mockMvc.perform(get("/api/halls/1")).andExpect(status().isNotFound());
	}

	@Test
	void updateHall_WhenExists_ShouldReturnOk() throws Exception {
		CinemaHallCreateDto updateDto = CinemaHallCreateDto.builder().name("Updated Hall").rows(6).seatsPerRow(8)
				.build();

		CinemaHallResponseDto responseDto = CinemaHallResponseDto.builder().id(1L).name("Updated Hall").capacity(48)
				.build();

		when(cinemaHallService.updateHall(eq(1L), any())).thenReturn(responseDto);

		mockMvc.perform(put("/api/halls/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Hall"));
	}

	@Test
	void updateHall_WhenNotExists_ShouldReturnNotFound() throws Exception {
		CinemaHallCreateDto updateDto = CinemaHallCreateDto.builder().name("Updated Hall").rows(6).seatsPerRow(8)
				.build();

		when(cinemaHallService.updateHall(eq(1L), any())).thenThrow(new EntityNotFoundException());

		mockMvc.perform(put("/api/halls/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isNotFound());
	}

	@Test
	void deleteHall_WhenExists_ShouldReturnNoContent() throws Exception {
		doNothing().when(cinemaHallService).deleteHall(1L);

		mockMvc.perform(delete("/api/halls/1")).andExpect(status().isNoContent());
	}

	@Test
	void deleteHall_WhenNotExists_ShouldReturnNotFound() throws Exception {
		doThrow(new EntityNotFoundException()).when(cinemaHallService).deleteHall(1L);

		mockMvc.perform(delete("/api/halls/1")).andExpect(status().isNotFound());
	}

	@Test
	void getAllHalls_ShouldReturnOk() throws Exception {
		List<CinemaHallResponseDto> halls = Arrays.asList(
				CinemaHallResponseDto.builder().id(1L).name("Hall 1").capacity(50).build(),
				CinemaHallResponseDto.builder().id(2L).name("Hall 2").capacity(40).build());

		when(cinemaHallService.getAllHalls()).thenReturn(halls);

		mockMvc.perform(get("/api/halls")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Hall 1")).andExpect(jsonPath("$[1].name").value("Hall 2"));
	}

	@Test
	void checkHallExists_WhenExists_ShouldReturnTrue() throws Exception {
		when(cinemaHallService.existsById(1L)).thenReturn(true);

		mockMvc.perform(get("/api/halls/1/exists")).andExpect(status().isOk()).andExpect(content().string("true"));
	}

	@Test
	void checkHallExists_WhenNotExists_ShouldReturnFalse() throws Exception {
		when(cinemaHallService.existsById(1L)).thenReturn(false);

		mockMvc.perform(get("/api/halls/1/exists")).andExpect(status().isOk()).andExpect(content().string("false"));
	}
}