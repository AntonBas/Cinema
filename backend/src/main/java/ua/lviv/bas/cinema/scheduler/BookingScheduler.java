package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {
	private final BookingRepository bookingRepository;
	private final PaymentRepository paymentRepository;
	private final BonusService bonusService;

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
			booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.EXPIRED));

			if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
				bonusService.refundBonusPointsForCancellation(booking);
			}
		}

		bookingRepository.saveAll(expiredBookings);
		log.info("Successfully expired {} bookings", expiredBookings.size());
	}

	@Scheduled(fixedRateString = "${scheduler.payment.expiration-interval:300000}")
	@Transactional
	public void processExpiredPayments() {
		log.debug("Starting expired payments processing");
		LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
		List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING,
				cutoffTime);

		if (expiredPayments.isEmpty()) {
			log.debug("No expired payments found");
			return;
		}

		log.info("Found {} expired payments to process", expiredPayments.size());

		for (Payment payment : expiredPayments) {
			payment.setStatus(PaymentStatus.EXPIRED);
			payment.setUpdatedAt(LocalDateTime.now());

			if (payment.getBooking().getStatus() == BookingStatus.PENDING) {
				payment.getBooking().setStatus(BookingStatus.EXPIRED);
				payment.getBooking().getBookedSeats().forEach(seat -> seat.setStatus(BookedSeatStatus.EXPIRED));
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
		int deletedCount = bookingRepository.deleteByStatusInAndCreatedAtBefore(
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
		List<Payment> oldPayments = paymentRepository
				.findByStatusInAndCreatedAtBefore(List.of(PaymentStatus.FAILED, PaymentStatus.EXPIRED), ninetyDaysAgo);

		if (!oldPayments.isEmpty()) {
			paymentRepository.deleteAll(oldPayments);
			log.info("Cleaned up {} old payments", oldPayments.size());
		} else {
			log.debug("No old payments to clean up");
		}
	}

	@Scheduled(cron = "${scheduler.booking.reminder-cron:0 */10 * * * *}")
	@Transactional(readOnly = true)
	public void sendExpirationReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime reminderTime = now.plusMinutes(5);
		List<Booking> soonToExpire = bookingRepository.findByExpiresAtBetweenAndStatus(now, reminderTime,
				BookingStatus.PENDING);

		if (!soonToExpire.isEmpty()) {
			log.info("Found {} bookings expiring soon", soonToExpire.size());
		}
	}

	@Scheduled(cron = "${scheduler.payment.daily-stats:0 0 23 * * *}")
	@Transactional(readOnly = true)
	public void generateDailyPaymentStatistics() {
		LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
		LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

		long totalPayments = paymentRepository.countByCreatedAtBetween(startOfDay, endOfDay);
		long completedPayments = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
		long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
		long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);

		log.info("Daily payment statistics:");
		log.info("  Total payments: {}", totalPayments);
		log.info("  Completed: {}", completedPayments);
		log.info("  Pending: {}", pendingPayments);
		log.info("  Failed: {}", failedPayments);
	}
}