package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.service.cinema.CinemaHallService;

@ExtendWith(MockitoExtension.class)
public class AdminCinemaHallControllerTest {

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private AdminCinemaHallController controller;

	private final Long HALL_ID = 1L;
	private final String HALL_NAME = "Test Hall";

	@Test
	void createHallShouldReturnCreatedHall() {
		CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, 5, 10, SeatType.STANDARD, null);
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 5, 10, SeatType.STANDARD, null, 50);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(response);

		CinemaHallResponse result = controller.createHall(request);

		assertThat(result).isEqualTo(response);
		verify(cinemaHallService).createHall(request);
	}

	@Test
	void createHallShouldThrowExceptionWhenDuplicateName() {
		CinemaHallRequest request = new CinemaHallRequest("Existing Hall", null, null, null, null);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class)))
				.thenThrow(new DuplicateEntityException("CinemaHall", "Existing Hall"));

		assertThatThrownBy(() -> controller.createHall(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getHallsShouldReturnList() {
		List<CinemaHallListResponse> response = List.of(new CinemaHallListResponse(1L, "Hall A", 50),
				new CinemaHallListResponse(2L, "Hall B", 30));

		when(cinemaHallService.getHalls()).thenReturn(response);

		List<CinemaHallListResponse> result = controller.getHalls();

		assertThat(result).hasSize(2);
		verify(cinemaHallService).getHalls();
	}

	@Test
	void getHallShouldReturnHall() {
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 5, 10, SeatType.STANDARD, null, 50);

		when(cinemaHallService.getHall(HALL_ID)).thenReturn(response);

		CinemaHallResponse result = controller.getHall(HALL_ID);

		assertThat(result).isEqualTo(response);
		verify(cinemaHallService).getHall(HALL_ID);
	}

	@Test
	void getHallShouldThrowExceptionWhenNotFound() {
		when(cinemaHallService.getHall(999L)).thenThrow(new CinemaHallNotFoundException(999L));

		assertThatThrownBy(() -> controller.getHall(999L)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void updateHallShouldReturnUpdatedHall() {
		CinemaHallRequest request = new CinemaHallRequest("Updated Hall", 6, 12, SeatType.VIP, List.of(3, 4));
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "Updated Hall", 6, 12, SeatType.VIP,
				List.of(3, 4), 72);

		when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(response);

		CinemaHallResponse result = controller.updateHall(HALL_ID, request);

		assertThat(result).isEqualTo(response);
		verify(cinemaHallService).updateHall(HALL_ID, request);
	}

	@Test
	void updateHallShouldThrowExceptionWhenNotFound() {
		CinemaHallRequest request = new CinemaHallRequest(null, null, null, null, null);

		when(cinemaHallService.updateHall(eq(999L), any(CinemaHallRequest.class)))
				.thenThrow(new CinemaHallNotFoundException(999L));

		assertThatThrownBy(() -> controller.updateHall(999L, request)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void deleteHallShouldCallService() {
		controller.deleteHall(HALL_ID);

		verify(cinemaHallService).deleteHall(HALL_ID);
	}

	@Test
	void deleteHallShouldThrowExceptionWhenNotFound() {
		doThrow(new CinemaHallNotFoundException(999L)).when(cinemaHallService).deleteHall(999L);

		assertThatThrownBy(() -> controller.deleteHall(999L)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void getHallLayoutShouldReturnLayout() {
		HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 5, 10, 50, List.of());

		when(cinemaHallService.getHallLayout(HALL_ID)).thenReturn(response);

		HallLayoutResponse result = controller.getHallLayout(HALL_ID);

		assertThat(result).isEqualTo(response);
		verify(cinemaHallService).getHallLayout(HALL_ID);
	}

	@Test
	void getHallLayoutShouldThrowExceptionWhenNotFound() {
		when(cinemaHallService.getHallLayout(999L)).thenThrow(new CinemaHallNotFoundException(999L));

		assertThatThrownBy(() -> controller.getHallLayout(999L)).isInstanceOf(CinemaHallNotFoundException.class);
	}
}