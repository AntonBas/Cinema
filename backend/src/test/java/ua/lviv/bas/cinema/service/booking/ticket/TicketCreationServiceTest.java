package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class TicketCreationServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private TicketCreationService ticketCreationService;

	private Ticket testTicket;
	private Booking testBooking;

	@BeforeEach
	void setUp() {
		testBooking = new Booking();
		testBooking.setId(1L);

		testTicket = new Ticket();
		testTicket.setBooking(testBooking);
		testTicket.setOriginalPrice(new BigDecimal("100.00"));
		testTicket.setFinalPrice(new BigDecimal("100.00"));
	}

	@Test
	void createTicket_Success() {
		String ticketCode = "TKT-123456";
		when(numberGenerator.generateTicketCode()).thenReturn(ticketCode);
		when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

		Ticket result = ticketCreationService.createTicket(testTicket);

		assertThat(result).isNotNull();
		assertThat(result.getUniqueCode()).isEqualTo(ticketCode);
		assertThat(result.getStatus()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(result.getPurchaseTime()).isNotNull();
		verify(ticketRepository).save(testTicket);
	}

	@Test
	void createTickets_Success() {
		String ticketCode1 = "TKT-123456";
		String ticketCode2 = "TKT-789012";

		Ticket ticket1 = new Ticket();
		ticket1.setBooking(testBooking);

		Ticket ticket2 = new Ticket();
		ticket2.setBooking(testBooking);

		List<Ticket> tickets = Arrays.asList(ticket1, ticket2);

		when(numberGenerator.generateTicketCode()).thenReturn(ticketCode1, ticketCode2);
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		List<Ticket> result = ticketCreationService.createTickets(tickets);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getUniqueCode()).isEqualTo(ticketCode1);
		assertThat(result.get(1).getUniqueCode()).isEqualTo(ticketCode2);
		assertThat(result.get(0).getStatus()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(result.get(1).getStatus()).isEqualTo(TicketStatus.ACTIVE);
		verify(ticketRepository).saveAll(tickets);
	}

	@Test
	void createTickets_MultipleTickets() {
		Ticket ticket1 = new Ticket();
		ticket1.setBooking(testBooking);

		Ticket ticket2 = new Ticket();
		ticket2.setBooking(testBooking);

		Ticket ticket3 = new Ticket();
		ticket3.setBooking(testBooking);

		List<Ticket> tickets = Arrays.asList(ticket1, ticket2, ticket3);

		when(numberGenerator.generateTicketCode()).thenReturn("TKT-1", "TKT-2", "TKT-3");
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		List<Ticket> result = ticketCreationService.createTickets(tickets);

		assertThat(result).hasSize(3);
		for (Ticket ticket : result) {
			assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
			assertThat(ticket.getPurchaseTime()).isNotNull();
		}
	}
}