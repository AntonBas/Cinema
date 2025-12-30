package ua.lviv.bas.cinema.controller.api;

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

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.service.common.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private SessionController sessionController;

	private SessionScheduleResponse createSessionScheduleDto(Long id) {
		return SessionScheduleResponse.builder().id(id).startTime(LocalDateTime.of(2024, 1, 15, 18, 0))
				.endTime(LocalDateTime.of(2024, 1, 15, 20, 0)).basePrice(new BigDecimal("250.00")).movieId(1L)
				.movieTitle("Test Movie").moviePosterFileName("poster.jpg").movieAgeRating("PG-13").movieDuration(120)
				.hallId(1L).hallName("Hall 1").availableSeats(80).hallCapacity("80/100")
				.status(CinemaSessionStatus.SCHEDULED).build();
	}

	@Test
	void getScheduleSessions_ShouldReturnSessionsWithPagination() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessions(any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController
				.getScheduleSessions(PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessions(PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessionsByDate_ShouldReturnSessionsForSpecificDate() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByDate(eq(date), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByDate(date,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByDate(date, PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessionsByMovie_ShouldReturnSessionsForSpecificMovie() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByMovie(eq(1L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByMovie(1L,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByMovie(1L, PageRequest.of(0, 20));
	}

	@Test
	void getUpcomingScheduleSessions_ShouldReturnUpcomingSessionsWithDefaultDays() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getUpcomingScheduleSessions(eq(7), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getUpcomingScheduleSessions(7,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getUpcomingScheduleSessions(7, PageRequest.of(0, 20));
	}

	@Test
	void getUpcomingScheduleSessions_ShouldReturnUpcomingSessionsWithCustomDays() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getUpcomingScheduleSessions(eq(3), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getUpcomingScheduleSessions(3,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(1L, responseBody.getContent().get(0).getId());

		verify(sessionService).getUpcomingScheduleSessions(3, PageRequest.of(0, 20));
	}

	@Test
	void getSessionById_ShouldReturnSessionForPublic() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);

		when(sessionService.getSessionByIdForPublic(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionScheduleResponse> response = sessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionScheduleResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());
		assertEquals("Test Movie", responseBody.getMovieTitle());
		assertEquals("Hall 1", responseBody.getHallName());
		assertEquals(80, responseBody.getAvailableSeats());
		assertEquals("80/100", responseBody.getHallCapacity());

		verify(sessionService).getSessionByIdForPublic(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionByIdForPublic(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(999L));
		verify(sessionService).getSessionByIdForPublic(999L);
	}

	@Test
	void getSessionById_WhenSessionNotAvailableForPublic_ShouldThrowException() {

		when(sessionService.getSessionByIdForPublic(2L)).thenThrow(new SessionNotFoundException(2L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(2L));
		verify(sessionService).getSessionByIdForPublic(2L);
	}

	@Test
	void getScheduleSessionsByDate_WithDifferentDate_ShouldReturnSessions() {
		LocalDate futureDate = LocalDate.of(2024, 12, 25);
		SessionScheduleResponse sessionDto = createSessionScheduleDto(3L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByDate(eq(futureDate), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByDate(futureDate,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(3L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByDate(futureDate, PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessionsByMovie_WithDifferentMovieId_ShouldReturnSessions() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(4L);
		Page<SessionScheduleResponse> sessionPage = new PageImpl<>(List.of(sessionDto));

		when(sessionService.getScheduleSessionsByMovie(eq(2L), any(Pageable.class))).thenReturn(sessionPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController.getScheduleSessionsByMovie(2L,
				PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1, responseBody.getContent().size());
		assertEquals(4L, responseBody.getContent().get(0).getId());

		verify(sessionService).getScheduleSessionsByMovie(2L, PageRequest.of(0, 20));
	}

	@Test
	void getScheduleSessions_WithEmptyResult_ShouldReturnEmptyPage() {
		Page<SessionScheduleResponse> emptyPage = new PageImpl<>(List.of());

		when(sessionService.getScheduleSessions(any(Pageable.class))).thenReturn(emptyPage);

		ResponseEntity<Page<SessionScheduleResponse>> response = sessionController
				.getScheduleSessions(PageRequest.of(0, 20));

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Page<SessionScheduleResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(0, responseBody.getContent().size());

		verify(sessionService).getScheduleSessions(PageRequest.of(0, 20));
	}
}