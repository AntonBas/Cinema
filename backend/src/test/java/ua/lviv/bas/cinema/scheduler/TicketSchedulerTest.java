package ua.lviv.bas.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketSchedulerTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketSpecification ticketSpecification;

    @InjectMocks
    private TicketScheduler ticketScheduler;

    private static final Specification<Ticket> NOOP_SPEC = (root, query, cb) -> null;

    @Test
    void markTicketsAsExpiredAfterSessionWhenNoneFoundShouldNotSave() {
        when(ticketSpecification.hasStatus(TicketStatus.ACTIVE)).thenReturn(NOOP_SPEC);
        when(ticketSpecification.hasSessionStatus(CinemaSessionStatus.COMPLETED)).thenReturn(NOOP_SPEC);
        when(ticketRepository.findAll(any(Specification.class))).thenReturn(List.of());

        ticketScheduler.markTicketsAsExpiredAfterSession();

        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void markTicketsAsExpiredAfterSessionShouldMarkFoundTicketsExpired() {
        var ticket = Ticket.builder().id(1L).status(TicketStatus.ACTIVE).build();

        when(ticketSpecification.hasStatus(TicketStatus.ACTIVE)).thenReturn(NOOP_SPEC);
        when(ticketSpecification.hasSessionStatus(CinemaSessionStatus.COMPLETED)).thenReturn(NOOP_SPEC);
        when(ticketRepository.findAll(any(Specification.class))).thenReturn(List.of(ticket));

        ticketScheduler.markTicketsAsExpiredAfterSession();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        verify(ticketRepository).saveAll(List.of(ticket));
    }

    @Test
    void cleanupRefundedTicketsWhenNoneFoundShouldNotDelete() {
        when(ticketSpecification.hasStatus(TicketStatus.REFUNDED)).thenReturn(NOOP_SPEC);
        when(ticketSpecification.purchaseTimeBefore(any(LocalDateTime.class))).thenReturn(NOOP_SPEC);
        when(ticketRepository.findAll(any(Specification.class))).thenReturn(List.of());

        ticketScheduler.cleanupRefundedTickets();

        verify(ticketRepository, never()).deleteAll(any());
    }

    @Test
    void cleanupRefundedTicketsShouldDeleteFoundTickets() {
        var ticket = Ticket.builder().id(1L).status(TicketStatus.REFUNDED).build();

        when(ticketSpecification.hasStatus(TicketStatus.REFUNDED)).thenReturn(NOOP_SPEC);
        when(ticketSpecification.purchaseTimeBefore(any(LocalDateTime.class))).thenReturn(NOOP_SPEC);
        when(ticketRepository.findAll(any(Specification.class))).thenReturn(List.of(ticket));

        ticketScheduler.cleanupRefundedTickets();

        verify(ticketRepository).deleteAll(List.of(ticket));
    }
}
