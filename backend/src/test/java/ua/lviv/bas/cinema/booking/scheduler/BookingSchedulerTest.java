package ua.lviv.bas.cinema.booking.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.service.SeatReservationService;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardConcurrentModificationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private BonusLedgerService bonusLedgerService;
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

        verifyNoInteractions(seatReservationService, bonusLedgerService);
        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void processExpiredBookingsShouldExpireSeatsRefundBonusPointsAndEvictCache() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).bonusPointsUsed(50).build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        bookingScheduler.processExpiredBookings();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);

        verify(seatReservationService).releaseReservations(List.of(seat), SESSION_ID);
        verify(bonusLedgerService).refundPoints(booking);
        verify(bookingRepository).save(booking);
    }

    @Test
    void processExpiredBookingsWhenNoBonusPointsUsedShouldSkipRefund() {
        var seat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var booking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(seat)).bonusPointsUsed(0).build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        bookingScheduler.processExpiredBookings();

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void processExpiredBookingsShouldContinueBatchWhenOneBookingFailsWithNonOptimisticLockException() {
        var failingSeat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var failingBooking = Booking.builder().id(1L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(failingSeat)).bonusPointsUsed(50).build();

        var okSeat = SeatReservation.builder().status(ReservationStatus.CONFIRMED).build();
        var okBooking = Booking.builder().id(2L).session(testSession).status(BookingStatus.PENDING)
                .seatReservations(List.of(okSeat)).bonusPointsUsed(0).build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(failingBooking, okBooking));
        doThrow(new BonusCardConcurrentModificationException(null)).when(bonusLedgerService)
                .refundPoints(failingBooking);

        bookingScheduler.processExpiredBookings();

        assertThat(okBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        verify(bookingRepository).save(okBooking);
        verify(bookingRepository, never()).save(failingBooking);
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
