package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCreationService {
	private final TicketRepository ticketRepository;
	private final NumberGeneratorService numberGenerator;

	@Transactional
	public Ticket createTicket(Ticket ticket) {
		ticket.setUniqueCode(numberGenerator.generateTicketCode());
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setPurchaseTime(LocalDateTime.now());

		Ticket savedTicket = ticketRepository.save(ticket);
		log.info("Created ticket {} for booking {}", savedTicket.getId(), ticket.getBooking().getId());

		return savedTicket;
	}

	@Transactional
	public List<Ticket> createTickets(List<Ticket> tickets) {
		for (Ticket ticket : tickets) {
			ticket.setUniqueCode(numberGenerator.generateTicketCode());
			ticket.setStatus(TicketStatus.ACTIVE);
			ticket.setPurchaseTime(LocalDateTime.now());
		}

		List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
		log.info("Created {} tickets", savedTickets.size());

		return savedTickets;
	}
}