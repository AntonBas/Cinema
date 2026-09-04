package ua.lviv.bas.cinema.booking.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;

@Slf4j
@Component
public class BookingScheduler {
	private static final List<PaymentStatus> EVER_PAID_STATUSES = List.of(PaymentStatus.SUCCESS,
			PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED);

	private final BookingRepository bookingRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BonusLedgerService bonusLedgerService;
	private final CacheManager cacheManager;
	private final TransactionTemplate transactionTemplate;

	public BookingScheduler(BookingRepository bookingRepository,
			SeatReservationRepository seatReservationRepository, BonusLedgerService bonusLedgerService,
			CacheManager cacheManager, PlatformTransactionManager transactionManager) {
		this.bookingRepository = bookingRepository;
		this.seatReservationRepository = seatReservationRepository;
		this.bonusLedgerService = bonusLedgerService;
		this.cacheManager = cacheManager;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Scheduled(fixedRateString = "${scheduler.booking.expiration-interval:60000}")
	public void processExpiredBookings() {
		log.debug("Starting expired bookings processing");
		LocalDateTime now = LocalDateTime.now();
		List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

		if (expiredBookings.isEmpty()) {
			log.debug("No expired bookings found");
			return;
		}

		log.info("Found {} expired bookings to process", expiredBookings.size());

		int expiredCount = 0;
		for (Booking booking : expiredBookings) {
			Long bookingId = booking.getId();
			try {
				transactionTemplate.executeWithoutResult(status -> expireBooking(booking));
				expiredCount++;
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("Skipped expiring booking {} due to concurrent update, will retry on next run", bookingId);
			}
		}

		log.info("Successfully expired {} of {} bookings", expiredCount, expiredBookings.size());
	}

	private void expireBooking(Booking booking) {
		booking.setStatus(BookingStatus.EXPIRED);

		booking.getSeatReservations().forEach(sr -> {
			sr.setStatus(ReservationStatus.EXPIRED);
			sr.setBooking(null);
		});

		seatReservationRepository.saveAll(Objects.requireNonNull(booking.getSeatReservations(),
				"Booking seat reservations must not be null"));

		if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
			bonusLedgerService.refundPoints(booking);
		}

		bookingRepository.save(booking);

		evictCacheIfPresent("seatAvailability", booking.getSession().getId());
	}

	@Scheduled(cron = "${scheduler.booking.cleanup-cron:0 0 4 * * *}")
	@Transactional
	public void cleanupOldBookings() {
		log.debug("Starting old bookings cleanup");
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		int deletedCount = bookingRepository.deleteByStatusInAndCreatedDateBefore(
				List.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED), thirtyDaysAgo, EVER_PAID_STATUSES);

		if (deletedCount > 0) {
			log.info("Cleaned up {} old bookings", deletedCount);
		} else {
			log.debug("No old bookings to clean up");
		}
	}

	private void evictCacheIfPresent(String cacheName, Long key) {
		Cache cache = cacheManager.getCache(Objects.requireNonNull(cacheName, "Cache name must not be null"));
		if (cache != null) {
			cache.evict(Objects.requireNonNull(key, "Cache eviction key must not be null"));
		}
	}
}
