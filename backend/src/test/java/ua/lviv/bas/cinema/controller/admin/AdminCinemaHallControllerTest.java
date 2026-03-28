package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
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
	void createHall_ReturnsCreatedHall() {
		CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, null, null, null, null);
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(response);

		ResponseEntity<CinemaHallResponse> result = controller.createHall(request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isEqualTo(response);
		verify(cinemaHallService).createHall(request);
	}

	@Test
	void createHall_ThrowsException_WhenDuplicateName() {
		CinemaHallRequest request = new CinemaHallRequest("Existing Hall", null, null, null, null);

		when(cinemaHallService.createHall(any(CinemaHallRequest.class)))
				.thenThrow(new DuplicateEntityException("CinemaHall", "Existing Hall"));

		assertThatThrownBy(() -> controller.createHall(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getHallById_ReturnsHall() {
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

		when(cinemaHallService.getHallById(HALL_ID)).thenReturn(response);

		ResponseEntity<CinemaHallResponse> result = controller.getHallById(HALL_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo(response);
		verify(cinemaHallService).getHallById(HALL_ID);
	}

	@Test
	void getHallById_ThrowsException_WhenNotFound() {
		when(cinemaHallService.getHallById(999L)).thenThrow(new CinemaHallNotFoundException(999L));

		assertThatThrownBy(() -> controller.getHallById(999L)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void updateHall_ReturnsUpdatedHall() {
		CinemaHallRequest request = new CinemaHallRequest("Updated Hall", null, null, null, null);
		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "Updated Hall", 0);

		when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(response);

		ResponseEntity<CinemaHallResponse> result = controller.updateHall(HALL_ID, request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo(response);
		verify(cinemaHallService).updateHall(HALL_ID, request);
	}

	@Test
	void updateHall_ThrowsException_WhenNotFound() {
		CinemaHallRequest request = new CinemaHallRequest(null, null, null, null, null);

		when(cinemaHallService.updateHall(eq(999L), any(CinemaHallRequest.class)))
				.thenThrow(new CinemaHallNotFoundException(999L));

		assertThatThrownBy(() -> controller.updateHall(999L, request)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void deleteHall_ReturnsNoContent() {
		ResponseEntity<Void> result = controller.deleteHall(HALL_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(cinemaHallService).deleteHall(HALL_ID);
	}

	@Test
	void deleteHall_ThrowsException_WhenNotFound() {
		doThrow(new CinemaHallNotFoundException(999L)).when(cinemaHallService).deleteHall(999L);

		assertThatThrownBy(() -> controller.deleteHall(999L)).isInstanceOf(CinemaHallNotFoundException.class);
	}
}