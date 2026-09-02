package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.service.BookingService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.ticket.service.TicketService;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentSuccessOrchestratorTest {

    @Mock
    private BookingService bookingService;
    @Mock
    private TicketService ticketService;
    @Mock
    private BonusQueryService bonusQueryService;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private EmailService emailService;
    @Mock
    private DateTimeFormatterService dateTimeFormatter;
    @Mock
    private NumberGeneratorService numberGenerator;

    @InjectMocks
    private PaymentSuccessOrchestrator paymentSuccessOrchestrator;

    private User testUser;
    private Booking testBooking;
    private Payment testPayment;

    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 2L;
    private static final Long PAYMENT_ID = 3L;
    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(USER_ID).email("test@example.com").build();

        var movie = CinemaTestFixtures.movie();
        var hall = CinemaTestFixtures.hall();
        var session = CinemaTestFixtures.session(movie, hall);

        Seat seat = Seat.builder().row(1).number(1).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat)
                .status(ReservationStatus.CONFIRMED).build();

        testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(session).status(BookingStatus.PENDING)
                .finalPrice(AMOUNT).expiresAt(LocalDateTime.now().plusHours(1))
                .seatReservations(Collections.singletonList(seatReservation)).build();

        testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.SUCCESS)
                .liqpayOrderId("ORD_TEST123456789").build();

        lenient().doAnswer(invocation -> {
            Runnable emailAction = invocation.getArgument(2);
            emailAction.run();
            return null;
        }).when(emailService).sendSafely(any(String.class), any(), any());
    }

    @Test
    void handleShouldConfirmBookingCreateTicketsAndAccruePoints() {
        when(bonusQueryService.calculateAccrualPoints(AMOUNT)).thenReturn(20);
        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentSuccessOrchestrator.handle(testPayment);

        verify(bookingService).confirmBooking(BOOKING_ID);
        verify(ticketService).createTicketsForBooking(testBooking, testPayment);
        verify(bonusLedgerService).accruePointsForPayment(USER_ID, 20, testBooking, testPayment);
        verify(emailService).sendTicketsEmail(testUser.getEmail(), "BK-2024-00001", "Test Movie", "2024-01-01 14:00",
                "Hall A", AMOUNT, "Credit card", "Row 1, Seat 1");
    }

    @Test
    void handleWhenNoPointsToAccrueShouldSkipAccrual() {
        when(bonusQueryService.calculateAccrualPoints(AMOUNT)).thenReturn(0);
        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentSuccessOrchestrator.handle(testPayment);

        verify(bonusLedgerService, never()).accruePointsForPayment(any(), any(), any(), any());
    }
}
