package ua.lviv.bas.cinema.cinema.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.common.CinemaTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionStatusScheduler {

    private final SessionRepository sessionRepository;
    private final CacheManager cacheManager;

    @Scheduled(cron = "${scheduler.session-status.cron:0 */5 * * * *}")
    @Transactional
    public void updateSessionStatuses() {
        log.debug("Starting scheduled session status update");
        LocalDateTime now = CinemaTime.now();

        int startedCount = updateStatuses(sessionRepository.findSessionsToStart(now), CinemaSessionStatus.SCHEDULED,
                CinemaSessionStatus.ONGOING);
        int completedCount = updateStatuses(sessionRepository.findSessionsToComplete(now),
                CinemaSessionStatus.ONGOING, CinemaSessionStatus.COMPLETED);

        log.info("Session status update completed: {} started, {} completed", startedCount, completedCount);
        if (startedCount + completedCount > 0) {
            Stream.of("sessions", "adminSessions").map(cacheManager::getCache).filter(Objects::nonNull)
                    .forEach(Cache::clear);
        }
    }

    private int updateStatuses(List<Session> sessions, CinemaSessionStatus fromStatus, CinemaSessionStatus newStatus) {
        if (sessions.isEmpty()) {
            return 0;
        }
        List<Long> ids = sessions.stream().map(Session::getId).toList();
        int updatedCount = sessionRepository.updateStatusForIds(ids, fromStatus, newStatus);
        log.info("Transitioned {} session(s) to {}", updatedCount, newStatus);
        return updatedCount;
    }
}
