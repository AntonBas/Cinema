package ua.lviv.bas.cinema.payment.scheduler;

import java.time.Duration;
import java.time.Instant;
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
import ua.lviv.bas.cinema.payment.service.LatePaymentRefundService;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayStatus;
import ua.lviv.bas.cinema.payment.service.PaymentService;
import ua.lviv.bas.cinema.payment.service.PaymentSuccessOrchestrator;

@Slf4j
@Component
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BonusLedgerService bonusLedgerService;
    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentSuccessOrchestrator paymentSuccessOrchestrator;
    private final LatePaymentRefundService latePaymentRefundService;
    private final CacheManager cacheManager;
    private final TransactionTemplate transactionTemplate;

    private static final List<PaymentStatus> ACTIVE_STATUSES = List.of(PaymentStatus.PENDING,
            PaymentStatus.PROCESSING);
    private static final List<BookingStatus> EXPIRABLE_BOOKING_STATUSES = List.of(BookingStatus.PENDING,
            BookingStatus.EXPIRED, BookingStatus.CANCELLED);
    private static final List<BookingStatus> UNFULFILLABLE_BOOKING_STATUSES = List.of(BookingStatus.EXPIRED,
            BookingStatus.CANCELLED);

    @Value("${payment.processing-timeout-minutes:15}")
    private int processingTimeoutMinutes;

    @Value("${payment.orchestration-stuck-timeout-minutes:15}")
    private int orchestrationStuckTimeoutMinutes;

    public PaymentScheduler(PaymentRepository paymentRepository, BookingRepository bookingRepository,
            SeatReservationRepository seatReservationRepository, BonusLedgerService bonusLedgerService,
            PaymentService paymentService, PaymentGatewayService paymentGatewayService,
            PaymentSuccessOrchestrator paymentSuccessOrchestrator, LatePaymentRefundService latePaymentRefundService,
            CacheManager cacheManager, PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.bonusLedgerService = bonusLedgerService;
        this.paymentService = paymentService;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentSuccessOrchestrator = paymentSuccessOrchestrator;
        this.latePaymentRefundService = latePaymentRefundService;
        this.cacheManager = cacheManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedRateString = "${scheduler.payment.expiration-interval:300000}")
    public void processExpiredPayments() {
        log.debug("Starting expired payments processing");
        Instant now = Instant.now();
        List<Payment> expiredPayments = paymentRepository.findByStatusInAndBookingStatusInAndBookingExpiredBefore(
                ACTIVE_STATUSES, EXPIRABLE_BOOKING_STATUSES, now);

        if (expiredPayments.isEmpty()) {
            log.debug("No expired payments found");
            return;
        }

        log.info("Found {} expired payments to process", expiredPayments.size());

        Instant processingGraceCutoff = now.minus(Duration.ofMinutes(processingTimeoutMinutes));
        int expiredCount = 0;
        for (var payment : expiredPayments) {
            Long paymentId = payment.getId();
            try {
                if (resolveAtGatewayBeforeExpiry(payment, processingGraceCutoff)) {
                    continue;
                }
                if (Boolean.TRUE.equals(transactionTemplate.execute(status -> expirePayment(payment)))) {
                    expiredCount++;
                }
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Skipped expiring payment {} due to concurrent update, will retry on next run", paymentId);
            } catch (RuntimeException e) {
                log.error("Failed to expire payment {}, will retry on next run", paymentId, e);
            }
        }

        log.info("Successfully expired {} of {} payments", expiredCount, expiredPayments.size());
    }

    private boolean resolveAtGatewayBeforeExpiry(Payment payment, Instant processingGraceCutoff) {
        var check = paymentGatewayService.checkPaymentStatus(payment.getLiqpayOrderId());
        if (check.status() == PaymentGatewayStatus.SUCCESS) {
            log.info("Payment {} is already paid at LiqPay, completing it instead of expiring", payment.getId());
            paymentService.processSuccess(payment, check.rawData());
            return true;
        }
        if (check.status() == PaymentGatewayStatus.STILL_PROCESSING
                && payment.getBooking().getExpiresAt().isAfter(processingGraceCutoff)) {
            log.info("Payment {} is still processing at LiqPay, postponing its expiry", payment.getId());
            return true;
        }
        return false;
    }

    private boolean expirePayment(Payment payment) {
        if (paymentRepository.updateStatusIfCurrentIn(payment.getId(), ACTIVE_STATUSES, PaymentStatus.EXPIRED) == 0) {
            log.info("Payment {} changed status concurrently, not expiring it", payment.getId());
            return false;
        }
        payment.setStatus(PaymentStatus.EXPIRED);

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
        return true;
    }

    @Scheduled(fixedRateString = "${scheduler.payment.processing-reconciliation-interval:600000}")
    public void reconcileStuckProcessingPayments() {
        log.debug("Starting stuck PROCESSING payments reconciliation");
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(processingTimeoutMinutes));
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

    @Scheduled(fixedRateString = "${scheduler.payment.orchestration-reconciliation-interval:600000}")
    public void reconcileStuckSuccessfulPayments() {
        log.debug("Starting stuck post-payment orchestration reconciliation");
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(orchestrationStuckTimeoutMinutes));
        List<Payment> stuckPayments = paymentRepository.findByStatusAndBookingStatusAndLastModifiedDateBefore(
                PaymentStatus.SUCCESS, BookingStatus.PENDING, cutoff);

        if (stuckPayments.isEmpty()) {
            log.debug("No payments with incomplete post-payment orchestration");
            return;
        }

        log.warn("Found {} payment(s) SUCCESS with booking still PENDING, retrying post-payment orchestration",
                stuckPayments.size());

        for (var payment : stuckPayments) {
            try {
                paymentSuccessOrchestrator.handle(payment.getId());
                log.info("Recovered post-payment orchestration for payment {}", payment.getId());
            } catch (Exception e) {
                log.error("Failed to recover post-payment orchestration for payment {}, will retry on next run",
                        payment.getId(), e);
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.payment.orchestration-reconciliation-interval:600000}")
    public void refundUnfulfillableSuccessfulPayments() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(orchestrationStuckTimeoutMinutes));
        List<Payment> unfulfillable = paymentRepository.findWithoutTicketsByStatusAndBookingStatusIn(
                PaymentStatus.SUCCESS, UNFULFILLABLE_BOOKING_STATUSES, cutoff);

        for (var payment : unfulfillable) {
            try {
                paymentService.refundUnfulfillableSuccess(payment.getId());
            } catch (Exception e) {
                log.error("Failed to refund unfulfillable payment {}, will retry on next run", payment.getId(), e);
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.payment.refund-retry-interval:600000}")
    public void retryRequiredRefunds() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(processingTimeoutMinutes));
        List<Payment> pendingRefunds = paymentRepository
                .findByStatusAndLastModifiedDateBefore(PaymentStatus.REFUND_REQUIRED, cutoff);

        for (var payment : pendingRefunds) {
            try {
                latePaymentRefundService.refund(payment.getId());
            } catch (Exception e) {
                log.error("Failed to retry automatic refund of payment {}, will retry on next run", payment.getId(), e);
            }
        }
    }

    @Scheduled(cron = "${scheduler.payment.cleanup-cron:0 0 5 * * *}")
    @Transactional
    public void cleanupOldPayments() {
        log.debug("Starting old payments cleanup");
        Instant ninetyDaysAgo = Instant.now().minus(Duration.ofDays(90));
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
