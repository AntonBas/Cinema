package ua.lviv.bas.cinema.payment.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayCheckResult;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayStatus;
import ua.lviv.bas.cinema.payment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private PaymentScheduler paymentScheduler;

    private static final Long SESSION_ID = 10L;

    private Session testSession;

    @BeforeEach
    void setUp() {
        testSession = Session.builder().id(SESSION_ID).build();
        ReflectionTestUtils.setField(paymentScheduler, "processingTimeoutMinutes", 15);
    }

    @Test
    void processExpiredPaymentsWhenNoneFoundShouldDoNothing() {
        when(paymentRepository.findByStatusAndCreatedDateBeforeWithBookingDetails(eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        paymentScheduler.processExpiredPayments();

        verifyNoInteractions(seatReservationRepository, cacheManager);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processExpiredPaymentsShouldExpireBookingAndEvictCacheWhenBookingPending() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBeforeWithBookingDetails(eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(payment));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        paymentScheduler.processExpiredPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seat.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(seat.getBooking()).isNull();

        verify(seatReservationRepository).saveAll(List.of(seat));
        verify(cache, times(1)).evict(SESSION_ID);
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
    }

    @Test
    void processExpiredPaymentsWhenBookingNotPendingShouldNotTouchBookingOrCache() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.CONFIRMED)
                .seatReservations(List.of(seat)).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBeforeWithBookingDetails(eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(payment));

        paymentScheduler.processExpiredPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(seat.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        verifyNoInteractions(seatReservationRepository, cacheManager);
        verify(paymentRepository).save(payment);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void processExpiredPaymentsWhenBonusPointsUsedShouldRefundThem() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).bonusPointsUsed(75).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBeforeWithBookingDetails(eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(payment));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        paymentScheduler.processExpiredPayments();

        verify(bonusLedgerService).refundPoints(booking);
    }

    @Test
    void processExpiredPaymentsWhenPaymentConcurrentlyModifiedShouldSkipIt() {
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBeforeWithBookingDetails(eq(PaymentStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(payment));
        when(paymentRepository.save(payment))
                .thenThrow(new ObjectOptimisticLockingFailureException(Payment.class, 2L));

        paymentScheduler.processExpiredPayments();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        verifyNoInteractions(seatReservationRepository, cacheManager);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void reconcileStuckProcessingPaymentsWhenNoneFoundShouldDoNothing() {
        when(paymentRepository.findByStatusAndLastModifiedDateBefore(eq(PaymentStatus.PROCESSING),
                any(LocalDateTime.class))).thenReturn(List.of());

        paymentScheduler.reconcileStuckProcessingPayments();

        verifyNoInteractions(paymentGatewayService, paymentService);
    }

    @Test
    void reconcileStuckProcessingPaymentsWhenGatewayConfirmsSuccessShouldCallProcessSuccess() {
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PROCESSING)
                .liqpayOrderId("ORD_2").build();

        when(paymentRepository.findByStatusAndLastModifiedDateBefore(eq(PaymentStatus.PROCESSING),
                any(LocalDateTime.class))).thenReturn(List.of(payment));
        when(paymentGatewayService.checkPaymentStatus("ORD_2"))
                .thenReturn(new PaymentGatewayCheckResult(PaymentGatewayStatus.SUCCESS, Map.of("payment_id", "P1")));

        paymentScheduler.reconcileStuckProcessingPayments();

        verify(paymentService).processSuccess(payment, Map.of("payment_id", "P1"));
        verify(paymentService, never()).processFailure(any(), any());
    }

    @Test
    void reconcileStuckProcessingPaymentsWhenGatewayConfirmsFailureShouldCallProcessFailure() {
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PROCESSING)
                .liqpayOrderId("ORD_2").build();

        when(paymentRepository.findByStatusAndLastModifiedDateBefore(eq(PaymentStatus.PROCESSING),
                any(LocalDateTime.class))).thenReturn(List.of(payment));
        when(paymentGatewayService.checkPaymentStatus("ORD_2")).thenReturn(
                new PaymentGatewayCheckResult(PaymentGatewayStatus.FAILED, Map.of("err_description", "declined")));

        paymentScheduler.reconcileStuckProcessingPayments();

        verify(paymentService).processFailure(payment, Map.of("err_description", "declined"));
        verify(paymentService, never()).processSuccess(any(), any());
    }

    @Test
    void reconcileStuckProcessingPaymentsWhenGatewayStillProcessingShouldLeaveItAlone() {
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PROCESSING)
                .liqpayOrderId("ORD_2").build();

        when(paymentRepository.findByStatusAndLastModifiedDateBefore(eq(PaymentStatus.PROCESSING),
                any(LocalDateTime.class))).thenReturn(List.of(payment));
        when(paymentGatewayService.checkPaymentStatus("ORD_2"))
                .thenReturn(new PaymentGatewayCheckResult(PaymentGatewayStatus.STILL_PROCESSING, Map.of()));

        paymentScheduler.reconcileStuckProcessingPayments();

        verifyNoInteractions(paymentService);
    }

    @Test
    void cleanupOldPaymentsShouldDeleteFailedAndExpiredPaymentsOlderThanNinetyDays() {
        var oldPayment = Payment.builder().id(3L).status(PaymentStatus.FAILED).build();
        when(paymentRepository.findByStatusInAndCreatedDateBefore(
                eq(List.of(PaymentStatus.FAILED, PaymentStatus.EXPIRED)), any(LocalDateTime.class)))
                .thenReturn(List.of(oldPayment));

        paymentScheduler.cleanupOldPayments();

        verify(paymentRepository).deleteAll(List.of(oldPayment));
    }
}
