package ua.lviv.bas.cinema.booking.scheduler;

import java.time.LocalDateTime;
import java.util.List;

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
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.service.SeatReservationService;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;

@Slf4j
@Component
public class BookingScheduler {
	private static final List<PaymentStatus> EVER_PAID_STATUSES = List.of(PaymentStatus.SUCCESS,
			PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED);

	private final BookingRepository bookingRepository;
	private final SeatReservationService seatReservationService;
	private final BonusLedgerService bonusLedgerService;
	private final TransactionTemplate transactionTemplate;

	public BookingScheduler(BookingRepository bookingRepository, SeatReservationService seatReservationService,
			BonusLedgerService bonusLedgerService, PlatformTransactionManager transactionManager) {
		this.bookingRepository = bookingRepository;
		this.seatReservationService = seatReservationService;
		this.bonusLedgerService = bonusLedgerService;
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
			} catch (RuntimeException e) {
				log.error("Failed to expire booking {}, will retry on next run", bookingId, e);
			}
		}

		log.info("Successfully expired {} of {} bookings", expiredCount, expiredBookings.size());
	}

	private void expireBooking(Booking booking) {
		booking.setStatus(BookingStatus.EXPIRED);

		seatReservationService.releaseReservations(booking.getSeatReservations(), booking.getSession().getId());

		if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
			bonusLedgerService.refundPoints(booking);
		}

		bookingRepository.save(booking);
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
}
