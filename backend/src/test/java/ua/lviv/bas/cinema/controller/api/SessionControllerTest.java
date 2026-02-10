package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
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
		return SessionScheduleResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).movieId(1L)
				.movieTitle("Test Movie").moviePosterFileName("poster.jpg").movieAgeRating("PG-13").movieDuration(120)
				.hallId(1L).hallName("Hall 1").availableSeats(80).hallCapacity(100)
				.status(CinemaSessionStatus.SCHEDULED).build();
	}

	@Test
	void getScheduleSessions_ShouldReturnSessionsWithPagination() {
		SessionFilterRequest filter = new SessionFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto), pageable, 1);

		when(sessionService.getScheduleSessions(any(SessionFilterRequest.class), any(Pageable.class)))
				.thenReturn(sessionPage);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController.getScheduleSessions(filter,
				pageable);

		assertEquals(200, response.getStatusCode().value());
		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(1L, body.getContent().get(0).getId());
		verify(sessionService).getScheduleSessions(any(SessionFilterRequest.class), any(Pageable.class));
	}

	@Test
	void getSessionById_ShouldReturnSessionForPublic() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);

		when(sessionService.getSessionByIdForPublic(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionScheduleResponse> response = sessionController.getSessionById(1L);

		assertEquals(200, response.getStatusCode().value());
		SessionScheduleResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		assertEquals("Test Movie", body.getMovieTitle());
		assertEquals("Hall 1", body.getHallName());
		assertEquals(80, body.getAvailableSeats());
		assertEquals(100, body.getHallCapacity());
		verify(sessionService).getSessionByIdForPublic(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionByIdForPublic(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(999L));
		verify(sessionService).getSessionByIdForPublic(999L);
	}

	@Test
	void getScheduleSessions_WithEmptyResult_ShouldReturnEmptyPage() {
		SessionFilterRequest filter = new SessionFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);
		Page<SessionScheduleResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(sessionService.getScheduleSessions(any(SessionFilterRequest.class), any(Pageable.class)))
				.thenReturn(emptyPage);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController.getScheduleSessions(filter,
				pageable);

		assertEquals(200, response.getStatusCode().value());
		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		verify(sessionService).getScheduleSessions(any(SessionFilterRequest.class), any(Pageable.class));
	}
}