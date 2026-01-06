package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = new ArrayList<>();

		for (BookedSeat bookedSeat : booking.getBookedSeats()) {
			Ticket ticket = Ticket.builder().bookedSeat(bookedSeat).payment(payment).user(booking.getUser())
					.finalPrice(calculateTicketPrice(bookedSeat)).uniqueCode(generateTicketCode())
					.status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now()).build();

			tickets.add(ticket);
		}

		return ticketRepository.saveAll(tickets);
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
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
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
	}

	public byte[] generateTicketQRCode(String ticketCode) {
		return qrCodeService.generateQRCode(ticketBaseUrl + "/api/tickets/validate/" + ticketCode, qrCodeSize);
	}

	public void sendTicketsToUser(Booking booking) {
		List<Ticket> tickets = ticketRepository.findByPaymentBookingId(booking.getId());

		if (tickets.isEmpty())
			return;

		User user = booking.getUser();
		String bookingNumber = bookingService.generateBookingNumber(booking);
		String movieTitle = booking.getSession().getMovie().getTitle();
		String sessionTime = bookingService.formatDateTime(booking.getSession().getStartTime());
		String hallName = booking.getSession().getHall().getName();

		String seatInfo = tickets.stream().map(ticket -> {
			BookedSeat bookedSeat = ticket.getBookedSeat();
			return String.format("Row %d Seat %d", bookedSeat.getSeat().getRow(), bookedSeat.getSeat().getNumber());
		}).collect(Collectors.joining(", "));

		String qrCodeUrl = generateQrCodeUrl(tickets.get(0).getUniqueCode());

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (Ticket ticket : tickets) {
			totalAmount = totalAmount.add(ticket.getFinalPrice());
		}

		try {
			emailService.sendPaymentSuccessWithTickets(user.getEmail(), bookingNumber, movieTitle, sessionTime,
					hallName, totalAmount, "Card", qrCodeUrl, seatInfo);
		} catch (Exception e) {
			log.error("Failed to send tickets email: {}", e.getMessage());
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

		if (ticket.getStatus() == TicketStatus.CANCELLED)
			return;

		ticket.setStatus(TicketStatus.CANCELLED);
		ticketRepository.save(ticket);
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

		Session session = ticket.getBookedSeat().getSession();
		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw TicketValidationException.sessionStarted();
		}

		if (session.getStatus() == ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.CANCELLED) {
			throw new TicketValidationException("Session has been cancelled");
		}
	}
}