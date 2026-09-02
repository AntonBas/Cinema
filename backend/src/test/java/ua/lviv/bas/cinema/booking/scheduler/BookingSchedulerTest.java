package ua.lviv.bas.cinema.booking.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private BookingScheduler bookingScheduler;

    private static final Long SESSION_ID = 10L;

    private Session testSession;

    @BeforeEach
    void setUp() {
        testSession = Session.builder().id(SESSION_ID).build();
    }

    @Test
    void processExpiredBookingsWhenNoneFoundShouldDoNothing() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        bookingScheduler.processExpiredBookings();

        verifyNoInteractions(seatReservationRepository, bonusLedgerService, cacheManager);
        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void processExpiredBookingsShouldExpireSeatsRefundBonusPointsAndEvictCache() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).bonusPointsUsed(50).build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        bookingScheduler.processExpiredBookings();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seat.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(seat.getBooking()).isNull();

        verify(seatReservationRepository).saveAll(List.of(seat));
        verify(bonusLedgerService).refundPoints(booking);
        verify(cacheManager).getCache("seatAvailability");
        verify(cacheManager).getCache("availableSeatsCount");
        verify(cache, times(2)).evict(SESSION_ID);
        verify(bookingRepository).saveAll(List.of(booking));
    }

    @Test
    void processExpiredBookingsWhenNoBonusPointsUsedShouldSkipRefund() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).bonusPointsUsed(0).build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        bookingScheduler.processExpiredBookings();

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void processExpiredPaymentsWhenNoneFoundShouldDoNothing() {
        when(paymentRepository.findByStatusAndCreatedDateBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        bookingScheduler.processExpiredPayments();

        verifyNoInteractions(seatReservationRepository, cacheManager);
        verify(paymentRepository, never()).saveAll(any());
    }

    @Test
    void processExpiredPaymentsShouldExpireBookingAndEvictCacheWhenBookingPending() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(payment));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        bookingScheduler.processExpiredPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seat.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(seat.getBooking()).isNull();

        verify(seatReservationRepository).saveAll(List.of(seat));
        verify(cache, times(2)).evict(SESSION_ID);
        verify(paymentRepository).saveAll(List.of(payment));
    }

    @Test
    void processExpiredPaymentsWhenBookingNotPendingShouldNotTouchBookingOrCache() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.CONFIRMED)
                .seatReservations(List.of(seat)).build();
        var payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStatusAndCreatedDateBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(payment));

        bookingScheduler.processExpiredPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(seat.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        verifyNoInteractions(seatReservationRepository, cacheManager);
        verify(paymentRepository).saveAll(List.of(payment));
    }
}
