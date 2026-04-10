package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.hall.SeatNotFoundException;
import ua.lviv.bas.cinema.service.cinema.SeatService;

@ExtendWith(MockitoExtension.class)
public class AdminSeatControllerTest {

	@Mock
	private SeatService seatService;

	@InjectMocks
	private AdminSeatController seatController;

	private final Long HALL_ID = 1L;

	private SeatResponse createSeatResponse(Long id, int row, int number, SeatType seatType, boolean active) {
		return new SeatResponse(id, row, number, seatType, active);
	}

	@Test
	void updateSeatTypeShouldReturnUpdatedSeat() {
		Long seatId = 1L;
		SeatResponse updatedSeat = createSeatResponse(seatId, 1, 1, SeatType.VIP, true);

		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.VIP)).thenReturn(updatedSeat);

		SeatResponse response = seatController.updateSeatType(HALL_ID, seatId, SeatType.VIP);

		assertThat(response).isNotNull();
		assertThat(response.seatType()).isEqualTo(SeatType.VIP);
		assertThat(response.id()).isEqualTo(seatId);
		assertThat(response.active()).isTrue();

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.VIP);
	}

	@Test
	void updateSeatTypeWhenNotFoundShouldThrowException() {
		Long seatId = 999L;
		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.VIP)).thenThrow(new SeatNotFoundException(seatId));

		assertThrows(SeatNotFoundException.class, () -> seatController.updateSeatType(HALL_ID, seatId, SeatType.VIP));

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.VIP);
	}

	@Test
	void setSeatActiveStatusShouldReturnActivatedSeat() {
		Long seatId = 1L;
		SeatResponse activatedSeat = createSeatResponse(seatId, 1, 1, SeatType.STANDARD, true);

		when(seatService.setSeatActiveStatus(HALL_ID, seatId, true)).thenReturn(activatedSeat);

		SeatResponse response = seatController.setSeatActiveStatus(HALL_ID, seatId, true);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(seatId);
		assertThat(response.active()).isTrue();

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, true);
	}

	@Test
	void setSeatActiveStatusShouldReturnDeactivatedSeat() {
		Long seatId = 1L;
		SeatResponse deactivatedSeat = createSeatResponse(seatId, 1, 1, SeatType.STANDARD, false);

		when(seatService.setSeatActiveStatus(HALL_ID, seatId, false)).thenReturn(deactivatedSeat);

		SeatResponse response = seatController.setSeatActiveStatus(HALL_ID, seatId, false);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(seatId);
		assertThat(response.active()).isFalse();

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, false);
	}

	@Test
	void setSeatActiveStatusWhenNotFoundShouldThrowException() {
		Long seatId = 999L;
		when(seatService.setSeatActiveStatus(HALL_ID, seatId, true)).thenThrow(new SeatNotFoundException(seatId));

		assertThrows(SeatNotFoundException.class, () -> seatController.setSeatActiveStatus(HALL_ID, seatId, true));

		verify(seatService).setSeatActiveStatus(HALL_ID, seatId, true);
	}

	@Test
	void updateSeatTypeToStandardShouldSucceed() {
		Long seatId = 1L;
		SeatResponse updatedSeat = createSeatResponse(seatId, 1, 1, SeatType.STANDARD, true);

		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.STANDARD)).thenReturn(updatedSeat);

		SeatResponse response = seatController.updateSeatType(HALL_ID, seatId, SeatType.STANDARD);

		assertThat(response).isNotNull();
		assertThat(response.seatType()).isEqualTo(SeatType.STANDARD);

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.STANDARD);
	}

	@Test
	void updateSeatTypeToCoupleShouldSucceed() {
		Long seatId = 1L;
		SeatResponse updatedSeat = createSeatResponse(seatId, 1, 1, SeatType.COUPLE, true);

		when(seatService.updateSeatType(HALL_ID, seatId, SeatType.COUPLE)).thenReturn(updatedSeat);

		SeatResponse response = seatController.updateSeatType(HALL_ID, seatId, SeatType.COUPLE);

		assertThat(response).isNotNull();
		assertThat(response.seatType()).isEqualTo(SeatType.COUPLE);

		verify(seatService).updateSeatType(HALL_ID, seatId, SeatType.COUPLE);
	}
}