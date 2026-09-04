package ua.lviv.bas.cinema.booking.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
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
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private PlatformTransactionManager transactionManager;

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
        verify(cache, times(1)).evict(SESSION_ID);
        verify(bookingRepository).save(booking);
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
    void cleanupOldBookingsShouldExcludeBookingsWithEverPaidPayments() {
        when(bookingRepository.deleteByStatusInAndCreatedDateBefore(
                eq(List.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED)), any(LocalDateTime.class), any()))
                .thenReturn(3);

        bookingScheduler.cleanupOldBookings();

        verify(bookingRepository).deleteByStatusInAndCreatedDateBefore(
                eq(List.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED)), any(LocalDateTime.class),
                eq(List.of(PaymentStatus.SUCCESS, PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED)));
    }
}
