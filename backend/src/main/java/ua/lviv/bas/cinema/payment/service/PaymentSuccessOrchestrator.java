package ua.lviv.bas.cinema.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.service.BookingService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.ticket.service.TicketService;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSuccessOrchestrator {

    private final BookingService bookingService;
    private final TicketService ticketService;
    private final BonusQueryService bonusQueryService;
    private final BonusLedgerService bonusLedgerService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;

    public void handle(Payment payment) {
        var booking = payment.getBooking();

        bookingService.confirmBooking(booking.getId());
        ticketService.createTicketsForBooking(booking, payment);

        var pointsToAccrue = bonusQueryService.calculateAccrualPoints(booking.getFinalPrice());
        if (pointsToAccrue != null && pointsToAccrue > 0) {
            bonusLedgerService.accruePointsForPayment(booking.getUser().getId(), pointsToAccrue, booking, payment);
        }

        sendSuccessEmail(payment, booking);
    }

    private void sendSuccessEmail(Payment payment, Booking booking) {
        emailService.sendSafely("send payment success email", booking.getId(), () -> {
            var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
            var seatsInfo = extractSeatsInfo(booking);
            var bookingNumber = numberGenerator.generateBookingNumber(booking);

            emailService.sendTicketsEmail(booking.getUser().getEmail(), bookingNumber,
                    booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
                    payment.getAmount(), "Credit card", seatsInfo);

            log.debug("Sent payment success email to {}", booking.getUser().getEmail());
        });
    }

    private String extractSeatsInfo(Booking booking) {
        return booking.getSeatReservations().stream()
                .map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
                .collect(Collectors.joining(", "));
    }
}
