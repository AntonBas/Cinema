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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
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

	private SessionAdminResponse createSessionResponse(Long id, String title) {
		return SessionAdminResponse.builder().id(id).movieTitle(title).startTime(LocalDateTime.now().plusHours(2))
				.basePrice(BigDecimal.valueOf(250)).status(CinemaSessionStatus.SCHEDULED).build();
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.basePrice(BigDecimal.valueOf(250)).movieId(1L).hallId(1L).build();

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie");

		when(sessionService.createSession(request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.createSession(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		verify(sessionService).createSession(request);
	}

	@Test
	void createSession_WhenTimeConflict_ShouldThrowException() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.basePrice(BigDecimal.valueOf(250)).movieId(1L).hallId(1L).build();

		when(sessionService.createSession(request))
				.thenThrow(new SessionTimeConflictException(1L, request.getStartTime()));

		assertThrows(SessionTimeConflictException.class, () -> adminSessionController.createSession(request));
		verify(sessionService).createSession(request);
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionAdminResponse response = createSessionResponse(1L, "Test Movie");

		when(sessionService.getSessionForAdmin(1L)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
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
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie");
		response.setBasePrice(BigDecimal.valueOf(300));

		when(sessionService.updateSession(1L, request)).thenReturn(response);

		ResponseEntity<SessionAdminResponse> result = adminSessionController.updateSession(1L, request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		SessionAdminResponse body = result.getBody();
		assertNotNull(body);
		assertEquals(BigDecimal.valueOf(300), body.getBasePrice());
		verify(sessionService).updateSession(1L, request);
	}

	@Test
	void updateSession_WhenNotFound_ShouldThrowException() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		when(sessionService.updateSession(999L, request)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.updateSession(999L, request));
		verify(sessionService).updateSession(999L, request);
	}

	@Test
	void updateSession_WhenTimeConflict_ShouldThrowException() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.build();

		when(sessionService.updateSession(1L, request))
				.thenThrow(new SessionTimeConflictException(1L, request.getStartTime()));

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
	void cancelSession_WhenTimeConflict_ShouldThrowException() {
		doThrow(new SessionTimeConflictException(1L, LocalDateTime.now())).when(sessionService).cancelSession(1L);

		assertThrows(SessionTimeConflictException.class, () -> adminSessionController.cancelSession(1L));
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
	void reactivateSession_WhenTimeConflict_ShouldThrowException() {
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
	void deleteSession_WhenNotFound_ShouldThrowException() {
		doThrow(new SessionNotFoundException(1L)).when(sessionService).deleteSession(1L);

		assertThrows(SessionNotFoundException.class, () -> adminSessionController.deleteSession(1L));
		verify(sessionService).deleteSession(1L);
	}

	@Test
	void getSessions_WithoutFilters_ShouldReturnSessions() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startTime"));
		SessionAdminResponse response = createSessionResponse(1L, "Test Movie");
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response), pageable, 1);
		PageResponse<SessionAdminResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getSessionsForAdmin(any(), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionAdminResponse>> result = adminSessionController.getSessions(null, null, null,
				null, null, pageable);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		PageResponse<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(20, body.getSize());
		assertEquals(1, body.getTotalElements());
		verify(sessionService).getSessionsForAdmin(any(), eq(pageable));
	}

	@Test
	void getSessions_WithAllFilters_ShouldReturnFilteredSessions() {
		Pageable pageable = PageRequest.of(0, 20);
		Long hallId = 1L;
		String movieTitle = "Test";
		CinemaSessionStatus status = CinemaSessionStatus.SCHEDULED;
		LocalDate dateFrom = LocalDate.now();
		LocalDate dateTo = LocalDate.now().plusDays(7);

		SessionAdminResponse response = createSessionResponse(1L, "Test Movie");
		Page<SessionAdminResponse> page = new PageImpl<>(List.of(response), pageable, 1);
		PageResponse<SessionAdminResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getSessionsForAdmin(any(), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionAdminResponse>> result = adminSessionController.getSessions(hallId,
				movieTitle, status, dateFrom, dateTo, pageable);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		PageResponse<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		verify(sessionService).getSessionsForAdmin(any(), eq(pageable));
	}

	@Test
	void getSessions_WhenNoResults_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<SessionAdminResponse> emptyPage = Page.empty(pageable);
		PageResponse<SessionAdminResponse> emptyPageResponse = PageResponse.from(emptyPage);

		when(sessionService.getSessionsForAdmin(any(), eq(pageable))).thenReturn(emptyPageResponse);

		ResponseEntity<PageResponse<SessionAdminResponse>> result = adminSessionController.getSessions(null, null, null,
				null, null, pageable);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		PageResponse<SessionAdminResponse> body = result.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());
		verify(sessionService).getSessionsForAdmin(any(), eq(pageable));
	}
}