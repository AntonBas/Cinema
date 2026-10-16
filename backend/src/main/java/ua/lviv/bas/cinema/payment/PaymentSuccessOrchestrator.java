package ua.lviv.bas.cinema.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.booking.BookingService;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.bonus.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.BonusQueryService;
import ua.lviv.bas.cinema.ticket.email.TicketsIssuedEvent;
import ua.lviv.bas.cinema.ticket.TicketService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSuccessOrchestrator {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final BonusQueryService bonusQueryService;
    private final BonusLedgerService bonusLedgerService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long paymentId) {
        var payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));
        var booking = payment.getBooking();

        bookingService.confirmBooking(booking.getId());
        ticketService.createTicketsForBooking(booking, payment);

        var pointsToAccrue = bonusQueryService.calculateAccrualPoints(booking.getFinalPrice());
        if (pointsToAccrue != null && pointsToAccrue > 0) {
            bonusLedgerService.accruePointsForPayment(booking.getUser().getId(), pointsToAccrue, booking, payment);
        }

        eventPublisher.publishEvent(new TicketsIssuedEvent(booking.getId()));

        log.info("Payment {} completed successfully: booking {} confirmed, tickets issued, {} bonus points accrued",
                paymentId, booking.getId(), pointsToAccrue == null ? 0 : pointsToAccrue);
    }
}
