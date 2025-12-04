package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionAdminResponse createSessionAdminDto(Long id) {
		return SessionAdminResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).movieId(1L)
				.movieTitle("Test Movie").movieDuration(120).hallId(1L).hallName("Hall 1").hallCapacity(100)
				.available(true).ticketsSold(50).totalRevenue(new BigDecimal("12500.00")).build();
	}

	private SessionScheduleResponse createSessionScheduleDto(Long id) {
		return SessionScheduleResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).availableSeats(100)
				.movieId(1L).movieTitle("Test Movie").moviePosterFileName("poster.jpg").movieAgeRating("PG-13")
				.movieDuration(120).hallId(1L).hallName("Hall 1").hallCapacity("100/100").build();
	}

	private SessionRequest createSessionRequest() {
		return SessionRequest.builder().startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.basePrice(new BigDecimal("250.00")).movieId(1L).hallId(1L).build();
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.createSession(request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.createSession(request);

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).createSession(request);
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.getSessionById(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.getSessionById(1L);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).getSessionById(1L);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionRequest request = createSessionRequest();
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.updateSession(1L, request);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void deleteSession_ShouldReturnNoContent() {
		ResponseEntity<Void> response = sessionController.deleteSession(1L);

		assertNotNull(response);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void getAllSessions_ShouldReturnAllSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAllSessions(any(Pageable.class), eq(null))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getAllSessions(PageRequest.of(0, 20),
				null);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getAllSessions(PageRequest.of(0, 20), null);
	}

	@Test
	void getAllSessions_ShouldReturnSearchedSessions() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAllSessions(any(Pageable.class), eq("test"))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getAllSessions(PageRequest.of(0, 20),
				"test");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getAllSessions(PageRequest.of(0, 20), "test");
	}

	@Test
	void getSessionsByDate_ShouldReturnSessionsWithPagination() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByDate(eq(date), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByDate(date,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByDate(date, PageRequest.of(0, 20));
	}

	@Test
	void getSessionsByHall_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByHall(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByHall(1L,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByHall(1L, PageRequest.of(0, 20));
	}

	@Test
	void getSessionsByMovie_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByMovie(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByMovie(1L,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByMovie(1L, PageRequest.of(0, 20));
	}

	@Test
	void getAvailableSessions_ShouldReturnAvailableSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAvailableSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController
				.getAvailableSessions(PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getAvailableSessions(PageRequest.of(0, 20));
	}

	@Test
	void getUpcomingSessions_ShouldReturnUpcomingSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getUpcomingSessions(eq(7), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getUpcomingSessions(7,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getUpcomingSessions(7, PageRequest.of(0, 20));
	}

	@Test
	void getTodaySessions_ShouldReturnTodaySessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getTodaySessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getTodaySessions(PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getTodaySessions(PageRequest.of(0, 20));
	}

	@Test
	void checkTimeConflict_ShouldReturnTrue() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(null))).thenReturn(true);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, null);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(true, responseBody);

		verify(sessionService).hasTimeConflict(1L, fixedTime, 120, null);
	}

	@Test
	void checkTimeConflict_ShouldReturnFalse() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(null))).thenReturn(false);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, null);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(false, responseBody);

		verify(sessionService).hasTimeConflict(1L, fixedTime, 120, null);
	}

	@Test
	void getFilteredSessions_ShouldReturnFilteredSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		LocalDate date = LocalDate.of(2024, 1, 15);
		Long hallId = 1L;
		Long movieId = 1L;
		Integer days = 7;

		when(sessionService.getFilteredSessions(eq(date), eq(hallId), eq(movieId), eq(days), any(Pageable.class)))
				.thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getFilteredSessions(date, hallId,
				movieId, days, PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getFilteredSessions(date, hallId, movieId, days, PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController
				.getScheduleSessions(PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessions(PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessionsByDate_ShouldReturnScheduleSessionsWithPagination() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByDate(eq(date), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByDate(date,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByDate(date, PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessionsByMovie_ShouldReturnScheduleSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByMovie(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByMovie(1L,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByMovie(1L, PageRequest.of(0, 20));
	}

	@Test
	void getAvailableScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController
				.getAvailableScheduleSessions(PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessions(PageRequest.of(0, 20));
	}

	@Test
	void getUpcomingScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		LocalDate futureDate = LocalDate.now().plusDays(7);
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByDate(eq(futureDate), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getUpcomingScheduleSessions(7,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByDate(futureDate, PageRequest.of(0, 20));
	}

	@Test
	void getTodayScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		LocalDate today = LocalDate.now();
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByDate(eq(today), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController
				.getTodayScheduleSessions(PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByDate(today, PageRequest.of(0, 20));
	}

	@Test
	void createSession_WhenTimeConflict_ShouldThrowException() {
		SessionRequest request = createSessionRequest();

		when(sessionService.createSession(request))
				.thenThrow(new SessionTimeConflictException(1L, LocalDateTime.now()));

		assertThrows(SessionTimeConflictException.class, () -> sessionController.createSession(request));
		verify(sessionService).createSession(request);
	}

	@Test
	void createSession_WhenDuplicate_ShouldThrowException() {
		SessionRequest request = createSessionRequest();

		when(sessionService.createSession(request))
				.thenThrow(new DuplicateEntityException("Session", "Duplicate session"));

		assertThrows(DuplicateEntityException.class, () -> sessionController.createSession(request));
		verify(sessionService).createSession(request);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionById(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(999L));
		verify(sessionService).getSessionById(999L);
	}

	@Test
	void updateSession_WhenNotFound_ShouldThrowException() {
		SessionRequest request = createSessionRequest();

		when(sessionService.updateSession(999L, request)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.updateSession(999L, request));
		verify(sessionService).updateSession(999L, request);
	}

	@Test
	void updateSession_WhenTimeConflict_ShouldThrowException() {
		SessionRequest request = createSessionRequest();

		when(sessionService.updateSession(1L, request))
				.thenThrow(new SessionTimeConflictException(1L, LocalDateTime.now()));

		assertThrows(SessionTimeConflictException.class, () -> sessionController.updateSession(1L, request));
		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void deleteSession_WhenNotFound_ShouldThrowException() {
		doThrow(new SessionNotFoundException(999L)).when(sessionService).deleteSession(999L);

		assertThrows(SessionNotFoundException.class, () -> sessionController.deleteSession(999L));
		verify(sessionService).deleteSession(999L);
	}
}