package ua.lviv.bas.cinema.service.ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;
import ua.lviv.bas.cinema.service.common.NumberGeneratorService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    @CacheEvict(value = "tickets", allEntries = true)
    @Transactional
    public void createTicketsForBooking(Booking booking, Payment payment) {
        var tickets = booking.getSeatReservations().stream()
                .map(seatReservation -> buildTicket(booking, payment, seatReservation)).toList();

        var savedTickets = ticketRepository.saveAll(tickets);
        log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());

        for (var ticket : savedTickets) {
            auditCreate(ticket, booking.getId());
        }
    }

    private Ticket buildTicket(Booking booking, Payment payment, SeatReservation seatReservation) {
        return Ticket.builder().booking(booking).user(booking.getUser()).ticketType(seatReservation.getTicketType())
                .payment(payment).seatReservation(seatReservation).originalPrice(seatReservation.getSeatPrice())
                .finalPrice(seatReservation.getSeatPrice()).uniqueCode(numberGenerator.generateTicketCode())
                .status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now()).build();
    }

    public TicketCashierResponse getTicketForCashier(String uniqueCode) {
        var ticket = ticketRepository.findByUniqueCode(uniqueCode).orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + uniqueCode));
        return ticketMapper.toTicketCashierResponse(ticket);
    }

    @Cacheable(value = "tickets", key = "#ticketCode + '-' + #user.id")
    public TicketResponse getTicket(String ticketCode, User user) {
        var ticket = ticketRepository.findByUniqueCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw TicketValidationException.notFound();
        }

        return toTicketResponse(ticket);
    }

    @Cacheable(value = "tickets", key = "'user:' + #user.id + '-' + #status + '-' + #movieTitle + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<TicketResponse> getTickets(User user, TicketStatus status, String movieTitle, Pageable pageable) {
        Specification<Ticket> spec = ticketSpecification.buildForUser(user.getId(), status, movieTitle);
        var page = ticketRepository.findAll(spec, pageable);
        return page.map(this::toTicketResponse);
    }

    @CacheEvict(value = "tickets", allEntries = true)
    @Transactional
    public TicketCashierResponse validate(String ticketCode) {
        var ticket = ticketRepository.findByUniqueCode(ticketCode).orElseThrow(TicketValidationException::notFound);

        var oldStatus = ticket.getStatus();
        validateForEntry(ticket);

        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);
        log.info("Ticket {} validated and marked as used", ticketCode);
        auditValidate(ticket, oldStatus);

        return ticketMapper.toTicketCashierResponse(ticket);
    }

    public byte[] generateQR(String ticketCode) {
        var qrContent = ticketBaseUrl + "/cashier/scan/" + ticketCode;
        return qrCodeService.generateQRCode(qrContent, qrCodeSize);
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        var response = ticketMapper.toTicketResponse(ticket);
        var qrCodeUrl = "/api/tickets/" + ticket.getUniqueCode() + "/qr";
        return new TicketResponse(response.id(), response.ticketCode(), qrCodeUrl, response.status(),
                response.purchaseTime(), response.price(), response.ticketType(), response.movieTitle(),
                response.sessionTime(), response.hallName(), response.row(), response.seatNumber());
    }

    private void validateForEntry(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.USED) {
            throw TicketValidationException.alreadyUsed();
        }
        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            throw new TicketValidationException("Ticket has been refunded");
        }
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new TicketValidationException("Ticket is not active");
        }

        var session = ticket.getBooking().getSession();
        var now = LocalDateTime.now();

        if (session.getStartTime().isAfter(now.plusHours(1))) {
            throw new TicketValidationException("Too early. Entry allowed 1 hour before session start");
        }
        if (session.getStartTime().isBefore(now.minusHours(2))) {
            throw new TicketValidationException("Session ended more than 2 hours ago");
        }
        if (session.getStatus() == CinemaSessionStatus.CANCELLED) {
            throw new TicketValidationException("Session has been cancelled");
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "tickets", allEntries = true),
            @CacheEvict(value = "seatAvailability", key = "#ticket.seatReservation.session.id"),
            @CacheEvict(value = "availableSeatsCount", key = "#ticket.seatReservation.session.id")
    })
    @Transactional
    public void markAsRefunded(Ticket ticket, Refund refund) {
        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setRefund(refund);
        ticketRepository.save(ticket);

        var seatReservation = ticket.getSeatReservation();
        if (seatReservation != null) {
            seatReservation.setStatus(ReservationStatus.CANCELLED);
        }
    }

    private void auditCreate(Ticket ticket, Long bookingId) {
        Map<String, Object> details = new HashMap<>();
        details.put("ticketCode", ticket.getUniqueCode());
        details.put("seatNumber", ticket.getSeatReservation().getSeat().getNumber());
        details.put("price", ticket.getFinalPrice());
        details.put("bookingId", bookingId);
        auditService.logChange("Ticket", ticket.getId(), "Ticket #" + ticket.getUniqueCode(), AuditAction.CREATED, null,
                details);
    }

    private void auditValidate(Ticket ticket, TicketStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", TicketStatus.USED);
        newDetails.put("validatedAt", LocalDateTime.now());
        auditService.logChange("Ticket", ticket.getId(), "Ticket #" + ticket.getUniqueCode(), AuditAction.VALIDATED,
                oldDetails, newDetails);
    }
}