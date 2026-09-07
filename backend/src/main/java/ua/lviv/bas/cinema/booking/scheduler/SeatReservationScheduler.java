package ua.lviv.bas.cinema.booking.scheduler;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;

@Slf4j
@Component
public class SeatReservationScheduler {

    private final SeatReservationRepository seatReservationRepository;
    private final CacheManager cacheManager;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public SeatReservationScheduler(SeatReservationRepository seatReservationRepository, CacheManager cacheManager,
            PlatformTransactionManager transactionManager) {
        this.seatReservationRepository = seatReservationRepository;
        this.cacheManager = cacheManager;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Scheduled(fixedRateString = "${scheduler.seat-reservation.expiration-interval:60000}")
    @Transactional(readOnly = true)
    public void expireTempSeatReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatReservation> expiredReservations = seatReservationRepository
                .findByStatusAndReservedUntilBefore(ReservationStatus.PENDING, now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        Set<Long> affectedSessionIds = new HashSet<>();
        int deletedCount = 0;

        for (SeatReservation reservation : expiredReservations) {
            Long reservationId = reservation.getId();
            Long sessionId = reservation.getSession().getId();

            if (deleteIfStillExpired(reservationId, now) > 0) {
                affectedSessionIds.add(sessionId);
                deletedCount++;
            } else {
                log.debug("Seat reservation {} was concurrently extended, skipping expiry", reservationId);
            }
        }

        affectedSessionIds.forEach(sessionId -> evictCacheIfPresent("seatAvailability", sessionId));

        log.info("Deleted {} expired temporary seat reservations for sessions: {}", deletedCount,
                affectedSessionIds);
    }

    private int deleteIfStillExpired(Long reservationId, LocalDateTime cutoff) {
        return requiresNewTransactionTemplate.execute(status -> seatReservationRepository
                .deleteByIdIfStillExpired(reservationId, ReservationStatus.PENDING, cutoff));
    }

    @Scheduled(fixedRateString = "${scheduler.seat-reservation.cleanup-expired-interval:300000}")
    @Transactional
    public void cleanupExpiredReservations() {
        List<SeatReservation> expiredReservations = seatReservationRepository.findByStatus(ReservationStatus.EXPIRED);

        if (expiredReservations.isEmpty()) {
            return;
        }

        seatReservationRepository.deleteAll(expiredReservations);
        log.info("Deleted {} expired reservations", expiredReservations.size());
    }

    private void evictCacheIfPresent(String cacheName, Long key) {
        Cache cache = cacheManager.getCache(Objects.requireNonNull(cacheName, "Cache name must not be null"));
        if (cache != null) {
            cache.evict(Objects.requireNonNull(key, "Cache eviction key must not be null"));
        }
    }
}
