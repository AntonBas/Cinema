package ua.lviv.bas.cinema.booking.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatReservationSchedulerTest {

    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private SeatReservationScheduler seatReservationScheduler;

    @Test
    void expireTempSeatReservationsWhenNoneFoundShouldDoNothing() {
        when(seatReservationRepository.findByStatusAndReservedUntilBefore(eq(ReservationStatus.PENDING),
                any(LocalDateTime.class))).thenReturn(List.of());

        seatReservationScheduler.expireTempSeatReservations();

        verify(seatReservationRepository, never()).deleteAll(any());
        verifyNoInteractions(cacheManager);
    }

    @Test
    void expireTempSeatReservationsShouldDeleteAndEvictCachePerAffectedSession() {
        var sessionA = Session.builder().id(1L).build();
        var sessionB = Session.builder().id(2L).build();
        var reservationA = SeatReservation.builder().session(sessionA).status(ReservationStatus.PENDING).build();
        var reservationB = SeatReservation.builder().session(sessionB).status(ReservationStatus.PENDING).build();

        when(seatReservationRepository.findByStatusAndReservedUntilBefore(eq(ReservationStatus.PENDING),
                any(LocalDateTime.class))).thenReturn(List.of(reservationA, reservationB));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        seatReservationScheduler.expireTempSeatReservations();

        verify(seatReservationRepository).deleteAll(List.of(reservationA, reservationB));
        verify(cache, times(1)).evict(1L);
        verify(cache, times(1)).evict(2L);
        verify(cache, times(2)).evict(any());
    }

    @Test
    void expireTempSeatReservationsWhenReservationsShareSessionShouldEvictOncePerCache() {
        var session = Session.builder().id(1L).build();
        var reservationA = SeatReservation.builder().session(session).status(ReservationStatus.PENDING).build();
        var reservationB = SeatReservation.builder().session(session).status(ReservationStatus.PENDING).build();

        when(seatReservationRepository.findByStatusAndReservedUntilBefore(eq(ReservationStatus.PENDING),
                any(LocalDateTime.class))).thenReturn(List.of(reservationA, reservationB));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        seatReservationScheduler.expireTempSeatReservations();

        verify(cacheManager).getCache("seatAvailability");
        verify(cache, times(1)).evict(1L);
    }

    @Test
    void expireTempSeatReservationsWhenCacheAbsentShouldStillDeleteReservations() {
        var session = Session.builder().id(1L).build();
        var reservation = SeatReservation.builder().session(session).status(ReservationStatus.PENDING).build();

        when(seatReservationRepository.findByStatusAndReservedUntilBefore(eq(ReservationStatus.PENDING),
                any(LocalDateTime.class))).thenReturn(List.of(reservation));
        when(cacheManager.getCache(anyString())).thenReturn(null);

        seatReservationScheduler.expireTempSeatReservations();

        verify(seatReservationRepository).deleteAll(List.of(reservation));
        verifyNoInteractions(cache);
    }
}
