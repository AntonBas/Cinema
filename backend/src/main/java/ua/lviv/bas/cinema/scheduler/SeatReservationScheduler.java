package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatReservationScheduler {

	private final SeatReservationRepository seatReservationRepository;
	private final CacheManager cacheManager;

	@Scheduled(fixedRateString = "${scheduler.seat-reservation.expiration-interval:60000}")
	@Transactional
	public void expireTempSeatReservations() {
		LocalDateTime now = LocalDateTime.now();
		List<SeatReservation> expiredReservations = seatReservationRepository
				.findByStatusAndReservedUntilBefore(ReservationStatus.PENDING, now);

		if (expiredReservations.isEmpty()) {
			return;
		}

		Set<Long> affectedSessionIds = expiredReservations.stream().map(reservation -> reservation.getSession().getId())
				.collect(Collectors.toSet());

		seatReservationRepository.deleteAll(expiredReservations);

		affectedSessionIds.forEach(sessionId -> {
			evictCacheIfPresent("seatAvailability", sessionId);
			evictCacheIfPresent("availableSeatsCount", sessionId);
		});

		log.info("Deleted {} expired temporary seat reservations for sessions: {}", expiredReservations.size(),
				affectedSessionIds);
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