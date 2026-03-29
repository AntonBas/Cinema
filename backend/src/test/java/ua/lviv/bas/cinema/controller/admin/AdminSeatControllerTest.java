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

import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.service.cinema.SeatService;

@ExtendWith(MockitoExtension.class)
public class AdminSeatControllerTest {

	@Mock
	private SeatService seatService;

	@InjectMocks
	private AdminSeatController seatController;

	private final Long HALL_ID = 1L;

	private SeatResponse createSeatDto(Long id, int row, int number, SeatType seatType, boolean active) {
		return new SeatResponse(id, row, number, seatType, active);
	}

	@Test
	void updateSeatType_ShouldReturnUpdatedSeat() {
		Long seatId = 1L;
		SeatResponse updatedSeat = createSeatDto(seatId, 1, 1, SeatType.VIP, true);

		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.VIP)).thenReturn(updatedSeat);

		ResponseEntity<SeatResponse> response = seatController.updateSeatType(HALL_ID, seatId, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(SeatType.VIP, responseBody.seatType());
		assertEquals(seatId, responseBody.id());
		assertEquals(true, responseBody.active());

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.VIP);
	}

	@Test
	void updateSeatType_WhenNotFound_ShouldThrowException() {
		Long seatId = 999L;
		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.VIP)).thenThrow(new SeatNotFoundException(seatId));

		assertThrows(SeatNotFoundException.class, () -> seatController.updateSeatType(HALL_ID, seatId, SeatType.VIP));

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.VIP);
	}

	@Test
	void setSeatActiveStatus_ShouldReturnActivatedSeat() {
		Long seatId = 1L;
		SeatResponse activatedSeat = createSeatDto(seatId, 1, 1, SeatType.STANDARD, true);

		when(seatService.setSeatActiveStatus(HALL_ID, seatId, true)).thenReturn(activatedSeat);

		ResponseEntity<SeatResponse> response = seatController.setSeatActiveStatus(HALL_ID, seatId, true);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(seatId, responseBody.id());
		assertEquals(true, responseBody.active());

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, true);
	}

	@Test
	void setSeatActiveStatus_ShouldReturnDeactivatedSeat() {
		Long seatId = 1L;
		SeatResponse deactivatedSeat = createSeatDto(seatId, 1, 1, SeatType.STANDARD, false);

		when(seatService.setSeatActiveStatus(HALL_ID, seatId, false)).thenReturn(deactivatedSeat);

		ResponseEntity<SeatResponse> response = seatController.setSeatActiveStatus(HALL_ID, seatId, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(seatId, responseBody.id());
		assertEquals(false, responseBody.active());

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, false);
	}

	@Test
	void setSeatActiveStatus_WhenNotFound_ShouldThrowException() {
		Long seatId = 999L;
		when(seatService.setSeatActiveStatus(HALL_ID, seatId, true)).thenThrow(new SeatNotFoundException(seatId));

		assertThrows(SeatNotFoundException.class, () -> seatController.setSeatActiveStatus(HALL_ID, seatId, true));

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, true);
	}
}