package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.repository.BookingRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

	private final BookingRepository bookingRepository;

	@Scheduled(fixedDelayString = "${scheduler.booking.expiration-interval:60000}")
	@Async("taskExecutor")
	@Transactional
	public void processExpiredBookings() {
		log.debug("Starting expired bookings processing");

		List<Booking> expiredBookings = bookingRepository.findByExpiresAtBeforeAndStatus(LocalDateTime.now(),
				BookingStatus.PENDING);

		if (expiredBookings.isEmpty()) {
			log.debug("No expired bookings found");
			return;
		}

		log.info("Found {} expired bookings to process", expiredBookings.size());

		expiredBookings.forEach(booking -> {
			booking.setStatus(BookingStatus.EXPIRED);
			booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.EXPIRED));
		});

		bookingRepository.saveAll(expiredBookings);
		log.info("Successfully expired {} bookings", expiredBookings.size());
	}

	@Scheduled(cron = "${scheduler.booking.cleanup-cron:0 0 4 * * *}")
	@Async("taskExecutor")
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

	@Scheduled(cron = "${scheduler.booking.reminder-cron:0 */10 * * * *}")
	@Async("taskExecutor")
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
}