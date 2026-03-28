package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;
	private final TicketValidationService validationService;
	private final QRCodeService qrCodeService;
	private final NumberGeneratorService numberGenerator;

	@Value("${app.ticket.qr.size:200}")
	private int qrCodeSize;

	@Value("${app.ticket.base-url}")
	private String ticketBaseUrl;

	@Transactional
	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = booking.getSeatReservations().stream()
				.map(bookedSeat -> buildTicket(booking, payment, bookedSeat)).collect(Collectors.toList());

		List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
		log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());
		return savedTickets;
	}

	private Ticket buildTicket(Booking booking, Payment payment, SeatReservation seatReservation) {
		return Ticket.builder().booking(booking).user(booking.getUser()).ticketType(seatReservation.getTicketType())
				.payment(payment).seatReservation(seatReservation).originalPrice(seatReservation.getSeatPrice())
				.finalPrice(seatReservation.getSeatPrice()).uniqueCode(numberGenerator.generateTicketCode())
				.status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now()).build();
	}

	@Caching(evict = { @CacheEvict(value = "tickets", key = "#ticketCode + '-' + #ticket.user.id"),
			@CacheEvict(value = "tickets", key = "#ticket.id + '-' + #ticket.user.id"),
			@CacheEvict(value = "tickets", allEntries = true) })
	@Transactional
	public void validateTicket(String ticketCode) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode).orElseThrow(TicketValidationException::notFound);

		validationService.validateTicketForEntry(ticket);

		ticket.setStatus(TicketStatus.USED);
		ticketRepository.save(ticket);
		log.info("Ticket {} validated and marked as used", ticketCode);
	}

	public byte[] generateTicketQRCode(String ticketCode) {
		String qrContent = ticketBaseUrl + "/api/tickets/validate/" + ticketCode;
		return qrCodeService.generateQRCode(qrContent, qrCodeSize);
	}

	@Transactional(readOnly = true)
	public boolean isTicketValid(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(validationService::isTicketValidForEntry)
				.orElse(false);
	}

	@Transactional(readOnly = true)
	public TicketStatus checkTicketStatus(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(Ticket::getStatus).orElse(null);
	}
}