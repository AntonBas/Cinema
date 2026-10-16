package ua.lviv.bas.cinema.ticket.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.booking.BookingStatus;
import ua.lviv.bas.cinema.booking.BookingRepository;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketsEmailRetrySchedulerTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TicketsEmailService ticketsEmailService;

    @InjectMocks
    private TicketsEmailRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "retryIntervalMillis", 300_000L);
        ReflectionTestUtils.setField(scheduler, "giveUpAfterHours", 24);
    }

    @Test
    void retryShouldResendEveryPendingBookingAndContinueAfterFailure() {
        when(bookingRepository.findIdsAwaitingTicketsEmail(eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of()).thenReturn(List.of(1L, 2L));
        doThrow(new ExternalServiceException("Email Service", new RuntimeException("Brevo down")))
                .when(ticketsEmailService).sendAndMarkSent(1L);

        scheduler.retryUnsentTicketsEmails();

        verify(ticketsEmailService).sendAndMarkSent(1L);
        verify(ticketsEmailService).sendAndMarkSent(2L);
    }

    @Test
    void retryShouldOnlyQueryBookingsPastGracePeriodAndWithinGiveUpWindow() {
        when(bookingRepository.findIdsAwaitingTicketsEmail(eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of());
        var before = Instant.now();

        scheduler.retryUnsentTicketsEmails();

        verify(bookingRepository).findIdsAwaitingTicketsEmail(eq(BookingStatus.CONFIRMED),
                argThat(from -> !from.isAfter(before.minus(Duration.ofHours(24)).plusSeconds(5))
                        && from.isAfter(before.minus(Duration.ofHours(24)).minusSeconds(5))),
                argThat(to -> !to.isAfter(Instant.now().minus(Duration.ofMinutes(2)))));
        verify(ticketsEmailService, never()).sendAndMarkSent(any());
    }

    @Test
    void retryShouldNotResendBookingsPastGiveUpWindow() {
        when(bookingRepository.findIdsAwaitingTicketsEmail(eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of(9L)).thenReturn(List.of());

        scheduler.retryUnsentTicketsEmails();

        verify(ticketsEmailService, never()).sendAndMarkSent(9L);
    }
}
