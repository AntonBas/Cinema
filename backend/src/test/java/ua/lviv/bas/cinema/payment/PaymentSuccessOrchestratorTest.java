package ua.lviv.bas.cinema.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.lviv.bas.cinema.booking.Booking;
import ua.lviv.bas.cinema.booking.BookingService;
import ua.lviv.bas.cinema.booking.BookingStatus;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.bonus.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.BonusQueryService;
import ua.lviv.bas.cinema.ticket.email.TicketsIssuedEvent;
import ua.lviv.bas.cinema.ticket.TicketService;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentSuccessOrchestratorTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private TicketService ticketService;
    @Mock
    private BonusQueryService bonusQueryService;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentSuccessOrchestrator paymentSuccessOrchestrator;

    private Booking testBooking;
    private Payment testPayment;

    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 2L;
    private static final Long PAYMENT_ID = 3L;
    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        var testUser = User.builder().id(USER_ID).email("test@example.com").build();
        var session = CinemaTestFixtures.session(CinemaTestFixtures.movie(), CinemaTestFixtures.hall());

        testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(session).status(BookingStatus.PENDING)
                .totalPrice(AMOUNT).finalPrice(AMOUNT).expiresAt(Instant.now().plus(Duration.ofHours(1))).build();

        testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.SUCCESS)
                .liqpayOrderId("ORD_TEST123456789").build();

        lenient().when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
    }

    @Test
    void handleShouldConfirmBookingCreateTicketsAccruePointsAndPublishTicketsIssuedEvent() {
        when(bonusQueryService.calculateAccrualPoints(AMOUNT)).thenReturn(20);

        paymentSuccessOrchestrator.handle(PAYMENT_ID);

        verify(bookingService).confirmBooking(BOOKING_ID);
        verify(ticketService).createTicketsForBooking(testBooking, testPayment);
        verify(bonusLedgerService).accruePointsForPayment(USER_ID, 20, testBooking, testPayment);
        verify(eventPublisher).publishEvent(new TicketsIssuedEvent(BOOKING_ID));
    }

    @Test
    void handleWhenNoPointsToAccrueShouldSkipAccrual() {
        when(bonusQueryService.calculateAccrualPoints(AMOUNT)).thenReturn(0);

        paymentSuccessOrchestrator.handle(PAYMENT_ID);

        verify(bonusLedgerService, never()).accruePointsForPayment(any(), any(), any(), any());
    }
}
