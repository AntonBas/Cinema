package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.service.cinema.SessionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    private SessionScheduleResponse createSessionScheduleResponse() {
        return new SessionScheduleResponse(1L, LocalDateTime.of(2024, 1, 15, 18, 0),
                LocalDateTime.of(2024, 1, 15, 20, 0), new BigDecimal("250.00"), 80, 1L, "Test Movie", "poster.jpg",
                "PG-13", 120, 1L, "Hall 1", 100);
    }

    @Test
    void getScheduleWithoutFiltersShouldReturnSessions() {
        List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse());

        when(sessionService.getSchedule(null, null, null)).thenReturn(sessionList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.getFirst().movieTitle()).isEqualTo("Test Movie");

        verify(sessionService).getSchedule(null, null, null);
    }

    @Test
    void getScheduleWithSearchTermShouldReturnFilteredSessions() {
        List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse());

        when(sessionService.getSchedule("Test", null, null)).thenReturn(sessionList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule("Test", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.getFirst().movieTitle()).isEqualTo("Test Movie");

        verify(sessionService).getSchedule("Test", null, null);
    }

    @Test
    void getScheduleWithDateShouldReturnFilteredSessions() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse());

        when(sessionService.getSchedule(null, date, null)).thenReturn(sessionList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(null, date, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.getFirst().startTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));

        verify(sessionService).getSchedule(null, date, null);
    }

    @Test
    void getScheduleWithAllFiltersShouldReturnFilteredSessions() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse());

        when(sessionService.getSchedule("Test", date, 1L)).thenReturn(sessionList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule("Test", date, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.getFirst().movieTitle()).isEqualTo("Test Movie");
        assertThat(body.getFirst().startTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));

        verify(sessionService).getSchedule("Test", date, 1L);
    }

    @Test
    void getScheduleWhenNoResultsShouldReturnEmptyList() {
        List<SessionScheduleResponse> emptyList = List.of();

        when(sessionService.getSchedule(null, null, null)).thenReturn(emptyList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().isEmpty();

        verify(sessionService).getSchedule(null, null, null);
    }

    @Test
    void getScheduleWithMovieIdShouldReturnFilteredSessions() {
        List<SessionScheduleResponse> sessionList = List.of(createSessionScheduleResponse());

        when(sessionService.getSchedule(null, null, 1L)).thenReturn(sessionList);

        ResponseEntity<List<SessionScheduleResponse>> response = sessionController.getSchedule(null, null, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SessionScheduleResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.getFirst().movieId()).isEqualTo(1L);

        verify(sessionService).getSchedule(null, null, 1L);
    }
}