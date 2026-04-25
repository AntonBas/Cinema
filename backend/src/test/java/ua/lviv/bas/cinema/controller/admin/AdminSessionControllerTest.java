package ua.lviv.bas.cinema.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.service.cinema.SessionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminSessionControllerTest {

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private AdminSessionController adminSessionController;

    private SessionResponse createSessionResponse(Long id, String title, BigDecimal basePrice) {
        return new SessionResponse(id, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4), basePrice,
                CinemaSessionStatus.SCHEDULED, 1L, title, 120, 1L, "Hall 1");
    }

    private SessionAdminResponse createSessionAdminResponse(Long id, String title, BigDecimal basePrice) {
        return new SessionAdminResponse(id, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4),
                basePrice, CinemaSessionStatus.SCHEDULED, 1L, title, 120, 1L, "Hall 1", 100, 0, BigDecimal.ZERO);
    }

    @Test
    void createSessionShouldCreateSuccessfully() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        BigDecimal price = BigDecimal.valueOf(250);
        SessionRequest request = new SessionRequest(startTime, price, 1L, 1L);

        SessionResponse response = createSessionResponse(1L, "Test Movie", price);

        when(sessionService.createSession(request)).thenReturn(response);

        SessionResponse result = adminSessionController.createSession(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.basePrice()).isEqualTo(price);
        verify(sessionService).createSession(request);
    }

    @Test
    void createSessionWhenTimeConflictShouldThrowException() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        SessionRequest request = new SessionRequest(startTime, BigDecimal.valueOf(250), 1L, 1L);

        when(sessionService.createSession(request))
                .thenThrow(new SessionTimeConflictException("Hall 1", request.startTime()));

        assertThrows(SessionTimeConflictException.class, () -> adminSessionController.createSession(request));
        verify(sessionService).createSession(request);
    }

    @Test
    void getSessionShouldReturnSession() {
        BigDecimal price = BigDecimal.valueOf(250);
        SessionResponse response = createSessionResponse(1L, "Test Movie", price);

        when(sessionService.getSession(1L)).thenReturn(response);

        SessionResponse result = adminSessionController.getSessionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.basePrice()).isEqualTo(price);
        verify(sessionService).getSession(1L);
    }

    @Test
    void getSessionWhenNotFoundShouldThrowException() {
        when(sessionService.getSession(999L)).thenThrow(new SessionNotFoundException(999L));

        assertThrows(SessionNotFoundException.class, () -> adminSessionController.getSessionById(999L));
        verify(sessionService).getSession(999L);
    }

    @Test
    void getSessionsShouldReturnSessions() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "startTime"));
        Long hallId = 1L;
        String movieTitle = "Test";
        CinemaSessionStatus status = CinemaSessionStatus.SCHEDULED;
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(7);
        BigDecimal price = BigDecimal.valueOf(250);

        SessionAdminResponse response = createSessionAdminResponse(1L, "Test Movie", price);
        Page<SessionAdminResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(sessionService.getSessions(hallId, movieTitle, status, dateFrom, dateTo, pageable)).thenReturn(page);

        PageResponse<SessionAdminResponse> result = adminSessionController.getSessions(hallId, movieTitle, status,
                dateFrom, dateTo, pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.number()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(sessionService).getSessions(hallId, movieTitle, status, dateFrom, dateTo, pageable);
    }

    @Test
    void getSessionsWithNullFiltersShouldReturnAllSessions() {
        Pageable pageable = PageRequest.of(0, 10);
        SessionAdminResponse response = createSessionAdminResponse(1L, "Test Movie", BigDecimal.valueOf(250));
        Page<SessionAdminResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(sessionService.getSessions(null, null, null, null, null, pageable)).thenReturn(page);

        PageResponse<SessionAdminResponse> result = adminSessionController.getSessions(null, null, null, null, null,
                pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        verify(sessionService).getSessions(null, null, null, null, null, pageable);
    }

    @Test
    void updateSessionShouldUpdateSuccessfully() {
        BigDecimal newPrice = BigDecimal.valueOf(300);
        SessionRequest request = new SessionRequest(null, newPrice, null, null);

        SessionResponse response = createSessionResponse(1L, "Test Movie", newPrice);

        when(sessionService.updateSession(1L, request)).thenReturn(response);

        SessionResponse result = adminSessionController.updateSession(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualTo(newPrice);
        verify(sessionService).updateSession(1L, request);
    }

    @Test
    void updateSessionWhenNotFoundShouldThrowException() {
        SessionRequest request = new SessionRequest(null, BigDecimal.valueOf(300), null, null);

        when(sessionService.updateSession(999L, request)).thenThrow(new SessionNotFoundException(999L));

        assertThrows(SessionNotFoundException.class, () -> adminSessionController.updateSession(999L, request));
        verify(sessionService).updateSession(999L, request);
    }

    @Test
    void updateSessionWhenTimeConflictShouldThrowException() {
        LocalDateTime newStartTime = LocalDateTime.now().plusHours(3);
        SessionRequest request = new SessionRequest(newStartTime, null, null, null);

        when(sessionService.updateSession(1L, request))
                .thenThrow(new SessionTimeConflictException("Hall 1", request.startTime()));

        assertThrows(SessionTimeConflictException.class, () -> adminSessionController.updateSession(1L, request));
        verify(sessionService).updateSession(1L, request);
    }

    @Test
    void cancelSessionShouldCancelSuccessfully() {
        adminSessionController.cancelSession(1L);

        verify(sessionService).cancelSession(1L);
    }

    @Test
    void cancelSessionWhenNotFoundShouldThrowException() {
        doThrow(new SessionNotFoundException(1L)).when(sessionService).cancelSession(1L);

        assertThrows(SessionNotFoundException.class, () -> adminSessionController.cancelSession(1L));
        verify(sessionService).cancelSession(1L);
    }

    @Test
    void reactivateSessionShouldReactivateSuccessfully() {
        adminSessionController.reactivateSession(1L);

        verify(sessionService).reactivateSession(1L);
    }

    @Test
    void reactivateSessionWhenNotFoundShouldThrowException() {
        doThrow(new SessionNotFoundException(1L)).when(sessionService).reactivateSession(1L);

        assertThrows(SessionNotFoundException.class, () -> adminSessionController.reactivateSession(1L));
        verify(sessionService).reactivateSession(1L);
    }

    @Test
    void deleteSessionShouldDeleteSuccessfully() {
        adminSessionController.deleteSession(1L);

        verify(sessionService).deleteSession(1L);
    }

    @Test
    void deleteSessionWhenNotFoundShouldThrowException() {
        doThrow(new SessionNotFoundException(1L)).when(sessionService).deleteSession(1L);

        assertThrows(SessionNotFoundException.class, () -> adminSessionController.deleteSession(1L));
        verify(sessionService).deleteSession(1L);
    }
}