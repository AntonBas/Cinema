package ua.lviv.bas.cinema.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionStatusSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionStatusScheduler sessionStatusScheduler;

    @Test
    void updateSessionStatusesWhenNoneFoundShouldNotUpdate() {
        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of());
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of());

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository, never()).updateStatusForIds(any(), any(), any());
    }

    @Test
    void updateSessionStatusesShouldBulkStartAndCompleteSessions() {
        var sessionToStart = Session.builder().id(1L).status(CinemaSessionStatus.SCHEDULED).build();
        var sessionToComplete = Session.builder().id(2L).status(CinemaSessionStatus.ONGOING).build();

        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of(sessionToStart));
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class)))
                .thenReturn(List.of(sessionToComplete));
        when(sessionRepository.updateStatusForIds(List.of(1L), CinemaSessionStatus.SCHEDULED,
                CinemaSessionStatus.ONGOING)).thenReturn(1);
        when(sessionRepository.updateStatusForIds(List.of(2L), CinemaSessionStatus.ONGOING,
                CinemaSessionStatus.COMPLETED)).thenReturn(1);

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository).updateStatusForIds(List.of(1L), CinemaSessionStatus.SCHEDULED,
                CinemaSessionStatus.ONGOING);
        verify(sessionRepository).updateStatusForIds(List.of(2L), CinemaSessionStatus.ONGOING,
                CinemaSessionStatus.COMPLETED);
    }
}
