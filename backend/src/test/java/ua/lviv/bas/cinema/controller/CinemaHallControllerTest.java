package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.CinemaHallDto;
import ua.lviv.bas.cinema.dto.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.CinemaHallWithSeatsDto;
import ua.lviv.bas.cinema.dto.HallLayoutDto;
import ua.lviv.bas.cinema.dto.SeatDto;
import ua.lviv.bas.cinema.dto.SeatLayoutRequest;
import ua.lviv.bas.cinema.dto.SeatRowDto;
import ua.lviv.bas.cinema.service.CinemaHallService;

@ExtendWith(MockitoExtension.class)
class CinemaHallControllerTest {

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private CinemaHallController cinemaHallController;

	private CinemaHallDto createCinemaHallDto(Long id, String name, int capacity) {
		return CinemaHallDto.builder().id(id).name(name).capacity(capacity).build();
	}

	private CinemaHallRequest createCinemaHallRequest(String name) {
		return CinemaHallRequest.builder().name(name).build();
	}

	private CinemaHallWithSeatsDto createCinemaHallWithSeatsDto(Long id, String name, int capacity,
			List<SeatDto> seats) {
		return CinemaHallWithSeatsDto.builder().id(id).name(name).capacity(capacity).seats(seats).build();
	}

	private SeatLayoutRequest createSeatLayoutRequest(int rows, int seatsPerRow, SeatType seatType) {
		return SeatLayoutRequest.builder().rows(rows).seatsPerRow(seatsPerRow).defaultSeatType(seatType).build();
	}

	private SeatDto createSeatDto(Long id, int row, int number, SeatType seatType) {
		return SeatDto.builder().id(id).row(row).number(number).seatType(seatType).build();
	}

	private HallLayoutDto createHallLayoutDto(Long hallId, String hallName, int totalRows, int maxSeatsPerRow,
			int totalSeats) {
		return HallLayoutDto.builder().hallId(hallId).hallName(hallName).totalRows(totalRows)
				.maxSeatsPerRow(maxSeatsPerRow).totalSeats(totalSeats).build();
	}

