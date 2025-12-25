package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.service.common.CinemaHallService;

@ExtendWith(MockitoExtension.class)
class CinemaHallControllerTest {

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private CinemaHallController cinemaHallController;

	private static final Long HALL_ID = 1L;
	private static final String HALL_NAME = "Test Hall";
	private static final int CAPACITY = 50;

	private CinemaHallResponse createCinemaHallDto(Long id, String name, int capacity) {
		return CinemaHallResponse.builder().id(id).name(name).capacity(capacity).build();
	}

	private CinemaHallWithSeatsResponse createCinemaHallWithSeatsDto(Long id, String name, int capacity,
			List<SeatResponse> seats) {
		return CinemaHallWithSeatsResponse.builder().id(id).name(name).capacity(capacity).seats(seats).build();
	}

	private SeatResponse createSeatDto(Long id, int row, int number, SeatType seatType) {
		return SeatResponse.builder().id(id).row(row).number(number).seatType(seatType).build();
	}

	private HallLayoutResponse createHallLayoutDto(Long hallId, String hallName, int totalRows, int maxSeatsPerRow,
			int totalSeats) {
		return HallLayoutResponse.builder().hallId(hallId).hallName(hallName).totalRows(totalRows)
				.maxSeatsPerRow(maxSeatsPerRow).totalSeats(totalSeats).build();
	}

	@Test
	void getHallById_ShouldReturnHall() {
		CinemaHallResponse hallDto = createCinemaHallDto(HALL_ID, HALL_NAME, CAPACITY);

		when(cinemaHallService.getHallById(HALL_ID)).thenReturn(hallDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.getHallById(HALL_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		CinemaHallResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(HALL_ID, responseBody.getId());
		assertEquals(HALL_NAME, responseBody.getName());
		assertEquals(CAPACITY, responseBody.getCapacity());
		verify(cinemaHallService).getHallById(HALL_ID);
	}

	@Test
	void getHallById_WhenNotFound_ShouldThrowException() {
		when(cinemaHallService.getHallById(999L)).thenThrow(new CinemaHallNotFoundException(999L));

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallController.getHallById(999L));
	}

	@Test
	void getAllHalls_ShouldReturnListOfHalls() {
		CinemaHallResponse hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallResponse hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallResponse> halls = List.of(hall1, hall2);

		when(cinemaHallService.getAllHalls()).thenReturn(halls);

		ResponseEntity<List<CinemaHallResponse>> response = cinemaHallController.getAllHalls();

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<CinemaHallResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(2L, responseBody.get(1).getId());
		verify(cinemaHallService).getAllHalls();
	}

	@Test
	void getHallWithSeats_ShouldReturnHallWithSeats() {
		SeatResponse seat1 = createSeatDto(1L, 1, 1, SeatType.STANDARD);
		SeatResponse seat2 = createSeatDto(2L, 1, 2, SeatType.VIP);
		List<SeatResponse> seats = List.of(seat1, seat2);
		CinemaHallWithSeatsResponse hallWithSeats = createCinemaHallWithSeatsDto(HALL_ID, HALL_NAME, CAPACITY, seats);

		when(cinemaHallService.getHallWithSeats(HALL_ID)).thenReturn(hallWithSeats);

		ResponseEntity<CinemaHallWithSeatsResponse> response = cinemaHallController.getHallWithSeats(HALL_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		CinemaHallWithSeatsResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(HALL_ID, responseBody.getId());
		assertEquals(2, responseBody.getSeats().size());
		verify(cinemaHallService).getHallWithSeats(HALL_ID);
	}

	@Test
	void getHallLayout_ShouldReturnHallLayout() {
		HallLayoutResponse layoutDto = createHallLayoutDto(HALL_ID, HALL_NAME, 5, 10, CAPACITY);

		when(cinemaHallService.getHallLayout(HALL_ID)).thenReturn(layoutDto);

		ResponseEntity<HallLayoutResponse> response = cinemaHallController.getHallLayout(HALL_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		HallLayoutResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(HALL_ID, responseBody.getHallId());
		assertEquals(HALL_NAME, responseBody.getHallName());
		assertEquals(5, responseBody.getTotalRows());
		assertEquals(10, responseBody.getMaxSeatsPerRow());
		assertEquals(CAPACITY, responseBody.getTotalSeats());
		verify(cinemaHallService).getHallLayout(HALL_ID);
	}

	@Test
	void searchHalls_WithName_ShouldReturnFilteredHalls() {
		CinemaHallResponse hall1 = createCinemaHallDto(1L, "Main Hall", 50);
		List<CinemaHallResponse> halls = List.of(hall1);

		when(cinemaHallService.searchHalls("Main")).thenReturn(halls);

		ResponseEntity<List<CinemaHallResponse>> response = cinemaHallController.searchHalls("Main");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<CinemaHallResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.size());
		assertEquals("Main Hall", responseBody.get(0).getName());
		verify(cinemaHallService).searchHalls("Main");
	}

	@Test
	void searchHalls_WithNullName_ShouldReturnAllHalls() {
		CinemaHallResponse hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallResponse hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallResponse> halls = List.of(hall1, hall2);

		when(cinemaHallService.searchHalls(null)).thenReturn(halls);

		ResponseEntity<List<CinemaHallResponse>> response = cinemaHallController.searchHalls(null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<CinemaHallResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		verify(cinemaHallService).searchHalls(null);
	}

	@Test
	void searchHalls_WithEmptyName_ShouldReturnAllHalls() {
		CinemaHallResponse hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallResponse hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallResponse> halls = List.of(hall1, hall2);

		when(cinemaHallService.searchHalls("")).thenReturn(halls);

		ResponseEntity<List<CinemaHallResponse>> response = cinemaHallController.searchHalls("");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<CinemaHallResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		verify(cinemaHallService).searchHalls("");
	}
}