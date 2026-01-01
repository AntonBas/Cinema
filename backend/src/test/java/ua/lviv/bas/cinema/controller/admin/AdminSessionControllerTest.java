package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.service.common.SessionService;

@ExtendWith(MockitoExtension.class)
class AdminSessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private AdminSessionController adminSessionController;

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.basePrice(BigDecimal.valueOf(250)).movieId(1L).hallId(1L).build();

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).startTime(request.getStartTime())
				.basePrice(request.getBasePrice()).build();

		when(sessionService.createSession(request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.createSession(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		verify(sessionService).createSession(request);
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L)
				.startTime(LocalDateTime.now().plusHours(2)).basePrice(BigDecimal.valueOf(250)).build();

		when(sessionService.getSessionById(1L)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		verify(sessionService).getSessionById(1L);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).basePrice(BigDecimal.valueOf(300))
				.build();

		when(sessionService.updateSession(1L, request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(BigDecimal.valueOf(300), body.getBasePrice());
		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void cancelSession_ShouldCancelSuccessfully() {
		ResponseEntity<Void> result = adminSessionController.cancelSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(sessionService).cancelSession(1L);
	}

	@Test
	void cancelSession_ShouldThrowException_WhenSessionNotFound() {
		doThrow(new SessionNotFoundException(1L)).when(sessionService).cancelSession(1L);

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.cancelSession(1L));
		verify(sessionService).cancelSession(1L);
	}

	@Test
	void reactivateSession_ShouldReactivateSuccessfully() {
		ResponseEntity<Void> result = adminSessionController.reactivateSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(sessionService).reactivateSession(1L);
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenTimeConflict() {
		doThrow(new SessionTimeConflictException(1L, LocalDateTime.now())).when(sessionService).reactivateSession(1L);

		assertThrows(SessionTimeConflictException.class, () -> adminSessionController.reactivateSession(1L));
		verify(sessionService).reactivateSession(1L);
	}

	@Test
	void deleteSession_ShouldDeleteSuccessfully() {
		ResponseEntity<Void> result = adminSessionController.deleteSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void deleteSession_ShouldThrowException_WhenSessionNotFound() {
		doThrow(new SessionNotFoundException(1L)).when(sessionService).deleteSession(1L);

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.deleteSession(1L));
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void getSessions_ShouldReturnAllSessions_WhenNoFilters() {
		Pageable pageable = PageRequest.of(0, 20);
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(null, null, null, null, null, pageable)).thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, null, null,
				null, null, null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(null, null, null, null, null, pageable);
	}

	@Test
	void getSessions_ShouldFilterBySearch() {
		Pageable pageable = PageRequest.of(0, 20);
		String search = "Test";
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(eq(search), any(), any(), any(), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, search, null,
				null, null, null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(eq(search), any(), any(), any(), any(), eq(pageable));
	}

	@Test
	void getSessions_ShouldFilterByDate() {
		Pageable pageable = PageRequest.of(0, 20);
		LocalDate date = LocalDate.now();
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(any(), eq(date), any(), any(), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, null, date,
				null, null, null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(any(), eq(date), any(), any(), any(), eq(pageable));
	}

	@Test
	void getSessions_ShouldFilterByHallId() {
		Pageable pageable = PageRequest.of(0, 20);
		Long hallId = 1L;
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(any(), any(), eq(hallId), any(), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, null, null,
				hallId, null, null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(any(), any(), eq(hallId), any(), any(), eq(pageable));
	}

	@Test
	void getSessions_ShouldFilterByMovieId() {
		Pageable pageable = PageRequest.of(0, 20);
		Long movieId = 1L;
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(any(), any(), any(), eq(movieId), any(), eq(pageable)))
				.thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, null, null,
				null, movieId, null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(any(), any(), any(), eq(movieId), any(), eq(pageable));
	}

	@Test
	void getSessions_ShouldFilterByStatus() {
		Pageable pageable = PageRequest.of(0, 20);
		CinemaSessionStatus status = CinemaSessionStatus.SCHEDULED;
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response));

		when(sessionService.getSessionsForAdmin(any(), any(), any(), any(), eq(status), eq(pageable))).thenReturn(page);

		ResponseEntity<Page<SessionAdminResponse>> result = adminSessionController.getSessions(pageable, null, null,
				null, null, status);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Page<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(any(), any(), any(), any(), eq(status), eq(pageable));
	}

	@Test
	void checkTimeConflict_ShouldReturnTrue_WhenConflictExists() {
		Long hallId = 1L;
		LocalDateTime startTime = LocalDateTime.now();
		Integer durationMinutes = 120;

		when(sessionService.hasTimeConflict(hallId, startTime, durationMinutes, null)).thenReturn(true);

		ResponseEntity<Boolean> result = adminSessionController.checkTimeConflict(hallId, startTime, durationMinutes,
				null);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Boolean body = result.getBody();
		assertNotNull(body);
		assertTrue(body);
		verify(sessionService).hasTimeConflict(hallId, startTime, durationMinutes, null);
	}

	@Test
	void checkTimeConflict_ShouldReturnFalse_WhenNoConflict() {
		Long hallId = 1L;
		LocalDateTime startTime = LocalDateTime.now();
		Integer durationMinutes = 120;
		Long excludeSessionId = 5L;

		when(sessionService.hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId)).thenReturn(false);

		ResponseEntity<Boolean> result = adminSessionController.checkTimeConflict(hallId, startTime, durationMinutes,
				excludeSessionId);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		Boolean body = result.getBody();
		assertNotNull(body);
		assertFalse(body);
		verify(sessionService).hasTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
	}
}