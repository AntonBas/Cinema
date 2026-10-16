package ua.lviv.bas.cinema.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.common.SeatInfoFormatter;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.ticket.Ticket;

@Component
@RequiredArgsConstructor
class RefundConfirmationEmailSender {

    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final NumberGeneratorService numberGenerator;
    private final SeatInfoFormatter seatInfoFormatter;

    void send(Refund refund, Ticket ticket, int bonusPointsReturned, int earnedPointsRevoked) {
        emailService.sendSafely("send refund email", refund.getId(), () -> {
            var booking = refund.getPayment().getBooking();
            var session = booking.getSession();
            var seat = ticket.getSeatReservation().getSeat();

            emailService.sendRefundEmail(refund.getUser().getEmail(), numberGenerator.generateBookingNumber(booking),
                    session.getMovie().getTitle(), dateTimeFormatter.formatStandard(session.getStartTime()),
                    session.getHall().getName(), refund.getTotalAmount(),
                    seatInfoFormatter.format(seat.getRow(), seat.getNumber()), refund.getReason(),
                    bonusPointsReturned, earnedPointsRevoked);
        });
    }
}
