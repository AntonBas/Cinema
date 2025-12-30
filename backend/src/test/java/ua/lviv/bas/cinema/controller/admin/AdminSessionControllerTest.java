package ua.lviv.bas.cinema.controller.admin;

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

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.service.common.SessionService;

@ExtendWith(MockitoExtension.class)
class AdminSessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private AdminSessionController sessionController;

	private SessionAdminResponse createSessionAdminDto(Long id) {
		return SessionAdminResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).movieId(1L)
				.movieTitle("Test Movie").movieDuration(120).hallId(1L).hallName("Hall 1").hallCapacity(100)
				.ticketsSold(50).totalRevenue(new BigDecimal("12500.00")).build();
	}

	private SessionCreateRequest createSessionRequest() {
		return SessionCreateRequest.builder().startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.basePrice(new BigDecimal("250.00")).movieId(1L).hallId(1L).build();
	}

	private SessionUpdateRequest createSessionUpdateRequest() {
		return SessionUpdateRequest.builder().basePrice(new BigDecimal("300.00")).status(CinemaSessionStatus.COMPLETED)
				.build();
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionCreateRequest request = createSessionRequest();
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.createSession(request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.createSession(request);

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

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).getSessionById(1L);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionUpdateRequest request = createSessionUpdateRequest();
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void deleteSession_ShouldReturnNoContent() {
		ResponseEntity<Void> response = sessionController.deleteSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void getAllSessions_ShouldReturnAllSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAllSessionsForAdmin(any(Pageable.class), eq(null))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getAllSessions(PageRequest.of(0, 20),
				null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getAllSessionsForAdmin(PageRequest.of(0, 20), null);
	}

	@Test
	void getFilteredSessions_ShouldReturnFilteredSessions() {
		SessionFilter filter = SessionFilter.builder().adminView(true).page(0).size(20).sortBy("startTime").build();

		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getFilteredSessions(any(SessionFilter.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getFilteredSessions(filter);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getFilteredSessions(any(SessionFilter.class));
	}

	@Test
	void getSessionsByDate_ShouldReturnSessionsWithPagination() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByDateForAdmin(eq(date), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByDate(date,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByDateForAdmin(date, PageRequest.of(0, 20));
	}

	@Test
	void getSessionsByHall_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByHallForAdmin(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByHall(1L,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByHallForAdmin(1L, PageRequest.of(0, 20));
	}

	@Test
	void getSessionsByMovie_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByMovieForAdmin(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getSessionsByMovie(1L,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByMovieForAdmin(1L, PageRequest.of(0, 20));
	}

	@Test
	void getSessionsByStatus_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getSessionsByStatus(eq(CinemaSessionStatus.SCHEDULED), any(Pageable.class)))
				.thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController
				.getSessionsByStatus(CinemaSessionStatus.SCHEDULED, PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getSessionsByStatus(CinemaSessionStatus.SCHEDULED, PageRequest.of(0, 20));
	}

	@Test
	void getAvailableSessions_ShouldReturnSessionsWithPagination() {
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAvailableSessionsForAdmin(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController
				.getAvailableSessions(PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getAvailableSessionsForAdmin(PageRequest.of(0, 20));
	}

	@Test
	void checkTimeConflict_ShouldReturnTrue() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(null))).thenReturn(true);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(true, responseBody);

		verify(sessionService).hasTimeConflict(1L, fixedTime, 120, null);
	}

	@Test
	void checkTimeConflict_WithExcludeSessionId_ShouldReturnFalse() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionService.hasTimeConflict(eq(1L), eq(fixedTime), eq(120), eq(5L))).thenReturn(false);

		ResponseEntity<Boolean> response = sessionController.checkTimeConflict(1L, fixedTime, 120, 5L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Boolean responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(false, responseBody);

		verify(sessionService).hasTimeConflict(1L, fixedTime, 120, 5L);
	}

	@Test
	void createSession_WhenTimeConflict_ShouldThrowException() {
		SessionCreateRequest request = createSessionRequest();

		when(sessionService.createSession(request))
				.thenThrow(new SessionTimeConflictException(1L, LocalDateTime.now()));

		assertThrows(SessionTimeConflictException.class, () -> sessionController.createSession(request));
		verify(sessionService).createSession(request);
	}

	@Test
	void createSession_WhenDuplicate_ShouldThrowException() {
		SessionCreateRequest request = createSessionRequest();

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
		SessionUpdateRequest request = createSessionUpdateRequest();

		when(sessionService.updateSession(999L, request)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.updateSession(999L, request));
		verify(sessionService).updateSession(999L, request);
	}

	@Test
	void deleteSession_WhenNotFound_ShouldThrowException() {
		doThrow(new SessionNotFoundException(999L)).when(sessionService).deleteSession(999L);

		assertThrows(SessionNotFoundException.class, () -> sessionController.deleteSession(999L));
		verify(sessionService).deleteSession(999L);
	}

	@Test
	void getAllSessions_WithSearch_ShouldReturnFilteredSessions() {
		String searchTerm = "Inception";
		SessionAdminResponse sessionDto = createSessionAdminDto(1L);
		Page<SessionAdminResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getAllSessionsForAdmin(any(Pageable.class), eq(searchTerm))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionAdminResponse>> response = sessionController.getAllSessions(PageRequest.of(0, 20),
				searchTerm);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionAdminResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());

		verify(sessionService).getAllSessionsForAdmin(PageRequest.of(0, 20), searchTerm);
	}

	@Test
	void updateSession_ShouldUpdateStatus_WhenStatusProvided() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().status(CinemaSessionStatus.CANCELLED).build();

		SessionAdminResponse sessionDto = createSessionAdminDto(1L);

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void updateSession_ShouldUpdateStartTime_WhenStartTimeProvided() {
		LocalDateTime newStartTime = LocalDateTime.of(2024, 1, 16, 19, 0);
		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(newStartTime).build();

		SessionAdminResponse sessionDto = SessionAdminResponse.builder().id(1L).startTime(newStartTime)
				.endTime(newStartTime.plusHours(2)).basePrice(new BigDecimal("250.00")).build();

		when(sessionService.updateSession(1L, request)).thenReturn(sessionDto);

		ResponseEntity<SessionAdminResponse> response = sessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionAdminResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());
		assertEquals(newStartTime, responseBody.getStartTime());

		verify(sessionService).updateSession(1L, request);
	}
}