package ua.lviv.bas.cinema.service.booking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketOperationException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.infrastructure.QRCodeService;
import ua.lviv.bas.cinema.service.notification.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

	private final TicketRepository ticketRepository;
	private final TicketMapper ticketMapper;
	private final EmailService emailService;
	private final QRCodeService qrCodeService;
	private final BookingRepository bookingRepository;

	@Value("${app.ticket.qr.size:200}")
	private int qrCodeSize;

	@Value("${app.ticket.base-url}")
	private String ticketBaseUrl;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = new ArrayList<>();

		for (BookedSeat bookedSeat : booking.getBookedSeats()) {
			Ticket ticket = Ticket.builder().booking(booking).user(booking.getUser())
					.ticketType(bookedSeat.getTicketType()).payment(payment).originalPrice(bookedSeat.getSeatPrice())
					.finalPrice(bookedSeat.getSeatPrice()).uniqueCode(generateTicketCode()).status(TicketStatus.ACTIVE)
					.purchaseTime(LocalDateTime.now()).build();

			tickets.add(ticket);
		}

		List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
		log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());

		return savedTickets;
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicketById(Long ticketId, User user) {
		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> TicketValidationException.notFound());

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		TicketResponse response = ticketMapper.toTicketResponse(ticket);
		response.setQrCodeUrl(generateQrCodeUrl(ticket.getUniqueCode()));

		return response;
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> getUserTickets(User user, TicketStatus status) {
		List<Ticket> tickets;
		if (status != null) {
			tickets = ticketRepository.findByUserIdAndStatusOrderByPurchaseTimeDesc(user.getId(), status);
		} else {
			tickets = ticketRepository.findByUserIdOrderByPurchaseTimeDesc(user.getId());
		}

		return tickets.stream().map(ticket -> {
			TicketResponse response = ticketMapper.toTicketResponse(ticket);
			response.setQrCodeUrl(generateQrCodeUrl(ticket.getUniqueCode()));
			return response;
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> getBookingTickets(Long bookingId, User user) {
		bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);

		return tickets.stream().map(ticket -> {
			TicketResponse response = ticketMapper.toTicketResponse(ticket);
			response.setQrCodeUrl(generateQrCodeUrl(ticket.getUniqueCode()));
			return response;
		}).collect(Collectors.toList());
	}

	public void validateTicket(String ticketCode) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode).orElseThrow(TicketValidationException::notFound);

		validateTicketForEntry(ticket);

		ticket.setStatus(TicketStatus.USED);
		ticketRepository.save(ticket);

		log.info("Ticket {} validated and marked as used", ticketCode);
	}

	public byte[] generateTicketQRCode(String ticketCode) {
		String qrContent = ticketBaseUrl + "/api/tickets/validate/" + ticketCode;
		return qrCodeService.generateQRCode(qrContent, qrCodeSize);
	}

	public void sendTicketsToUser(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

		if (tickets.isEmpty()) {
			log.warn("No tickets found for booking {}", booking.getId());
			return;
		}

		User user = booking.getUser();
		String bookingNumber = generateBookingNumber(booking);
		String movieTitle = booking.getSession().getMovie().getTitle();
		String sessionTime = booking.getSession().getStartTime().format(DATE_FORMATTER);
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

	public void cancelTicketsForBooking(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

		if (!tickets.isEmpty()) {
			boolean anyChanged = false;

			for (Ticket ticket : tickets) {
				if (ticket.getStatus() == TicketStatus.ACTIVE) {
					ticket.setStatus(TicketStatus.CANCELLED);
					anyChanged = true;
				}
			}

			if (anyChanged) {
				ticketRepository.saveAll(tickets);
				log.info("Cancelled some tickets for booking {}", booking.getId());
			} else {
				log.debug("No active tickets to cancel for booking {}", booking.getId());
			}
		}
	}

	public void voidTicket(Long ticketId, User user) {
		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketValidationException::notFound);

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		if (ticket.getStatus() == TicketStatus.USED) {
			throw TicketOperationException.cannotVoidUsed();
		}

		if (ticket.getStatus() == TicketStatus.CANCELLED) {
			log.debug("Ticket {} is already cancelled", ticketId);
			return;
		}

		ticket.setStatus(TicketStatus.CANCELLED);
		ticketRepository.save(ticket);

		log.info("Ticket {} voided by user {}", ticketId, user.getId());
	}

	@Transactional(readOnly = true)
	public boolean isTicketValid(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(this::isTicketValidForEntry).orElse(false);
	}

	@Transactional(readOnly = true)
	public TicketStatus checkTicketStatus(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(Ticket::getStatus).orElse(null);
	}

	private String generateTicketCode() {
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		return "TKT-" + uuid;
	}

	private String generateQrCodeUrl(String ticketCode) {
		return ticketBaseUrl + "/api/tickets/" + ticketCode + "/qr";
	}

	private void validateTicketForEntry(Ticket ticket) {
		if (ticket.getStatus() == TicketStatus.USED) {
			throw TicketValidationException.alreadyUsed();
		}

		if (ticket.getStatus() == TicketStatus.CANCELLED) {
			throw new TicketValidationException("Ticket has been cancelled");
		}

		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			throw new TicketValidationException("Ticket is not active");
		}

		Session session = ticket.getBooking().getSession();
		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw TicketValidationException.sessionStarted();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now().minusHours(2))) {
			throw new TicketValidationException("Session ended more than 2 hours ago");
		}

		if (session.getStatus() == CinemaSessionStatus.CANCELLED) {
			throw new TicketValidationException("Session has been cancelled");
		}
	}

	private boolean isTicketValidForEntry(Ticket ticket) {
		try {
			validateTicketForEntry(ticket);
			return true;
		} catch (TicketValidationException e) {
			return false;
		}
	}

	private String generateBookingNumber(Booking booking) {
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
	}
}