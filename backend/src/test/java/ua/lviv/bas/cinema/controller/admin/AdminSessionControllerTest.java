package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.service.cinema.SessionService;

@ExtendWith(MockitoExtension.class)
public class AdminSessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private AdminSessionController adminSessionController;

	private SessionAdminResponse createSessionResponse(Long id, String title, BigDecimal basePrice) {
		return new SessionAdminResponse(id, LocalDateTime.now().plusHours(2), null, basePrice,
				CinemaSessionStatus.SCHEDULED, 1L, title, 120, 1L, "Hall 1", 100, 0, BigDecimal.ZERO);
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		BigDecimal price = BigDecimal.valueOf(250);
		SessionCreateRequest request = new SessionCreateRequest(startTime, price, 1L, 1L);

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie", price);

		when(sessionService.createSession(request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.createSession(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.id());
		assertEquals(price, body.basePrice());
		verify(sessionService).createSession(request);
	}

	@Test
	void createSession_WhenTimeConflict_ShouldThrowException() {
		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		SessionCreateRequest request = new SessionCreateRequest(startTime, BigDecimal.valueOf(250), 1L, 1L);

		when(sessionService.createSession(request))
				.thenThrow(new SessionTimeConflictException(1L, request.startTime()));

		assertThrows(SessionTimeConflictException.class, () -> adminSessionController.createSession(request));
		verify(sessionService).createSession(request);
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		BigDecimal price = BigDecimal.valueOf(250);
		SessionAdminResponse response = createSessionResponse(1L, "Test Movie", price);

		when(sessionService.getSessionForAdmin(1L)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.id());
		assertEquals(price, body.basePrice());
		verify(sessionService).getSessionForAdmin(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionForAdmin(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.getSessionById(999L));
		verify(sessionService).getSessionForAdmin(999L);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		BigDecimal newPrice = BigDecimal.valueOf(300);
		SessionUpdateRequest request = new SessionUpdateRequest(null, newPrice, null, null);

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie", newPrice);

		when(sessionService.updateSession(1L, request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(newPrice, body.basePrice());
		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void updateSession_WhenNotFound_ShouldThrowException() {
		SessionUpdateRequest request = new SessionUpdateRequest(null, BigDecimal.valueOf(300), null, null);

		when(sessionService.updateSession(999L, request)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.updateSession(999L, request));
		verify(sessionService).updateSession(999L, request);
	}

	@Test
	void updateSession_WhenTimeConflict_ShouldThrowException() {
		LocalDateTime newStartTime = LocalDateTime.now().plusHours(3);
		SessionUpdateRequest request = new SessionUpdateRequest(newStartTime, null, null, null);

		when(sessionService.updateSession(1L, request))
				.thenThrow(new SessionTimeConflictException(1L, request.startTime()));

		assertThrows(SessionTimeConflictException.class, () -> adminSessionController.updateSession(1L, request));
		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void cancelSession_ShouldCancelSuccessfully() {
		ResponseEntity<Void> result = adminSessionController.cancelSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(sessionService).cancelSession(1L);
	}

	@Test
	void cancelSession_WhenNotFound_ShouldThrowException() {
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
	void reactivateSession_WhenNotFound_ShouldThrowException() {
		doThrow(new SessionNotFoundException(1L)).when(sessionService).reactivateSession(1L);

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.reactivateSession(1L));
		verify(sessionService).reactivateSession(1L);
	}

	@Test
	void deleteSession_ShouldDeleteSuccessfully() {
		ResponseEntity<Void> result = adminSessionController.deleteSession(1L);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void getSessions_ShouldReturnSessions() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "startTime"));
		Long hallId = 1L;
		String movieTitle = "Test";
		CinemaSessionStatus status = CinemaSessionStatus.SCHEDULED;
		LocalDate dateFrom = LocalDate.now();
		LocalDate dateTo = LocalDate.now().plusDays(7);
		BigDecimal price = BigDecimal.valueOf(250);

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie", price);
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response), pageable, 1);
		PageResponse<SessionAdminResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getSessionsForAdmin(hallId, movieTitle, status, dateFrom, dateTo, pageable))
				.thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionAdminResponse>> result = adminSessionController.getSessions(hallId,
				movieTitle, status, dateFrom, dateTo, pageable);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		PageResponse<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.content().size());
		assertEquals(0, body.number());
		assertEquals(10, body.size());
		assertEquals(1, body.totalElements());
		verify(sessionService).getSessionsForAdmin(hallId, movieTitle, status, dateFrom, dateTo, pageable);
	}
}