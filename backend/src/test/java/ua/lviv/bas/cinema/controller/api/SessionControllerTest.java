package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@ExtendWith(MockitoExtension.class)
public class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionScheduleResponse createSessionScheduleDto(Long id) {
		return new SessionScheduleResponse(id, LocalDateTime.of(2024, 1, 15, 18, 0),
				LocalDateTime.of(2024, 1, 15, 20, 0), new BigDecimal("250.00"), 80, 1L, "Test Movie", "poster.jpg",
				"PG-13", 120, 1L, "Hall 1", 100);
	}

	@Test
	void getScheduleSessions_WithoutFilters_ShouldReturnSessions() {
		String searchTerm = null;
		LocalDate date = null;
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleDto(1L));

		when(sessionService.getScheduleSessions(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getScheduleSessions(searchTerm,
				date);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());
		assertEquals("Test Movie", body.get(0).movieTitle());

		verify(sessionService).getScheduleSessions(searchTerm, date);
	}

	@Test
	void getScheduleSessions_WithSearchTerm_ShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		LocalDate date = null;
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleDto(1L));

		when(sessionService.getScheduleSessions(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getScheduleSessions(searchTerm,
				date);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());
		assertEquals("Test Movie", body.get(0).movieTitle());

		verify(sessionService).getScheduleSessions(searchTerm, date);
	}

	@Test
	void getScheduleSessions_WithDate_ShouldReturnFilteredSessions() {
		String searchTerm = null;
		LocalDate date = LocalDate.of(2024, 1, 15);
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleDto(1L));

		when(sessionService.getScheduleSessions(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getScheduleSessions(searchTerm,
				date);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 0), body.get(0).startTime());

		verify(sessionService).getScheduleSessions(searchTerm, date);
	}

	@Test
	void getScheduleSessions_WithAllFilters_ShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		LocalDate date = LocalDate.of(2024, 1, 15);
		List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleDto(1L));

		when(sessionService.getScheduleSessions(searchTerm, date)).thenReturn(sessionList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getScheduleSessions(searchTerm,
				date);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.size());
		assertEquals("Test Movie", body.get(0).movieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 0), body.get(0).startTime());

		verify(sessionService).getScheduleSessions(searchTerm, date);
	}

	@Test
	void getScheduleSessions_WhenNoResults_ShouldReturnEmptyList() {
		String searchTerm = null;
		LocalDate date = null;
		List<SessionScheduleResponse> emptyList = List.of();

		when(sessionService.getScheduleSessions(searchTerm, date)).thenReturn(emptyList);

		ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getScheduleSessions(searchTerm,
				date);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.size());

		verify(sessionService).getScheduleSessions(searchTerm, date);
	}

	@Test
	void getSessionById_ShouldReturnSessionForPublic() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);

		when(sessionService.getSessionForPublic(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionScheduleResponse> response = sessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionScheduleResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(1L, body.id());
		assertEquals("Test Movie", body.movieTitle());
		assertEquals("Hall 1", body.hallName());
		assertEquals(80, body.availableSeats());
		assertEquals(100, body.hallCapacity());

		verify(sessionService).getSessionForPublic(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionForPublic(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(999L));
		verify(sessionService).getSessionForPublic(999L);
	}
}