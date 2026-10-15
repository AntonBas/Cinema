package ua.lviv.bas.cinema.ticket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.common.SeatInfoFormatter;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.notification.EmailAttachment;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.notification.TicketsEmail;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketsEmailService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;
    private final SeatInfoFormatter seatInfoFormatter;
    private final TransactionTemplate readOnlyTransactionTemplate;

    public TicketsEmailService(BookingRepository bookingRepository, TicketRepository ticketRepository,
                               TicketService ticketService, EmailService emailService,
                               DateTimeFormatterService dateTimeFormatter, NumberGeneratorService numberGenerator,
                               SeatInfoFormatter seatInfoFormatter, PlatformTransactionManager transactionManager) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.emailService = emailService;
        this.dateTimeFormatter = dateTimeFormatter;
        this.numberGenerator = numberGenerator;
        this.seatInfoFormatter = seatInfoFormatter;
        this.readOnlyTransactionTemplate = new TransactionTemplate(transactionManager);
        this.readOnlyTransactionTemplate.setReadOnly(true);
    }

    public void sendAndMarkSent(Long bookingId) {
        var email = readOnlyTransactionTemplate.execute(status -> composeIfUnsent(bookingId));
        if (email == null) {
            log.debug("Tickets email for booking {} was already sent, skipping", bookingId);
            return;
        }

        emailService.sendTicketsEmail(email);
        bookingRepository.markTicketsEmailSent(bookingId, Instant.now());
        log.info("Tickets email for booking {} sent", bookingId);
    }

    private TicketsEmail composeIfUnsent(Long bookingId) {
        var booking = bookingRepository.findWithDetailsById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        if (booking.getTicketsEmailSentAt() != null) {
            return null;
        }

        var session = booking.getSession();
        var tickets = ticketRepository.findByBookingId(bookingId);
        var ticketCodes = tickets.stream().map(Ticket::getUniqueCode).toList();
        var qrCodes = tickets.stream()
                .map(ticket -> new EmailAttachment(ticket.getUniqueCode() + ".png", ticketService.generateQR(ticket)))
                .toList();
        var bonusPointsUsed = booking.getBonusPointsUsed() == null ? 0 : booking.getBonusPointsUsed();

        return new TicketsEmail(booking.getUser().getEmail(), numberGenerator.generateBookingNumber(booking),
                session.getMovie().getTitle(), dateTimeFormatter.formatStandard(session.getStartTime()),
                session.getHall().getName(), extractSeatsInfo(booking), booking.getTotalPrice(), bonusPointsUsed,
                booking.getBonusDiscountAmount(), booking.getPayment().getAmount(), ticketCodes, qrCodes);
    }

    private String extractSeatsInfo(Booking booking) {
        return booking.getSeatReservations().stream()
                .map(seat -> seatInfoFormatter.format(seat.getSeat().getRow(), seat.getSeat().getNumber()))
                .collect(Collectors.joining(", "));
    }
}