	@Test
	void createHall_ShouldReturnCreatedHall() {
		CinemaHallRequest request = createCinemaHallRequest("Hall A");
		CinemaHallDto responseDto = createCinemaHallDto(1L, "Hall A", 0);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(responseDto);

		ResponseEntity<CinemaHallDto> response = cinemaHallController.createHall(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		CinemaHallDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Hall A", responseBody.getName());
		verify(cinemaHallService).createHall(request);
	}

	@Test
	void getHallById_ShouldReturnHall() {
		CinemaHallDto hallDto = createCinemaHallDto(1L, "Hall A", 50);

		when(cinemaHallService.getHallById(1L)).thenReturn(hallDto);

		ResponseEntity<CinemaHallDto> response = cinemaHallController.getHallById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Hall A", responseBody.getName());
		assertEquals(50, responseBody.getCapacity());
		verify(cinemaHallService).getHallById(1L);
	}

	@Test
	void updateHall_ShouldReturnUpdatedHall() {
		CinemaHallRequest request = createCinemaHallRequest("Updated Hall");
		CinemaHallDto updatedDto = createCinemaHallDto(1L, "Updated Hall", 60);

		when(cinemaHallService.updateHall(eq(1L), any(CinemaHallRequest.class))).thenReturn(updatedDto);

		ResponseEntity<CinemaHallDto> response = cinemaHallController.updateHall(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Updated Hall", responseBody.getName());
		verify(cinemaHallService).updateHall(1L, request);
	}

	@Test
	void deleteHall_ShouldReturnNoContent() {
		ResponseEntity<Void> response = cinemaHallController.deleteHall(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(cinemaHallService).deleteHall(1L);
	}

	@Test
	void getAllHalls_ShouldReturnListOfHalls() {
		CinemaHallDto hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallDto hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallDto> halls = List.of(hall1, hall2);

		when(cinemaHallService.getAllHalls()).thenReturn(halls);

		ResponseEntity<List<CinemaHallDto>> response = cinemaHallController.getAllHalls();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<CinemaHallDto> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(2L, responseBody.get(1).getId());
		verify(cinemaHallService).getAllHalls();
	}

	@Test
	void generateSeats_ShouldReturnHallWithSeats() {
		SeatLayoutRequest seatRequest = createSeatLayoutRequest(5, 10, SeatType.STANDARD);
		SeatDto seat1 = createSeatDto(1L, 1, 1, SeatType.STANDARD);
		SeatDto seat2 = createSeatDto(2L, 1, 2, SeatType.VIP);
		List<SeatDto> seats = List.of(seat1, seat2);
		CinemaHallWithSeatsDto hallWithSeats = createCinemaHallWithSeatsDto(1L, "Hall A", 50, seats);

		when(cinemaHallService.generateSeats(eq(1L), any(SeatLayoutRequest.class))).thenReturn(hallWithSeats);

		ResponseEntity<CinemaHallWithSeatsDto> response = cinemaHallController.generateSeats(1L, seatRequest);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallWithSeatsDto responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Hall A", responseBody.getName());
		assertEquals(2, responseBody.getSeats().size());
		verify(cinemaHallService).generateSeats(1L, seatRequest);
	}

	@Test
	void getHallWithSeats_ShouldReturnHallWithSeats() {
		SeatDto seat1 = createSeatDto(1L, 1, 1, SeatType.STANDARD);
		SeatDto seat2 = createSeatDto(2L, 1, 2, SeatType.VIP);
		List<SeatDto> seats = List.of(seat1, seat2);
		CinemaHallWithSeatsDto hallWithSeats = createCinemaHallWithSeatsDto(1L, "Hall A", 50, seats);

		when(cinemaHallService.getHallWithSeats(1L)).thenReturn(hallWithSeats);

		ResponseEntity<CinemaHallWithSeatsDto> response = cinemaHallController.getHallWithSeats(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallWithSeatsDto responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals(2, responseBody.getSeats().size());
		verify(cinemaHallService).getHallWithSeats(1L);
	}

	@Test
	void getHallLayout_ShouldReturnHallLayout() {
		HallLayoutDto layoutDto = createHallLayoutDto(1L, "Hall A", 5, 10, 50);

		when(cinemaHallService.getHallLayout(1L)).thenReturn(layoutDto);

		ResponseEntity<HallLayoutDto> response = cinemaHallController.getHallLayout(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		HallLayoutDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getHallId());
		assertEquals("Hall A", responseBody.getHallName());
		assertEquals(5, responseBody.getTotalRows());
		assertEquals(10, responseBody.getMaxSeatsPerRow());
		assertEquals(50, responseBody.getTotalSeats());
		verify(cinemaHallService).getHallLayout(1L);
	}

	@Test
	void searchHalls_WithName_ShouldReturnFilteredHalls() {
		CinemaHallDto hall1 = createCinemaHallDto(1L, "Main Hall", 50);
		List<CinemaHallDto> halls = List.of(hall1);

		when(cinemaHallService.searchHalls("Main")).thenReturn(halls);

		ResponseEntity<List<CinemaHallDto>> response = cinemaHallController.searchHalls("Main");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<CinemaHallDto> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals("Main Hall", responseBody.get(0).getName());
		verify(cinemaHallService).searchHalls("Main");
	}

	@Test
	void searchHalls_WithNullName_ShouldReturnAllHalls() {
		CinemaHallDto hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallDto hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallDto> halls = List.of(hall1, hall2);

		when(cinemaHallService.searchHalls(null)).thenReturn(halls);

		ResponseEntity<List<CinemaHallDto>> response = cinemaHallController.searchHalls(null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<CinemaHallDto> responseBody = Objects.requireNonNull(response.getBody(),
				"Response body should not be null");
		assertEquals(2, responseBody.size());
		verify(cinemaHallService).searchHalls(null);
	}
}