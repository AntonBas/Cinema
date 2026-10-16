package ua.lviv.bas.cinema.ticket.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.ticket.email.TicketsEmailService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketsIssuedEmailListenerTest {

    @Mock
    private TicketsEmailService ticketsEmailService;

    @InjectMocks
    private TicketsIssuedEmailListener listener;

    @Test
    void onTicketsIssuedShouldSendTicketsEmailForBooking() {
        listener.onTicketsIssued(new TicketsIssuedEvent(7L));

        verify(ticketsEmailService).sendAndMarkSent(7L);
    }

    @Test
    void onTicketsIssuedWhenSendingFailsShouldNotPropagate() {
        doThrow(new ExternalServiceException("Email Service", new RuntimeException("Brevo down")))
                .when(ticketsEmailService).sendAndMarkSent(7L);

        assertThatCode(() -> listener.onTicketsIssued(new TicketsIssuedEvent(7L))).doesNotThrowAnyException();
    }
}
