package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
	void getScheduleSessions_WithoutFilters_ShouldReturnSessions() {
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "startTime"));
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> page = new PageImpl<>(List.of(sessionDto), pageable, 1);
		PageResponse<SessionScheduleResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getScheduleSessions(isNull(), isNull(), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController.getScheduleSessions(null,
				null, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(12, body.getSize());
		assertEquals(1, body.getTotalElements());

		verify(sessionService).getScheduleSessions(isNull(), isNull(), eq(pageable));
	}

	@Test
	void getScheduleSessions_WithSearchTerm_ShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "startTime"));
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> page = new PageImpl<>(List.of(sessionDto), pageable, 1);
		PageResponse<SessionScheduleResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getScheduleSessions(eq(searchTerm), isNull(), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController
				.getScheduleSessions(searchTerm, null, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Test Movie", body.getContent().get(0).getMovieTitle());

		verify(sessionService).getScheduleSessions(eq(searchTerm), isNull(), eq(pageable));
	}

	@Test
	void getScheduleSessions_WithDate_ShouldReturnFilteredSessions() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "startTime"));
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> page = new PageImpl<>(List.of(sessionDto), pageable, 1);
		PageResponse<SessionScheduleResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getScheduleSessions(isNull(), eq(date), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController.getScheduleSessions(null,
				date, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 0), body.getContent().get(0).getStartTime());

		verify(sessionService).getScheduleSessions(isNull(), eq(date), eq(pageable));
	}

	@Test
	void getScheduleSessions_WithAllFilters_ShouldReturnFilteredSessions() {
		String searchTerm = "Test";
		LocalDate date = LocalDate.of(2024, 1, 15);
		Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "startTime"));
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);
		Page<SessionScheduleResponse> page = new PageImpl<>(List.of(sessionDto), pageable, 1);
		PageResponse<SessionScheduleResponse> pageResponse = PageResponse.from(page);

		when(sessionService.getScheduleSessions(eq(searchTerm), eq(date), eq(pageable))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController
				.getScheduleSessions(searchTerm, date, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("Test Movie", body.getContent().get(0).getMovieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 0), body.getContent().get(0).getStartTime());

		verify(sessionService).getScheduleSessions(eq(searchTerm), eq(date), eq(pageable));
	}

	@Test
	void getScheduleSessions_WhenNoResults_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 12);
		Page<SessionScheduleResponse> emptyPage = Page.empty(pageable);
		PageResponse<SessionScheduleResponse> emptyPageResponse = PageResponse.from(emptyPage);

		when(sessionService.getScheduleSessions(isNull(), isNull(), eq(pageable))).thenReturn(emptyPageResponse);

		ResponseEntity<PageResponse<SessionScheduleResponse>> response = sessionController.getScheduleSessions(null,
				null, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<SessionScheduleResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());

		verify(sessionService).getScheduleSessions(isNull(), isNull(), eq(pageable));
	}

	@Test
	void getSessionById_ShouldReturnSessionForPublic() {
		SessionScheduleResponse sessionDto = createSessionScheduleDto(1L);

		when(sessionService.getSessionForPublic(1L)).thenReturn(sessionDto);

		ResponseEntity<SessionScheduleResponse> response = sessionController.getSessionById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		SessionScheduleResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		assertEquals("Test Movie", body.getMovieTitle());
		assertEquals("Hall 1", body.getHallName());
		assertEquals(80, body.getAvailableSeats());
		assertEquals(100, body.getHallCapacity());

		verify(sessionService).getSessionForPublic(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionService.getSessionForPublic(999L)).thenThrow(new SessionNotFoundException(999L));

		assertThrows(SessionNotFoundException.class, () -> sessionController.getSessionById(999L));
		verify(sessionService).getSessionForPublic(999L);
	}
}