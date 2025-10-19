package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;
import ua.lviv.bas.cinema.service.SeatService;

@WebMvcTest(SeatController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private SeatService seatService;

	@Test
	void getSeatsByHall_ShouldReturnOk() throws Exception {
		List<SeatDto> seats = Arrays.asList(
				SeatDto.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build(),
				SeatDto.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).build());

		when(seatService.getSeatsByHallId(1L)).thenReturn(seats);

		mockMvc.perform(get("/api/seats/hall/1")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].row").value(1)).andExpect(jsonPath("$[1].row").value(1));
	}

	@Test
	void getSeatById_WhenExists_ShouldReturnOk() throws Exception {
		SeatDto seatDto = SeatDto.builder().id(1L).row(2).number(3).seatType(SeatType.STANDARD).build();

		when(seatService.getSeatById(1L)).thenReturn(seatDto);

		mockMvc.perform(get("/api/seats/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.row").value(2));
	}

	@Test
	void getSeatById_WhenNotExists_ShouldReturnNotFound() throws Exception {
		when(seatService.getSeatById(1L)).thenThrow(new EntityNotFoundException());

		mockMvc.perform(get("/api/seats/1")).andExpect(status().isNotFound());
	}

	@Test
	void createSeat_ShouldReturnCreated() throws Exception {
		SeatCreateDto createDto = SeatCreateDto.builder().row(1).number(5).seatType(SeatType.VIP).build();

		SeatDto responseDto = SeatDto.builder().id(1L).row(1).number(5).seatType(SeatType.VIP).build();

		when(seatService.createSeat(any())).thenReturn(responseDto);

		mockMvc.perform(post("/api/seats").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.seatType").value("VIP"));
	}

	@Test
	void updateSeat_WhenExists_ShouldReturnOk() throws Exception {
		SeatCreateDto updateDto = SeatCreateDto.builder().row(2).number(10).seatType(SeatType.STANDARD).build();

		SeatDto responseDto = SeatDto.builder().id(1L).row(2).number(10).seatType(SeatType.STANDARD).build();

		when(seatService.updateSeat(eq(1L), any())).thenReturn(responseDto);

		mockMvc.perform(put("/api/seats/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.row").value(2)).andExpect(jsonPath("$.number").value(10));
	}

	@Test
	void deleteSeat_WhenExists_ShouldReturnNoContent() throws Exception {
		doNothing().when(seatService).deleteSeat(1L);

		mockMvc.perform(delete("/api/seats/1")).andExpect(status().isNoContent());
	}

	@Test
	void getAvailableSeats_ShouldReturnOk() throws Exception {
		List<SeatDto> availableSeats = Arrays.asList(SeatDto.builder().id(1L).row(1).number(1).available(true).build());

		when(seatService.getAvailableSeatsForSession(1L, 1L)).thenReturn(availableSeats);

		mockMvc.perform(get("/api/seats/availability").param("hallId", "1").param("sessionId", "1"))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].available").value(true));
	}

	@Test
	void checkSeatAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
		when(seatService.isSeatAvailable(1L, 1L)).thenReturn(true);

		mockMvc.perform(get("/api/seats/1/availability").param("sessionId", "1")).andExpect(status().isOk())
				.andExpect(content().string("true"));
	}

	@Test
	void getAvailableSeatsCount_ShouldReturnCount() throws Exception {
		when(seatService.getAvailableSeatsCountForSession(1L, 1L)).thenReturn(25);

		mockMvc.perform(get("/api/seats/count/available").param("hallId", "1").param("sessionId", "1"))
				.andExpect(status().isOk()).andExpect(content().string("25"));
	}
}