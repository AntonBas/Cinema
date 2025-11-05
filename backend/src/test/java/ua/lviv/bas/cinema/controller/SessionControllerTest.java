package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieShortResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionResponse createSessionDto(Long id, String movieTitle, String hallName, BigDecimal price) {
		MovieShortResponse movie = MovieShortResponse.builder().id(1L).title(movieTitle).build();

		CinemaHallResponse hall = CinemaHallResponse.builder().id(1L).name(hallName).build();

		// Фіксований час для стабільності тестів
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		return SessionResponse.builder().id(id).startTime(fixedTime).endTime(fixedTime.plusHours(2)).price(price)
				.movie(movie).hall(hall).available(true).build();
	}

	private SessionRequest createSessionRequest() {
		return SessionRequest.builder().startTime(LocalDateTime.of(2024, 1, 15, 18, 0)).price(new BigDecimal("250.00"))
				.movieId(1L).hallId(1L).build();
	}

	private Page<SessionResponse> createSessionPage(SessionResponse sessionDto) {
		return new PageImpl<>(List.of(sessionDto));
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.createSession(request)).thenReturn(sessionDto);

		ResponseEntity<SessionResponse> response = sessionController.createSession(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		SessionResponse responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getMovie().getTitle());
		assertEquals("Hall 1", responseBody.getHall().getName());
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));

		when(sessionService.getSessionById(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionResponse> response = sessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SessionResponse responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getMovie().getTitle());
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionResponse sessionDto = createSessionDto(1L, "Updated Movie", "Hall 1", new BigDecimal("300.00"));

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionResponse> response = sessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		SessionResponse responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
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
	void getAllSessions_ShouldReturnAllSessionsWithPagination() {
		SessionResponse session1 = createSessionDto(1L, "Movie 1", "Hall 1", new BigDecimal("250.00"));
		SessionResponse session2 = createSessionDto(2L, "Movie 2", "Hall 2", new BigDecimal("300.00"));
		Page<SessionResponse> sessionPage = new PageImpl<>(List.of(session1, session2));

		when(sessionService.getAllSessions(any(Pageable.class), eq(null))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getAllSessions(PageRequest.of(0, 20), null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(2, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());
		assertEquals(2L, responseBody.getContent().get(1).getId());
	}

	@Test
	void getSessionsByDate_ShouldReturnSessionsWithPagination() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getSessionsByDate(eq(date), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getSessionsByDate(date, PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());
	}

	@Test
	void getSessionsByHall_ShouldReturnSessionsWithPagination() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getSessionsByHall(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getSessionsByHall(1L, PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Hall 1", responseBody.getContent().get(0).getHall().getName());
	}

	@Test
	void getSessionsByMovie_ShouldReturnSessionsWithPagination() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getSessionsByMovie(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getSessionsByMovie(1L, PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Test Movie", responseBody.getContent().get(0).getMovie().getTitle());
	}

	@Test
	void getAvailableSessions_ShouldReturnAvailableSessionsWithPagination() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getAvailableSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getAvailableSessions(PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals(true, responseBody.getContent().get(0).isAvailable());
	}

	@Test
	void getUpcomingSessions_ShouldReturnUpcomingSessionsWithPagination() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getUpcomingSessions(eq(7), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getUpcomingSessions(7, PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());
	}

	@Test
	void getTodaySessions_ShouldReturnTodaySessionsWithPagination() {
		SessionResponse sessionDto = createSessionDto(1L, "Test Movie", "Hall 1", new BigDecimal("250.00"));
		Page<SessionResponse> sessionPage = createSessionPage(sessionDto);

		when(sessionService.getTodaySessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionResponse>> response = sessionController.getTodaySessions(PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<SessionResponse> responseBody = Objects.requireNonNull(response.getBody(), "Response body should not be null");
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());
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