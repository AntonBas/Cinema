package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.CinemaHallDto;
import ua.lviv.bas.cinema.dto.MovieSimpleDto;
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;
import ua.lviv.bas.cinema.service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionDto createSessionDto(Long id, String movieTitle, String hallName, BigDecimal price) {
		MovieSimpleDto movie = MovieSimpleDto.builder().id(1L).title(movieTitle).build();

		CinemaHallDto hall = CinemaHallDto.builder().id(1L).name(hallName).build();

		return SessionDto.builder().id(id).startTime(LocalDateTime.now().plusHours(2))
				.endTime(LocalDateTime.now().plusHours(4)).price(price).movie(movie).hall(hall).available(true).build();
	}

	private SessionRequest createSessionRequest() {
		return SessionRequest.builder().startTime(LocalDateTime.now().plusHours(2)).price(new BigDecimal("250.00"))
				.movieId(1L).hallId(1L).build();
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.createSession(request)).thenReturn(sessionDto);

		ResponseEntity<SessionDto> response = sessionController.createSession(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		SessionDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getMovie().getTitle());
		assertEquals("Hall 1", responseBody.getHall().getName());
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getSessionById(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionDto> response = sessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SessionDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getMovie().getTitle());
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionDto sessionDto = createSessionDto(1L, "Updated Movie", "Hall 1", new BigDecimal("300.00"));

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionDto> response = sessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SessionDto responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Updated Movie", responseBody.getMovie().getTitle());
		assertEquals(new BigDecimal("300.00"), responseBody.getPrice());
	}

	@Test
	void deleteSession_ShouldReturnNoContent() {
		ResponseEntity<Void> response = sessionController.deleteSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void getAllSessions_ShouldReturnAllSessions() {
		SessionDto session1 = createSessionDto(1L, "Movie 1", "Hall 1", new BigDecimal("250.00"));
		SessionDto session2 = createSessionDto(2L, "Movie 2", "Hall 2", new BigDecimal("300.00"));

		when(sessionService.getAllSessions()).thenReturn(List.of(session1, session2));

		ResponseEntity<List<SessionDto>> response = sessionController.getAllSessions();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(2, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
		assertEquals(2L, responseBody.get(1).getId());
	}

	@Test
	void getSessionsByDate_ShouldReturnSessions() {
		LocalDate date = LocalDate.now();
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getSessionsByDate(date)).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getSessionsByDate(date);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
	}

	@Test
	void getSessionsByHall_ShouldReturnSessions() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getSessionsByHall(1L)).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getSessionsByHall(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals("Hall 1", responseBody.get(0).getHall().getName());
	}

	@Test
	void getSessionsByMovie_ShouldReturnSessions() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getSessionsByMovie(1L)).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getSessionsByMovie(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals("Test Movie", responseBody.get(0).getMovie().getTitle());
	}

	@Test
	void getAvailableSessions_ShouldReturnAvailableSessions() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getAvailableSessions()).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getAvailableSessions();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals(true, responseBody.get(0).isAvailable());
	}

	@Test
	void getUpcomingSessions_ShouldReturnUpcomingSessions() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getUpcomingSessions(7)).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getUpcomingSessions(7);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
	}

	@Test
	void getTodaySessions_ShouldReturnTodaySessions() {
		SessionDto sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getTodaySessions()).thenReturn(List.of(sessionDto));

		ResponseEntity<List<SessionDto>> response = sessionController.getTodaySessions();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<SessionDto> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.size());
		assertEquals(1L, responseBody.get(0).getId());
	}

	@Test
	void checkTimeConflict_ShouldReturnTrue() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(null))).thenReturn(true);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Boolean responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(true, responseBody);
	}

	@Test
	void checkTimeConflict_ShouldReturnFalse() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(null))).thenReturn(false);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Boolean responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(false, responseBody);
	}
}