package ua.lviv.bas.cinema.service.ticket;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "tickets")
public class TicketService {

	private final TicketRepository ticketRepository;
	private final TicketSpecification ticketSpecification;
	private final TicketMapper ticketMapper;
	private final QRCodeService qrCodeService;
	private final NumberGeneratorService numberGenerator;
	private final AuditService auditService;

	@Value("${app.ticket.qr.size:200}")
	private int qrCodeSize;

	@Value("${app.base-url}")
	private String ticketBaseUrl;

	@Transactional
	public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
		List<Ticket> tickets = booking.getSeatReservations().stream()
				.map(seatReservation -> buildTicket(booking, payment, seatReservation)).collect(Collectors.toList());

		List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
		log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());

		for (Ticket ticket : savedTickets) {
			Map<String, Object> details = new HashMap<>();
			details.put("ticketCode", ticket.getUniqueCode());
			details.put("seatNumber", ticket.getSeatReservation().getSeat().getNumber());
			details.put("price", ticket.getFinalPrice());
			details.put("bookingId", booking.getId());

			auditService.logChange("Ticket", ticket.getId(), "Ticket #" + ticket.getUniqueCode(), AuditAction.CREATED,
					null, details);
		}

		return savedTickets;
	}

	private Ticket buildTicket(Booking booking, Payment payment, SeatReservation seatReservation) {
		return Ticket.builder().booking(booking).user(booking.getUser()).ticketType(seatReservation.getTicketType())
				.payment(payment).seatReservation(seatReservation).originalPrice(seatReservation.getSeatPrice())
				.finalPrice(seatReservation.getSeatPrice()).uniqueCode(numberGenerator.generateTicketCode())
				.status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now()).build();
	}

	@Cacheable(key = "#ticketId + '-' + #user.id")
	public TicketResponse getTicketById(Long ticketId, User user) {
		return ticketRepository.findByIdAndUserIdAndStatus(ticketId, user.getId(), TicketStatus.ACTIVE)
				.map(this::toTicketResponse).orElseThrow(TicketValidationException::notFound);
	}

	@Cacheable(key = "#ticketCode + '-' + #user.id")
	public TicketResponse getTicketByCode(String ticketCode, User user) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode)
				.orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		return toTicketResponse(ticket);
	}

	@Cacheable(key = "'user:' + #user.id + '-status:' + #filter.status() + '-movie:' + #filter.movieTitle() + '-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<TicketResponse> getUserTickets(User user, TicketFilterRequest filter, Pageable pageable) {
		Specification<Ticket> spec = ticketSpecification.buildForUser(user.getId(), filter.status(),
				filter.movieTitle());

		Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

		return tickets.map(this::toTicketResponse);
	}

	@Caching(evict = { @CacheEvict(value = "tickets", key = "#ticketCode + '-' + #ticket.user.id"),
			@CacheEvict(value = "tickets", key = "#ticket.id + '-' + #ticket.user.id"),
			@CacheEvict(value = "tickets", allEntries = true) })
	@Transactional
	public void validateTicket(String ticketCode) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode).orElseThrow(TicketValidationException::notFound);

		TicketStatus oldStatus = ticket.getStatus();
		String targetInfo = "Ticket #" + ticketCode;

		validateTicketForEntry(ticket);

		ticket.setStatus(TicketStatus.USED);
		ticketRepository.save(ticket);
		log.info("Ticket {} validated and marked as used", ticketCode);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", TicketStatus.USED);
		newDetails.put("validatedAt", LocalDateTime.now());

		auditService.logChange("Ticket", ticket.getId(), targetInfo, AuditAction.VALIDATED, oldDetails, newDetails);
	}

	@Transactional(readOnly = true)
	public boolean isTicketValid(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(this::isTicketValidForEntry).orElse(false);
	}

	@Transactional(readOnly = true)
	public TicketStatus checkTicketStatus(String ticketCode) {
		return ticketRepository.findByUniqueCode(ticketCode).map(Ticket::getStatus).orElse(null);
	}

	public byte[] generateTicketQRCode(String ticketCode) {
		String qrContent = ticketBaseUrl + "/api/tickets/validate/" + ticketCode;
		return qrCodeService.generateQRCode(qrContent, qrCodeSize);
	}

	private TicketResponse toTicketResponse(Ticket ticket) {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);
		String qrCodeUrl = "/api/tickets/" + ticket.getUniqueCode() + "/qr";
		return new TicketResponse(response.id(), response.ticketCode(), qrCodeUrl, response.status(),
				response.purchaseTime(), response.price(), response.ticketType(), response.movieTitle(),
				response.sessionTime(), response.hallName(), response.row(), response.seatNumber());
	}

	private void validateTicketForEntry(Ticket ticket) {
		validateTicketStatus(ticket);
		validateSession(ticket);
	}

	private void validateTicketStatus(Ticket ticket) {
		if (ticket.getStatus() == TicketStatus.USED) {
			throw TicketValidationException.alreadyUsed();
		}

		if (ticket.getStatus() == TicketStatus.REFUNDED) {
			throw new TicketValidationException("Ticket has been refunded");
		}

		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			throw new TicketValidationException("Ticket is not active");
		}
	}

	private void validateSession(Ticket ticket) {
		var session = ticket.getBooking().getSession();
		LocalDateTime now = LocalDateTime.now();

		if (session.getStartTime().isAfter(now)) {
			throw new TicketValidationException("Session has not started yet");
		}

		if (session.getStartTime().isBefore(now.minusHours(2))) {
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
}