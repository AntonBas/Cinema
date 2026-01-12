package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
		return SeatResponse.builder().id(id).row(row).number(number).seatType(seatType).active(active).build();
	}

	@Test
	void updateSeatType_ShouldReturnUpdatedSeat() {
		SeatResponse updatedSeat = createSeatDto(1L, 1, 1, SeatType.VIP, true);

		when(seatService.updateSeatType(1L, SeatType.VIP)).thenReturn(updatedSeat);

		ResponseEntity<SeatResponse> response = seatController.updateSeatType(1L, 1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(SeatType.VIP, responseBody.getSeatType());
		assertEquals(1L, responseBody.getId());
		assertTrue(responseBody.isActive());

		verify(seatService).updateSeatType(1L, SeatType.VIP);
	}

	@Test
	void updateSeatType_WhenNotFound_ShouldThrowException() {
		when(seatService.updateSeatType(999L, SeatType.VIP)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.updateSeatType(1L, 999L, SeatType.VIP));

		verify(seatService).updateSeatType(999L, SeatType.VIP);
	}

	@Test
	void activateSeat_ShouldReturnActivatedSeat() {
		SeatResponse activatedSeat = createSeatDto(1L, 1, 1, SeatType.STANDARD, true);

		when(seatService.activateSeat(1L)).thenReturn(activatedSeat);

		ResponseEntity<SeatResponse> response = seatController.activateSeat(1L, 1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertTrue(responseBody.isActive());

		verify(seatService).activateSeat(1L);
	}

	@Test
	void activateSeat_WhenNotFound_ShouldThrowException() {
		when(seatService.activateSeat(999L)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.activateSeat(1L, 999L));

		verify(seatService).activateSeat(999L);
	}

	@Test
	void deactivateSeat_ShouldReturnDeactivatedSeat() {
		SeatResponse deactivatedSeat = createSeatDto(1L, 1, 1, SeatType.STANDARD, false);

		when(seatService.deactivateSeat(1L)).thenReturn(deactivatedSeat);

		ResponseEntity<SeatResponse> response = seatController.deactivateSeat(1L, 1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertFalse(responseBody.isActive());

		verify(seatService).deactivateSeat(1L);
	}

	@Test
	void deactivateSeat_WhenNotFound_ShouldThrowException() {
		when(seatService.deactivateSeat(999L)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.deactivateSeat(1L, 999L));

		verify(seatService).deactivateSeat(999L);
	}
}