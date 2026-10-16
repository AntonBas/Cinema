package ua.lviv.bas.cinema.cinema.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionStatusSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache sessionsCache;
    @Mock
    private Cache adminSessionsCache;

    @InjectMocks
    private SessionStatusScheduler sessionStatusScheduler;

    @Test
    void updateSessionStatusesWhenNoneFoundShouldNotUpdate() {
        when(sessionRepository.findSessionsToStart(any(LocalDateTime.class))).thenReturn(List.of());
        when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(List.of());

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository, never()).updateStatusForIds(any(), any(), any());
        verifyNoInteractions(cacheManager);
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
        when(cacheManager.getCache("sessions")).thenReturn(sessionsCache);
        when(cacheManager.getCache("adminSessions")).thenReturn(adminSessionsCache);

        sessionStatusScheduler.updateSessionStatuses();

        verify(sessionRepository).updateStatusForIds(List.of(1L), CinemaSessionStatus.SCHEDULED,
                CinemaSessionStatus.ONGOING);
        verify(sessionRepository).updateStatusForIds(List.of(2L), CinemaSessionStatus.ONGOING,
                CinemaSessionStatus.COMPLETED);
        verify(sessionsCache).clear();
        verify(adminSessionsCache).clear();
    }
}
