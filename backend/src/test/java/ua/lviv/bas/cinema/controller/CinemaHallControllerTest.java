package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.service.CinemaHallService;

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

	private CinemaHallRequest createCinemaHallRequest(String name) {
		return CinemaHallRequest.builder().name(name).build();
	}

	private CinemaHallRequest createCinemaHallRequestWithSeats(String name, Integer rows, Integer seatsPerRow,
			SeatType seatType) {
		return CinemaHallRequest.builder().name(name).rows(rows).seatsPerRow(seatsPerRow).defaultSeatType(seatType)
				.build();
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
	void createHall_ShouldReturnCreatedHall() {
		CinemaHallRequest request = createCinemaHallRequest(HALL_NAME);
		CinemaHallResponse responseDto = createCinemaHallDto(HALL_ID, HALL_NAME, CAPACITY);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(responseDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.createHall(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		CinemaHallResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(HALL_ID, responseBody.getId());
		assertEquals(HALL_NAME, responseBody.getName());
		verify(cinemaHallService).createHall(request);
	}

	@Test
	void createHall_WithSeats_ShouldReturnCreatedHallWithSeats() {
		CinemaHallRequest request = createCinemaHallRequestWithSeats("Hall With Seats", 5, 10, SeatType.STANDARD);
		CinemaHallResponse responseDto = createCinemaHallDto(HALL_ID, "Hall With Seats", 50);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(responseDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.createHall(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		CinemaHallResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(HALL_ID, responseBody.getId());
		assertEquals("Hall With Seats", responseBody.getName());
		verify(cinemaHallService).createHall(request);
	}

	@Test
	void createHall_WhenDuplicateName_ShouldThrowException() {
		CinemaHallRequest request = createCinemaHallRequest("Existing Hall");

		when(cinemaHallService.createHall(any(CinemaHallRequest.class)))
				.thenThrow(new DuplicateEntityException("CinemaHall", "Existing Hall"));

		assertThrows(DuplicateEntityException.class, () -> cinemaHallController.createHall(request));
	}

	@Test
	void getHallById_ShouldReturnHall() {
		CinemaHallResponse hallDto = createCinemaHallDto(HALL_ID, HALL_NAME, CAPACITY);

		when(cinemaHallService.getHallById(HALL_ID)).thenReturn(hallDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.getHallById(HALL_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallResponse responseBody = Objects.requireNonNull(response.getBody());
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
	void updateHall_ShouldReturnUpdatedHall() {
		CinemaHallRequest request = createCinemaHallRequest("Updated Hall");
		CinemaHallResponse updatedDto = createCinemaHallDto(HALL_ID, "Updated Hall", 60);

		when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(updatedDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.updateHall(HALL_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(HALL_ID, responseBody.getId());
		assertEquals("Updated Hall", responseBody.getName());
		verify(cinemaHallService).updateHall(HALL_ID, request);
	}

	@Test
	void updateHall_WithSeats_ShouldReturnUpdatedHallWithNewSeats() {
		CinemaHallRequest request = createCinemaHallRequestWithSeats("Updated Hall", 6, 8, SeatType.VIP);
		CinemaHallResponse updatedDto = createCinemaHallDto(HALL_ID, "Updated Hall", 48);

		when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(updatedDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.updateHall(HALL_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		CinemaHallResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(HALL_ID, responseBody.getId());
		assertEquals("Updated Hall", responseBody.getName());
		assertEquals(48, responseBody.getCapacity());
		verify(cinemaHallService).updateHall(HALL_ID, request);
	}

	@Test
	void updateHall_WhenNotFound_ShouldThrowException() {
		CinemaHallRequest request = createCinemaHallRequest("Updated Hall");

		when(cinemaHallService.updateHall(eq(999L), any(CinemaHallRequest.class)))
				.thenThrow(new CinemaHallNotFoundException(999L));

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallController.updateHall(999L, request));
	}

	@Test
	void deleteHall_ShouldReturnNoContent() {
		ResponseEntity<Void> response = cinemaHallController.deleteHall(HALL_ID);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(cinemaHallService).deleteHall(HALL_ID);
	}

	@Test
	void deleteHall_WhenNotFound_ShouldThrowException() {
		Long nonExistentId = 999L;

		doThrow(new CinemaHallNotFoundException(nonExistentId)).when(cinemaHallService).deleteHall(nonExistentId);

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallController.deleteHall(nonExistentId));
	}

	@Test
	void getAllHalls_ShouldReturnListOfHalls() {
		CinemaHallResponse hall1 = createCinemaHallDto(1L, "Hall A", 50);
		CinemaHallResponse hall2 = createCinemaHallDto(2L, "Hall B", 60);
		List<CinemaHallResponse> halls = List.of(hall1, hall2);

		when(cinemaHallService.getAllHalls()).thenReturn(halls);

		ResponseEntity<List<CinemaHallResponse>> response = cinemaHallController.getAllHalls();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<CinemaHallResponse> responseBody = Objects.requireNonNull(response.getBody());
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
		CinemaHallWithSeatsResponse responseBody = Objects.requireNonNull(response.getBody());
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
		HallLayoutResponse responseBody = Objects.requireNonNull(response.getBody());
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
		List<CinemaHallResponse> responseBody = Objects.requireNonNull(response.getBody());
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
		List<CinemaHallResponse> responseBody = Objects.requireNonNull(response.getBody());
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
		List<CinemaHallResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(2, responseBody.size());
		verify(cinemaHallService).searchHalls("");
	}
}