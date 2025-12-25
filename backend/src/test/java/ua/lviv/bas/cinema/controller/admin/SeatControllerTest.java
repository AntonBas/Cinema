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
import ua.lviv.bas.cinema.service.common.SeatService;

@ExtendWith(MockitoExtension.class)
class SeatControllerTest {

	@Mock
	private SeatService seatService;

	@InjectMocks
	private SeatController seatController;

	private SeatResponse createSeatDto(Long id, int row, int number, SeatType seatType) {
		return SeatResponse.builder().id(id).row(row).number(number).seatType(seatType).build();
	}

	@Test
	void updateSeatType_ShouldReturnUpdatedSeat() {
		SeatResponse updatedSeat = createSeatDto(1L, 1, 1, SeatType.VIP);

		when(seatService.updateSeatType(1L, SeatType.VIP)).thenReturn(updatedSeat);

		ResponseEntity<SeatResponse> response = seatController.updateSeatType(1L, 1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(SeatType.VIP, responseBody.getSeatType());
		assertEquals(1L, responseBody.getId());

		verify(seatService).updateSeatType(1L, SeatType.VIP);
	}

	@Test
	void updateSeatType_WhenNotFound_ShouldThrowException() {
		when(seatService.updateSeatType(999L, SeatType.VIP)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.updateSeatType(1L, 999L, SeatType.VIP));

		verify(seatService).updateSeatType(999L, SeatType.VIP);
	}
}