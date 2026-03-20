package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.service.cinema.SeatService;

@ExtendWith(MockitoExtension.class)
public class AdminSeatControllerTest {

	@Mock
	private SeatService seatService;

	@InjectMocks
	private AdminSeatController seatController;

	private SeatResponse createSeatDto(Long id, int row, int number, SeatType seatType, boolean active) {
		return new SeatResponse(id, row, number, seatType, active);
	}

	@Test
	void updateSeatType_ShouldReturnUpdatedSeat() {
		SeatResponse updatedSeat = createSeatDto(1L, 1, 1, SeatType.VIP, true);

		when(seatService.updateSeatType(1L, SeatType.VIP)).thenReturn(updatedSeat);

		ResponseEntity<SeatResponse> response = seatController.updateSeatType(1L, 1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(SeatType.VIP, responseBody.seatType());
		assertEquals(1L, responseBody.id());
		assertEquals(true, responseBody.active());

		verify(seatService).updateSeatType(1L, SeatType.VIP);
	}

	@Test
	void updateSeatType_WhenNotFound_ShouldThrowException() {
		when(seatService.updateSeatType(999L, SeatType.VIP)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.updateSeatType(1L, 999L, SeatType.VIP));

		verify(seatService).updateSeatType(999L, SeatType.VIP);
	}

	@Test
	void setSeatActiveStatus_ShouldReturnActivatedSeat() {
		SeatResponse activatedSeat = createSeatDto(1L, 1, 1, SeatType.STANDARD, true);

		when(seatService.setSeatActiveStatus(1L, true)).thenReturn(activatedSeat);

		ResponseEntity<SeatResponse> response = seatController.setSeatActiveStatus(1L, 1L, true);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.id());
		assertEquals(true, responseBody.active());

		verify(seatService).setSeatActiveStatus(1L, true);
	}

	@Test
	void setSeatActiveStatus_ShouldReturnDeactivatedSeat() {
		SeatResponse deactivatedSeat = createSeatDto(1L, 1, 1, SeatType.STANDARD, false);

		when(seatService.setSeatActiveStatus(1L, false)).thenReturn(deactivatedSeat);

		ResponseEntity<SeatResponse> response = seatController.setSeatActiveStatus(1L, 1L, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.id());
		assertEquals(false, responseBody.active());

		verify(seatService).setSeatActiveStatus(1L, false);
	}

	@Test
	void setSeatActiveStatus_WhenNotFound_ShouldThrowException() {
		when(seatService.setSeatActiveStatus(999L, true)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.setSeatActiveStatus(1L, 999L, true));

		verify(seatService).setSeatActiveStatus(999L, true);
	}
}