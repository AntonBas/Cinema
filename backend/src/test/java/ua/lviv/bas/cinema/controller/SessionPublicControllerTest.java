package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionPublicControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionPublicController sessionPublicController;

	private SessionScheduleResponse createSessionScheduleDto(Long id) {
		return SessionScheduleResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).availableSeats(100)
				.movieId(1L).movieTitle("Test Movie").moviePosterFileName("poster.jpg").movieAgeRating("PG-13")
				.movieDuration(120).hallId(1L).hallName("Hall 1").hallCapacity("100/100").build();
	}

	@Test
	void getScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionPublicController
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

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionPublicController.getScheduleSessionsByDate(date,
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

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionPublicController.getScheduleSessionsByMovie(1L,
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
	void getUpcomingScheduleSessions_ShouldReturnScheduleSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getUpcomingScheduleSessions(eq(7), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionPublicController.getUpcomingScheduleSessions(7,
				PageRequest.of(0, 20));

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getUpcomingScheduleSessions(7, PageRequest.of(0, 20));
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);

		when(sessionService.getSessionByIdForPublic(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionScheduleResponse> response = sessionPublicController.getSessionById(1L);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionScheduleResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());

		verify(sessionService).getSessionByIdForPublic(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionByIdForPublic(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionPublicController.getSessionById(999L));
		verify(sessionService).getSessionByIdForPublic(999L);
	}
}