package ua.lviv.bas.cinema.payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.service.PaymentService;

@Slf4j
@Component
public class PaymentScheduler {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BonusLedgerService bonusLedgerService;
	private final PaymentService paymentService;
	private final PaymentGatewayService paymentGatewayService;
	private final CacheManager cacheManager;
	private final TransactionTemplate transactionTemplate;

	@Value("${payment.processing-timeout-minutes:15}")
	private int processingTimeoutMinutes;

	public PaymentScheduler(PaymentRepository paymentRepository, BookingRepository bookingRepository,
			SeatReservationRepository seatReservationRepository, BonusLedgerService bonusLedgerService,
			PaymentService paymentService, PaymentGatewayService paymentGatewayService, CacheManager cacheManager,
			PlatformTransactionManager transactionManager) {
		this.paymentRepository = paymentRepository;
		this.bookingRepository = bookingRepository;
		this.seatReservationRepository = seatReservationRepository;
		this.bonusLedgerService = bonusLedgerService;
		this.paymentService = paymentService;
		this.paymentGatewayService = paymentGatewayService;
		this.cacheManager = cacheManager;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Scheduled(fixedRateString = "${scheduler.payment.expiration-interval:300000}")
	public void processExpiredPayments() {
		log.debug("Starting expired payments processing");
		LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
		List<Payment> expiredPayments = paymentRepository
				.findByStatusAndCreatedDateBeforeWithBookingDetails(PaymentStatus.PENDING, cutoffTime);

		if (expiredPayments.isEmpty()) {
			log.debug("No expired payments found");
			return;
		}

		log.info("Found {} expired payments to process", expiredPayments.size());

		int expiredCount = 0;
		for (var payment : expiredPayments) {
			Long paymentId = payment.getId();
			try {
				transactionTemplate.executeWithoutResult(status -> expirePayment(paymentId));
				expiredCount++;
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("Skipped expiring payment {} due to concurrent update, will retry on next run", paymentId);
			}
		}

		log.info("Successfully expired {} of {} payments", expiredCount, expiredPayments.size());
	}

	private void expirePayment(Long paymentId) {
		var payment = paymentRepository.findById(paymentId).orElse(null);
		if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
			log.debug("Payment {} no longer PENDING (already resolved concurrently), skipping expiration", paymentId);
			return;
		}

		payment.setStatus(PaymentStatus.EXPIRED);
		paymentRepository.save(payment);

		var booking = payment.getBooking();
		if (booking.getStatus() == BookingStatus.PENDING) {
			booking.setStatus(BookingStatus.EXPIRED);
			booking.getSeatReservations().forEach(sr -> {
				sr.setStatus(ReservationStatus.EXPIRED);
				sr.setBooking(null);
			});
			seatReservationRepository.saveAll(Objects.requireNonNull(booking.getSeatReservations(),
					"Payment booking seat reservations must not be null"));

			if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
				bonusLedgerService.refundPoints(booking);
			}

			bookingRepository.save(booking);
			evictCacheIfPresent("seatAvailability", booking.getSession().getId());
		}
	}

	@Scheduled(fixedRateString = "${scheduler.payment.processing-reconciliation-interval:600000}")
	public void reconcileStuckProcessingPayments() {
		log.debug("Starting stuck PROCESSING payments reconciliation");
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
		List<Payment> stuckPayments = paymentRepository
				.findByStatusAndLastModifiedDateBefore(PaymentStatus.PROCESSING, cutoff);

		if (stuckPayments.isEmpty()) {
			log.debug("No payments stuck in PROCESSING");
			return;
		}

		log.info("Found {} payment(s) stuck in PROCESSING, attempting to reconcile with LiqPay", stuckPayments.size());

		for (var payment : stuckPayments) {
			try {
				reconcile(payment);
			} catch (Exception e) {
				log.error("Failed to reconcile PROCESSING payment {}, will retry on next run", payment.getId(), e);
			}
		}
	}

	private void reconcile(Payment payment) {
		var check = paymentGatewayService.checkPaymentStatus(payment.getLiqpayOrderId());

		switch (check.status()) {
			case SUCCESS -> {
				paymentService.processSuccess(payment, check.rawData());
				log.info("Reconciled payment {} from PROCESSING to SUCCESS - confirmed by LiqPay", payment.getId());
			}
			case FAILED -> {
				paymentService.processFailure(payment, check.rawData());
				log.warn("Reconciled payment {} from PROCESSING to FAILED - confirmed by LiqPay", payment.getId());
			}
			case STILL_PROCESSING, UNKNOWN -> log.debug(
					"Payment {} still not resolved at LiqPay (gateway status: {}), will retry on next run",
					payment.getId(), check.status());
		}
	}

	@Scheduled(cron = "${scheduler.payment.cleanup-cron:0 0 5 * * *}")
	@Transactional
	public void cleanupOldPayments() {
		log.debug("Starting old payments cleanup");
		LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
		List<Payment> oldPayments = paymentRepository
				.findByStatusInAndCreatedDateBefore(List.of(PaymentStatus.FAILED, PaymentStatus.EXPIRED),
						ninetyDaysAgo);

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
