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
	void getSeatsByHall_ShouldReturnSeats() {
		SeatResponse seat1 = createSeatDto(1L, 1, 1, SeatType.STANDARD);
		SeatResponse seat2 = createSeatDto(2L, 1, 2, SeatType.VIP);

		when(seatService.getSeatsByHall(1L)).thenReturn(List.of(seat1, seat2));

		ResponseEntity<List<SeatResponse>> response = seatController.getSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SeatResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(SeatType.STANDARD, responseBody.get(0).getSeatType());
		assertEquals(2L, responseBody.get(1).getId());
		assertEquals(SeatType.VIP, responseBody.get(1).getSeatType());

		verify(seatService).getSeatsByHall(1L);
	}

	@Test
	void getSeatsByHall_WhenNoSeats_ShouldReturnEmptyList() {
		when(seatService.getSeatsByHall(1L)).thenReturn(List.of());

		ResponseEntity<List<SeatResponse>> response = seatController.getSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SeatResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(0, responseBody.size());

		verify(seatService).getSeatsByHall(1L);
	}

	@Test
	void getSeatById_ShouldReturnSeat() {
		SeatResponse seat = createSeatDto(1L, 1, 1, SeatType.STANDARD);

		when(seatService.getSeatById(1L)).thenReturn(seat);

		ResponseEntity<SeatResponse> response = seatController.getSeatById(1L, 1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertEquals(1, responseBody.getRow());
		assertEquals(1, responseBody.getNumber());
		assertEquals(SeatType.STANDARD, responseBody.getSeatType());

		verify(seatService).getSeatById(1L);
	}

	@Test
	void getSeatById_WhenNotFound_ShouldThrowException() {
		when(seatService.getSeatById(999L)).thenThrow(new SeatNotFoundException(999L));

		assertThrows(SeatNotFoundException.class, () -> seatController.getSeatById(1L, 999L));

		verify(seatService).getSeatById(999L);
	}

	@Test
	void getSeatByPosition_ShouldReturnSeat() {
		SeatResponse seat = createSeatDto(1L, 1, 1, SeatType.STANDARD);

		when(seatService.getSeatByPosition(1L, 1, 1)).thenReturn(seat);

		ResponseEntity<SeatResponse> response = seatController.getSeatByPosition(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SeatResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1L, responseBody.getId());
		assertEquals(1, responseBody.getRow());
		assertEquals(1, responseBody.getNumber());

		verify(seatService).getSeatByPosition(1L, 1, 1);
	}

	@Test
	void getSeatByPosition_WhenNotFound_ShouldThrowException() {
		when(seatService.getSeatByPosition(1L, 1, 1)).thenThrow(new SeatNotFoundException(1L));

		assertThrows(SeatNotFoundException.class, () -> seatController.getSeatByPosition(1L, 1, 1));

		verify(seatService).getSeatByPosition(1L, 1, 1);
	}

	@Test
	void checkSeatAvailability_ShouldReturnTrue() {
		when(seatService.isSeatAvailable(1L, 1, 1)).thenReturn(true);

		ResponseEntity<Boolean> response = seatController.checkSeatAvailability(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(true, responseBody);

		verify(seatService).isSeatAvailable(1L, 1, 1);
	}

	@Test
	void checkSeatAvailability_ShouldReturnFalse() {
		when(seatService.isSeatAvailable(1L, 1, 1)).thenReturn(false);

		ResponseEntity<Boolean> response = seatController.checkSeatAvailability(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(false, responseBody);

		verify(seatService).isSeatAvailable(1L, 1, 1);
	}

	@Test
	void countSeatsByHall_ShouldReturnCount() {
		when(seatService.countSeatsByHall(1L)).thenReturn(50L);

		ResponseEntity<Long> response = seatController.countSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Long responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(50L, responseBody);

		verify(seatService).countSeatsByHall(1L);
	}

	@Test
	void countSeatsByHall_WhenNoSeats_ShouldReturnZero() {
		when(seatService.countSeatsByHall(1L)).thenReturn(0L);

		ResponseEntity<Long> response = seatController.countSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Long responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(0L, responseBody);

		verify(seatService).countSeatsByHall(1L);
	}

	@Test
	void getSeatsByType_ShouldReturnFilteredSeats() {
		SeatResponse vipSeat1 = createSeatDto(1L, 1, 1, SeatType.VIP);
		SeatResponse vipSeat2 = createSeatDto(2L, 1, 2, SeatType.VIP);

		when(seatService.getSeatsByType(1L, SeatType.VIP)).thenReturn(List.of(vipSeat1, vipSeat2));

		ResponseEntity<List<SeatResponse>> response = seatController.getSeatsByType(1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SeatResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.size());
		assertEquals(SeatType.VIP, responseBody.get(0).getSeatType());
		assertEquals(SeatType.VIP, responseBody.get(1).getSeatType());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(2L, responseBody.get(1).getId());

		verify(seatService).getSeatsByType(1L, SeatType.VIP);
	}

	@Test
	void getSeatsByType_WhenNoSeatsOfType_ShouldReturnEmptyList() {
		when(seatService.getSeatsByType(1L, SeatType.VIP)).thenReturn(List.of());

		ResponseEntity<List<SeatResponse>> response = seatController.getSeatsByType(1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SeatResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(0, responseBody.size());

		verify(seatService).getSeatsByType(1L, SeatType.VIP);
	}
}