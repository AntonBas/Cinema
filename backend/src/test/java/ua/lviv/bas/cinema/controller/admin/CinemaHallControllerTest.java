package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
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

	private CinemaHallRequest createCinemaHallRequest(String name) {
		return CinemaHallRequest.builder().name(name).build();
	}

	private CinemaHallRequest createCinemaHallRequestWithSeats(String name, Integer rows, Integer seatsPerRow,
			SeatType seatType) {
		return CinemaHallRequest.builder().name(name).rows(rows).seatsPerRow(seatsPerRow).defaultSeatType(seatType)
				.build();
	}

	@Test
	void createHall_ShouldReturnCreatedHall() {
		CinemaHallRequest request = createCinemaHallRequest(HALL_NAME);
		CinemaHallResponse responseDto = createCinemaHallDto(HALL_ID, HALL_NAME, CAPACITY);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(responseDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.createHall(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		CinemaHallResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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

		CinemaHallResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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
	void updateHall_ShouldReturnUpdatedHall() {
		CinemaHallRequest request = createCinemaHallRequest("Updated Hall");
		CinemaHallResponse updatedDto = createCinemaHallDto(HALL_ID, "Updated Hall", 60);

		when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(updatedDto);

		ResponseEntity<CinemaHallResponse> response = cinemaHallController.updateHall(HALL_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		CinemaHallResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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

		CinemaHallResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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
}