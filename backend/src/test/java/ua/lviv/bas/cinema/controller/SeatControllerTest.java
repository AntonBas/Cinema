package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import ua.lviv.bas.cinema.dto.shared.SeatDto;
import ua.lviv.bas.cinema.service.SeatService;

@ExtendWith(MockitoExtension.class)
class SeatControllerTest {

	@Mock
	private SeatService seatService;

	@InjectMocks
	private SeatController seatController;

	private SeatDto createSeatDto(Long id, int row, int number, SeatType seatType) {
		return SeatDto.builder().id(id).row(row).number(number).seatType(seatType).build();
	}

	@Test
	void getSeatsByHall_ShouldReturnSeats() {
		SeatDto seat1 = createSeatDto(1L, 1, 1, SeatType.STANDARD);
		SeatDto seat2 = createSeatDto(2L, 1, 2, SeatType.VIP);

		when(seatService.getSeatsByHall(1L)).thenReturn(List.of(seat1, seat2));

		ResponseEntity<List<SeatDto>> response = seatController.getSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SeatDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(SeatType.STANDARD, responseBody.get(0).getSeatType());
		assertEquals(2L, responseBody.get(1).getId());
		assertEquals(SeatType.VIP, responseBody.get(1).getSeatType());
	}

	@Test
	void getSeatById_ShouldReturnSeat() {
		SeatDto seat = createSeatDto(1L, 1, 1, SeatType.STANDARD);

		when(seatService.getSeatById(1L)).thenReturn(seat);

		ResponseEntity<SeatDto> response = seatController.getSeatById(1L, 1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SeatDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals(1, responseBody.getRow());
		assertEquals(1, responseBody.getNumber());
		assertEquals(SeatType.STANDARD, responseBody.getSeatType());
	}

	@Test
	void getSeatByPosition_ShouldReturnSeat() {
		SeatDto seat = createSeatDto(1L, 1, 1, SeatType.STANDARD);

		when(seatService.getSeatByPosition(1L, 1, 1)).thenReturn(seat);

		ResponseEntity<SeatDto> response = seatController.getSeatByPosition(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SeatDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals(1, responseBody.getRow());
		assertEquals(1, responseBody.getNumber());
	}

	@Test
	void updateSeatType_ShouldReturnUpdatedSeat() {
		SeatDto updatedSeat = createSeatDto(1L, 1, 1, SeatType.VIP);

		when(seatService.updateSeatType(1L, SeatType.VIP)).thenReturn(updatedSeat);

		ResponseEntity<SeatDto> response = seatController.updateSeatType(1L, 1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SeatDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(SeatType.VIP, responseBody.getSeatType());
		assertEquals(1L, responseBody.getId());
	}

	@Test
	void checkSeatAvailability_ShouldReturnTrue() {
		when(seatService.isSeatAvailable(1L, 1, 1)).thenReturn(true);

		ResponseEntity<Boolean> response = seatController.checkSeatAvailability(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Boolean responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(true, responseBody);
	}

	@Test
	void checkSeatAvailability_ShouldReturnFalse() {
		when(seatService.isSeatAvailable(1L, 1, 1)).thenReturn(false);

		ResponseEntity<Boolean> response = seatController.checkSeatAvailability(1L, 1, 1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Boolean responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(false, responseBody);
	}

	@Test
	void countSeatsByHall_ShouldReturnCount() {
		when(seatService.countSeatsByHall(1L)).thenReturn(50L);

		ResponseEntity<Long> response = seatController.countSeatsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Long responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(50L, responseBody);
	}

	@Test
	void getSeatsByType_ShouldReturnFilteredSeats() {
		SeatDto vipSeat1 = createSeatDto(1L, 1, 1, SeatType.VIP);
		SeatDto vipSeat2 = createSeatDto(2L, 1, 2, SeatType.VIP);

		when(seatService.getSeatsByType(1L, SeatType.VIP)).thenReturn(List.of(vipSeat1, vipSeat2));

		ResponseEntity<List<SeatDto>> response = seatController.getSeatsByType(1L, SeatType.VIP);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SeatDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals(SeatType.VIP, responseBody.get(0).getSeatType());
		assertEquals(SeatType.VIP, responseBody.get(1).getSeatType());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(2L, responseBody.get(1).getId());
	}
}