package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@ExtendWith(MockitoExtension.class)
public class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionScheduleResponse createSessionScheduleResponse(Long id) {
		return new SessionScheduleResponse(id, LocalDateTime.of(2024, 1, 15, 18, 0),
				LocalDateTime.of(2024, 1, 15, 20, 0), new BigDecimal("250.00"), 80, 1L, "Test Movie", "poster.jpg",
				"PG-13", 120, 1L, "Hall 1", 100);
	}

	@Test
	void getScheduleWithoutFiltersShouldReturnSessions() {
		String searchTerm = null;
		LocalDate date = null;
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse(1L));

		when(sessionService.getSchedule(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(searchTerm, date);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<SessionScheduleResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).hasSize(1);
		assertThat(body.get(0).movieTitle()).isEqualTo("Test Movie");

		verify(sessionService).getSchedule(searchTerm, date);
	}

	@Test
	void getScheduleWithSearchTermShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		LocalDate date = null;
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse(1L));

		when(sessionService.getSchedule(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(searchTerm, date);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<SessionScheduleResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).hasSize(1);
		assertThat(body.get(0).movieTitle()).isEqualTo("Test Movie");

		verify(sessionService).getSchedule(searchTerm, date);
	}

	@Test
	void getScheduleWithDateShouldReturnFilteredSessions() {
		String searchTerm = null;
		LocalDate date = LocalDate.of(2024, 1, 15);
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse(1L));

		when(sessionService.getSchedule(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(searchTerm, date);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<SessionScheduleResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).hasSize(1);
		assertThat(body.get(0).startTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));

		verify(sessionService).getSchedule(searchTerm, date);
	}

	@Test
	void getScheduleWithAllFiltersShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		LocalDate date = LocalDate.of(2024, 1, 15);
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse(1L));

		when(sessionService.getSchedule(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(searchTerm, date);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<SessionScheduleResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).hasSize(1);
		assertThat(body.get(0).movieTitle()).isEqualTo("Test Movie");
		assertThat(body.get(0).startTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));

		verify(sessionService).getSchedule(searchTerm, date);
	}

	@Test
	void getScheduleWhenNoResultsShouldReturnEmptyList() {
		String searchTerm = null;
		LocalDate date = null;
		List<SessionScheduleResponse> emptyList = List.of();

		when(sessionService.getSchedule(searchTerm, date)).thenReturn(emptyList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(searchTerm, date);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<SessionScheduleResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).isEmpty();

		verify(sessionService).getSchedule(searchTerm, date);
	}
}