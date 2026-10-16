package ua.lviv.bas.cinema.ticket;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.AuditAction;
import ua.lviv.bas.cinema.booking.Booking;
import ua.lviv.bas.cinema.payment.Payment;
import ua.lviv.bas.cinema.refund.Refund;
import ua.lviv.bas.cinema.refund.RefundCalculator;
import ua.lviv.bas.cinema.booking.reservation.SeatReservation;
import ua.lviv.bas.cinema.booking.reservation.ReservationStatus;
import ua.lviv.bas.cinema.cinema.CinemaSessionStatus;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.ticket.dto.TicketCashierResponse;
import ua.lviv.bas.cinema.ticket.dto.TicketResponse;
import ua.lviv.bas.cinema.ticket.exception.TicketNotFoundException;
import ua.lviv.bas.cinema.ticket.exception.TicketValidationException;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.audit.AuditDetails;
import ua.lviv.bas.cinema.audit.AuditService;
import ua.lviv.bas.cinema.common.CinemaTime;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketSpecification ticketSpecification;
    private final TicketMapper ticketMapper;
    private final RefundCalculator refundCalculator;
    private final QRCodeService qrCodeService;
    private final NumberGeneratorService numberGenerator;
    private final AuditService auditService;
    private final CacheManager cacheManager;
    private final EntityManager entityManager;

    @Value("${app.ticket.qr.size:200}")
    private int qrCodeSize;

    @Value("${app.frontend.url}")
    private String ticketBaseUrl;

    @CacheEvict(value = {"ticketList", "adminSessions"}, allEntries = true)
    @Transactional
    public List<Ticket> createTicketsForBooking(Booking booking, Payment payment) {
        if (ticketRepository.existsByBookingId(booking.getId())) {
            log.debug("Tickets already exist for booking {}, skipping creation", booking.getId());
            return ticketRepository.findByBookingId(booking.getId());
        }

        var tickets = booking.getSeatReservations().stream()
                .map(seatReservation -> buildTicket(booking, payment, seatReservation)).toList();

        var savedTickets = ticketRepository.saveAll(tickets);
        log.info("Created {} tickets for booking {}", savedTickets.size(), booking.getId());

        for (var ticket : savedTickets) {
            auditCreate(ticket, booking.getId());
        }
        return savedTickets;
    }

    private Ticket buildTicket(Booking booking, Payment payment, SeatReservation seatReservation) {
        return Ticket.builder().booking(booking).user(booking.getUser()).ticketType(seatReservation.getTicketType())
                .payment(payment).seatReservation(seatReservation).originalPrice(seatReservation.getSeatPrice())
                .finalPrice(seatReservation.getSeatPrice()).uniqueCode(numberGenerator.generateTicketCode())
                .status(TicketStatus.ACTIVE).purchaseTime(Instant.now()).build();
    }

    public boolean areAllTicketsRefunded(Long bookingId) {
        return !ticketRepository.existsByBookingIdAndStatusNot(bookingId, TicketStatus.REFUNDED);
    }

    public Ticket getTicketForUser(Long ticketId, Long userId) {
        return ticketRepository.findByIdAndUserId(ticketId, userId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found. Ticket ID: " + ticketId));
    }

    public Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found. Ticket ID: " + ticketId));
    }

    public TicketCashierResponse getTicketForCashier(String uniqueCode) {
        var ticket = ticketRepository.findByUniqueCode(uniqueCode).orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + uniqueCode));
        return ticketMapper.toTicketCashierResponse(ticket);
    }

    @Cacheable(value = "ticket", key = "#ticketCode + '-' + #user.id")
    public TicketResponse getTicket(String ticketCode, User user) {
        var ticket = ticketRepository.findByUniqueCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new TicketNotFoundException("Ticket " + ticketCode + " does not belong to user " + user.getId());
        }

        return toTicketResponse(ticket);
    }

    @Cacheable(value = "ticketList", key = "'user:' + #user.id + '-' + #status + '-' + #movieTitle + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<TicketResponse> getTickets(User user, TicketStatus status, String movieTitle, Pageable pageable) {
        Specification<Ticket> spec = ticketSpecification.buildForUser(user.getId(), status, movieTitle);
        var page = ticketRepository.findAll(spec, pageable);
        return page.map(this::toTicketResponse);
    }

    @CacheEvict(value = "ticketList", allEntries = true)
    @Transactional
    public TicketCashierResponse validate(String ticketCode) {
        var ticket = ticketRepository.findByUniqueCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

        var oldStatus = ticket.getStatus();
        validateForEntry(ticket);

        int updated = ticketRepository.updateStatusIfCurrent(ticket.getId(), TicketStatus.ACTIVE, TicketStatus.USED);
        if (updated == 0) {
            throw TicketValidationException.alreadyUsed();
        }

        entityManager.refresh(ticket);
        evictTicketCache(ticket);
        log.info("Ticket {} validated and marked as used", ticketCode);
        auditValidate(ticket, oldStatus);

        return ticketMapper.toTicketCashierResponse(ticket);
    }

    public byte[] generateQR(String ticketCode, User user) {
        var ticket = ticketRepository.findByUniqueCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new TicketNotFoundException("Ticket " + ticketCode + " does not belong to user " + user.getId());
        }

        return generateQR(ticket);
    }

    public byte[] generateQR(Ticket ticket) {
        return qrCodeService.generateQRCode(ticketBaseUrl + "/cashier/scan/" + ticket.getUniqueCode(), qrCodeSize);
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        var qrCodeUrl = "/api/tickets/code/" + ticket.getUniqueCode() + "/qr";
        return ticketMapper.toTicketResponse(ticket, qrCodeUrl, refundCalculator.validate(ticket) == null);
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
        var now = CinemaTime.now();

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
            @CacheEvict(value = "ticket", key = "#ticket.uniqueCode + '-' + #ticket.user.id"),
            @CacheEvict(value = "ticketList", allEntries = true),
            @CacheEvict(value = "adminSessions", allEntries = true),
            @CacheEvict(value = "seatAvailability", key = "#ticket.seatReservation.session.id")
    })
    @Transactional
    public void markAsRefunded(Ticket ticket, Refund refund) {
        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            log.debug("Ticket {} already marked as refunded, skipping", ticket.getId());
            return;
        }

        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setRefund(refund);
        ticketRepository.save(ticket);

        var seatReservation = ticket.getSeatReservation();
        if (seatReservation != null) {
            seatReservation.setStatus(ReservationStatus.CANCELLED);
        }
    }

    private void evictTicketCache(Ticket ticket) {
        Cache cache = cacheManager.getCache("ticket");
        if (cache != null) {
            cache.evict(ticket.getUniqueCode() + "-" + ticket.getUser().getId());
        }
    }

    private void auditCreate(Ticket ticket, Long bookingId) {
        var details = AuditDetails.of().put("ticketCode", ticket.getUniqueCode())
                .put("seatNumber", ticket.getSeatReservation().getSeat().getNumber())
                .put("price", ticket.getFinalPrice()).put("bookingId", bookingId).build();
        auditService.logChange("Ticket", ticket.getId(), "Ticket #" + ticket.getUniqueCode(), AuditAction.CREATED, null,
                details);
    }

    private void auditValidate(Ticket ticket, TicketStatus oldStatus) {
        var oldDetails = AuditDetails.of().put("status", oldStatus).build();
        var newDetails = AuditDetails.of().put("status", TicketStatus.USED).put("validatedAt", Instant.now())
                .build();
        auditService.logChange("Ticket", ticket.getId(), "Ticket #" + ticket.getUniqueCode(), AuditAction.VALIDATED,
                oldDetails, newDetails);
    }
}