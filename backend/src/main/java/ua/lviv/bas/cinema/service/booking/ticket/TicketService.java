package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {
	private final TicketRepository ticketRepository;
	private final TicketValidationService validationService;
	private final QRCodeService qrCodeService;
	private final EmailService emailService;
	private final NumberGeneratorService numberGenerator;

	@Value("${app.ticket.qr.size:200}")
	private int qrCodeSize;

	@Value("${app.ticket.base-url}")
	private String ticketBaseUrl;

	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = new ArrayList<>();

		for (BookedSeat bookedSeat : booking.getBookedSeats()) {
			Ticket ticket = Ticket.builder().booking(booking).user(booking.getUser())
					.ticketType(bookedSeat.getTicketType()).payment(payment).originalPrice(bookedSeat.getSeatPrice())
					.finalPrice(bookedSeat.getSeatPrice()).uniqueCode(numberGenerator.generateTicketCode())
					.status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now()).build();

			tickets.add(ticket);
		}

		List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
		log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());

		return savedTickets;
	}

	public void sendTicketsToUser(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

		if (tickets.isEmpty()) {
			log.warn("No tickets found for booking {}", booking.getId());
			return;
		}

		User user = booking.getUser();
		String bookingNumber = numberGenerator.generateBookingNumber(booking);
		String movieTitle = booking.getSession().getMovie().getTitle();
		String sessionTime = booking.getSession().getStartTime()
				.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
		String hallName = booking.getSession().getHall().getName();

		String seatInfo = booking.getBookedSeats().stream().map(bookedSeat -> {
			Seat seat = bookedSeat.getSeat();
			return String.format("Row %d Seat %d (%s)", seat.getRow(), seat.getNumber(),
					bookedSeat.getTicketType().getDisplayName());
		}).collect(Collectors.joining(", "));

		try {
			emailService.sendTicketsEmail(user.getEmail(), bookingNumber, movieTitle, sessionTime, hallName,
					booking.getFinalPrice(), "Credit Card", seatInfo);

			log.info("Tickets email sent to {} for booking {}", user.getEmail(), booking.getId());
		} catch (Exception e) {
			log.error("Failed to send tickets email for booking {}: {}", booking.getId(), e.getMessage(), e);
		}
	}

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