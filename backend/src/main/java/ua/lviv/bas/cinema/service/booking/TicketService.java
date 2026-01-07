package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
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
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
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
	private final BookingService bookingService;
	private final ua.lviv.bas.cinema.repository.BookingRepository bookingRepository;

	@Value("${app.ticket.qr.size:200}")
	private int qrCodeSize;

	@Value("${app.ticket.base-url}")
	private String ticketBaseUrl;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = new ArrayList<>();

		for (BookedSeat bookedSeat : booking.getBookedSeats()) {
			BigDecimal ticketPrice = calculateTicketPrice(bookedSeat);

			Ticket ticket = Ticket.builder().booking(booking).user(booking.getUser())
					.ticketType(bookedSeat.getTicketType()).payment(payment).originalPrice(ticketPrice)
					.finalPrice(ticketPrice).uniqueCode(generateTicketCode()).status(TicketStatus.ACTIVE)
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

		List<Ticket> tickets = ticketRepository.findByPaymentBookingId(bookingId);

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
		return qrCodeService.generateQRCode(ticketBaseUrl + "/api/tickets/validate/" + ticketCode, qrCodeSize);
	}

	public void sendTicketsToUser(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByPaymentBookingId(booking.getId());

		if (tickets.isEmpty()) {
			log.warn("No tickets found for booking {}", booking.getId());
			return;
		}

		User user = booking.getUser();
		String bookingNumber = bookingService.generateBookingNumber(booking);
		String movieTitle = booking.getSession().getMovie().getTitle();
		String sessionTime = booking.getSession().getStartTime().format(DATE_FORMATTER);
		String hallName = booking.getSession().getHall().getName();

		String seatInfo = booking.getBookedSeats().stream().map(bookedSeat -> {
			Seat seat = bookedSeat.getSeat();
			return String.format("Row %d Seat %d (%s)", seat.getRow(), seat.getNumber(),
					bookedSeat.getTicketType().getDisplayName());
		}).collect(Collectors.joining(", "));

		BigDecimal totalAmount = booking.getFinalPrice();

		try {
			emailService.sendTicketsEmail(user.getEmail(), bookingNumber, movieTitle, sessionTime, hallName,
					totalAmount, "Bank Card", seatInfo);

			log.info("Tickets email sent to {} for booking {}", user.getEmail(), booking.getId());
		} catch (Exception e) {
			log.error("Failed to send tickets email for booking {}: {}", booking.getId(), e.getMessage(), e);
		}
	}

	public void cancelTicketsForBooking(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByPaymentBookingId(booking.getId());

		if (!tickets.isEmpty()) {
			tickets.forEach(ticket -> {
				if (ticket.getStatus() == TicketStatus.ACTIVE) {
					ticket.setStatus(TicketStatus.CANCELLED);
				}
			});

			ticketRepository.saveAll(tickets);
			log.info("Cancelled {} tickets for booking {}", tickets.size(), booking.getId());
		}
	}

	public void voidTicket(Long ticketId, User user) {
		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketValidationException::notFound);

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		if (ticket.getStatus() == TicketStatus.USED) {
			throw new IllegalStateException("Cannot void an already used ticket");
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

	private BigDecimal calculateTicketPrice(BookedSeat bookedSeat) {
		Session session = bookedSeat.getSession();
		Seat seat = bookedSeat.getSeat();
		TicketType ticketType = bookedSeat.getTicketType();

		BigDecimal basePrice = session.getBasePrice();
		BigDecimal seatMultiplier = seat.getSeatType().getPriceMultiplier();
		BigDecimal ticketMultiplier = ticketType.getPriceMultiplier();

		return basePrice.multiply(seatMultiplier).multiply(ticketMultiplier);
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

		if (session.getStatus() == ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.CANCELLED) {
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
}