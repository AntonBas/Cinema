package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {
	private final BookingRepository bookingRepository;
	private final PaymentRepository paymentRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BonusService bonusService;
	private final CacheManager cacheManager;

	@Scheduled(fixedRateString = "${scheduler.booking.expiration-interval:60000}")
	@Transactional
	public void processExpiredBookings() {
		log.debug("Starting expired bookings processing");
		LocalDateTime now = LocalDateTime.now();
		List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

		if (expiredBookings.isEmpty()) {
			log.debug("No expired bookings found");
			return;
		}

		log.info("Found {} expired bookings to process", expiredBookings.size());

		for (Booking booking : expiredBookings) {
			booking.setStatus(BookingStatus.EXPIRED);

			booking.getSeatReservations().forEach(sr -> {
				sr.setStatus(ReservationStatus.EXPIRED);
				sr.setBooking(null);
			});

			seatReservationRepository.saveAll(Objects.requireNonNull(booking.getSeatReservations(),
					"Booking seat reservations must not be null"));

			if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
				bonusService.refundPoints(booking);
			}

			evictCacheIfPresent("seatAvailability", booking.getSession().getId());
			evictCacheIfPresent("availableSeatsCount", booking.getSession().getId());
		}

		bookingRepository.saveAll(expiredBookings);
		log.info("Successfully expired {} bookings", expiredBookings.size());
	}

	@Scheduled(fixedRateString = "${scheduler.payment.expiration-interval:300000}")
	@Transactional
	public void processExpiredPayments() {
		log.debug("Starting expired payments processing");
		LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
		List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedDateBefore(PaymentStatus.PENDING,
				cutoffTime);

		if (expiredPayments.isEmpty()) {
			log.debug("No expired payments found");
			return;
		}

		log.info("Found {} expired payments to process", expiredPayments.size());

		for (Payment payment : expiredPayments) {
			payment.setStatus(PaymentStatus.EXPIRED);

			if (payment.getBooking().getStatus() == BookingStatus.PENDING) {
				payment.getBooking().setStatus(BookingStatus.EXPIRED);
				payment.getBooking().getSeatReservations().forEach(sr -> {
					sr.setStatus(ReservationStatus.EXPIRED);
					sr.setBooking(null);
				});
				seatReservationRepository.saveAll(Objects.requireNonNull(payment.getBooking().getSeatReservations(),
						"Payment booking seat reservations must not be null"));
				evictCacheIfPresent("seatAvailability", payment.getBooking().getSession().getId());
				evictCacheIfPresent("availableSeatsCount", payment.getBooking().getSession().getId());
			}
		}

		paymentRepository.saveAll(expiredPayments);
		log.info("Successfully expired {} payments", expiredPayments.size());
	}

	@Scheduled(cron = "${scheduler.booking.cleanup-cron:0 0 4 * * *}")
	@Transactional
	public void cleanupOldBookings() {
		log.debug("Starting old bookings cleanup");
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		int deletedCount = bookingRepository.deleteByStatusInAndCreatedDateBefore(
				List.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED), thirtyDaysAgo);

		if (deletedCount > 0) {
			log.info("Cleaned up {} old bookings", deletedCount);
		} else {
			log.debug("No old bookings to clean up");
		}
	}

	@Scheduled(cron = "${scheduler.payment.cleanup-cron:0 0 5 * * *}")
	@Transactional
	public void cleanupOldPayments() {
		log.debug("Starting old payments cleanup");
		LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
		List<Payment> oldPayments = paymentRepository.findByStatusInAndCreatedDateBefore(
				List.of(PaymentStatus.FAILED, PaymentStatus.EXPIRED), ninetyDaysAgo);

		if (!oldPayments.isEmpty()) {
			paymentRepository.deleteAll(oldPayments);
			log.info("Cleaned up {} old payments", oldPayments.size());
		} else {
			log.debug("No old payments to clean up");
		}
	}

	private void evictCacheIfPresent(String cacheName, Long key) {
		Cache cache = cacheManager.getCache(Objects.requireNonNull(cacheName, "Cache name must not be null"));
		if (cache != null) {
			cache.evict(Objects.requireNonNull(key, "Cache eviction key must not be null"));
		}
	}
}