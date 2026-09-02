package ua.lviv.bas.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionStatusSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionStatusScheduler sessionStatusScheduler;

    @Test
    void updateSessionStatusesWhenNoneFoundShouldNotSave() {
        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of());
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of());

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository, never()).saveAll(any());
    }

    @Test
    void updateSessionStatusesShouldStartAndCompleteSessions() {
        var sessionToStart = Session.builder().id(1L).status(CinemaSessionStatus.SCHEDULED).build();
        var sessionToComplete = Session.builder().id(2L).status(CinemaSessionStatus.ONGOING).build();

        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of(sessionToStart));
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of(sessionToComplete));

        sessionStatusScheduler.updateSessionStatuses();

        assertThat(sessionToStart.getStatus()).isEqualTo(CinemaSessionStatus.ONGOING);
        assertThat(sessionToComplete.getStatus()).isEqualTo(CinemaSessionStatus.COMPLETED);
        verify(sessionRepository).saveAll(List.of(sessionToStart));
        verify(sessionRepository).saveAll(List.of(sessionToComplete));
    }
}
