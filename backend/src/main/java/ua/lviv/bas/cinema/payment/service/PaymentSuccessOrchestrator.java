package ua.lviv.bas.cinema.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.service.BookingService;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.common.SeatInfoFormatter;
import ua.lviv.bas.cinema.notification.EmailAttachment;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.notification.TicketsEmail;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.service.TicketService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSuccessOrchestrator {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final BonusQueryService bonusQueryService;
    private final BonusLedgerService bonusLedgerService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;
    private final SeatInfoFormatter seatInfoFormatter;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long paymentId) {
        var payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));
        var booking = payment.getBooking();

        bookingService.confirmBooking(booking.getId());
        var tickets = ticketService.createTicketsForBooking(booking, payment);

        var pointsToAccrue = bonusQueryService.calculateAccrualPoints(booking.getFinalPrice());
        if (pointsToAccrue != null && pointsToAccrue > 0) {
            bonusLedgerService.accruePointsForPayment(booking.getUser().getId(), pointsToAccrue, booking, payment);
        }

        sendSuccessEmail(payment, booking, tickets);

        log.info("Payment {} completed successfully: booking {} confirmed, tickets issued, {} bonus points accrued",
                paymentId, booking.getId(), pointsToAccrue == null ? 0 : pointsToAccrue);
    }

    private void sendSuccessEmail(Payment payment, Booking booking, List<Ticket> tickets) {
        emailService.sendSafely("send payment success email", booking.getId(), () -> {
            var session = booking.getSession();
            var ticketCodes = tickets.stream().map(Ticket::getUniqueCode).toList();
            var qrCodes = tickets.stream()
                    .map(ticket -> new EmailAttachment(ticket.getUniqueCode() + ".png", ticketService.generateQR(ticket)))
                    .toList();
            var bonusPointsUsed = booking.getBonusPointsUsed() == null ? 0 : booking.getBonusPointsUsed();

            emailService.sendTicketsEmail(new TicketsEmail(booking.getUser().getEmail(),
                    numberGenerator.generateBookingNumber(booking), session.getMovie().getTitle(),
                    dateTimeFormatter.formatStandard(session.getStartTime()), session.getHall().getName(),
                    extractSeatsInfo(booking), booking.getTotalPrice(), bonusPointsUsed,
                    booking.getBonusDiscountAmount(), payment.getAmount(), ticketCodes, qrCodes));

            log.debug("Sent payment success email to {}", booking.getUser().getEmail());
        });
    }

    private String extractSeatsInfo(Booking booking) {
        return booking.getSeatReservations().stream()
                .map(seat -> seatInfoFormatter.format(seat.getSeat().getRow(), seat.getSeat().getNumber()))
                .collect(Collectors.joining(", "));
    }
}
