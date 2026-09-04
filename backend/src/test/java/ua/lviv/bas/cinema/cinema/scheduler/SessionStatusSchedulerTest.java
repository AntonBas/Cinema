package ua.lviv.bas.cinema.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionStatusSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private SessionStatusScheduler sessionStatusScheduler;

    @Test
    void updateSessionStatusesWhenNoneFoundShouldNotSave() {
        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of());
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of());

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void updateSessionStatusesShouldStartAndCompleteSessions() {
        var sessionToStart = Session.builder().id(1L).status(CinemaSessionStatus.SCHEDULED).build();
        var sessionToComplete = Session.builder().id(2L).status(CinemaSessionStatus.ONGOING).build();

        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of(sessionToStart));
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of(sessionToComplete));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionToStart));
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(sessionToComplete));

        sessionStatusScheduler.updateSessionStatuses();

        assertThat(sessionToStart.getStatus()).isEqualTo(CinemaSessionStatus.ONGOING);
        assertThat(sessionToComplete.getStatus()).isEqualTo(CinemaSessionStatus.COMPLETED);
        verify(sessionRepository).save(sessionToStart);
        verify(sessionRepository).save(sessionToComplete);
    }

    @Test
    void updateSessionStatusesShouldSkipSessionThatWasConcurrentlyModified() {
        var conflictingSession = Session.builder().id(1L).status(CinemaSessionStatus.SCHEDULED).build();
        var okSession = Session.builder().id(2L).status(CinemaSessionStatus.SCHEDULED).build();

        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class)))
                .thenReturn(List.of(conflictingSession, okSession));
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(conflictingSession));
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(okSession));
        doThrow(new ObjectOptimisticLockingFailureException(Session.class, 1L)).when(sessionRepository)
                .save(conflictingSession);

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository).save(okSession);
        assertThat(okSession.getStatus()).isEqualTo(CinemaSessionStatus.ONGOING);
    }
}
