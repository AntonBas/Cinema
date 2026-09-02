package ua.lviv.bas.cinema.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.bonus.BonusLedgerService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefundTransactionExecutorTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private PaymentRefundService paymentRefundService;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private TicketService ticketService;
    @Mock
    private RefundCalculator refundCalculator;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private RefundTransactionExecutor refundTransactionExecutor;

    private User testUser;
    private Ticket testTicket;
    private Payment testPayment;
    private Refund testRefund;
    private RefundItem testRefundItem;

    private static final Long USER_ID = 1L;
    private static final Long TICKET_ID = 2L;
    private static final Long REFUND_ID = 3L;
    private static final BigDecimal REFUND_AMOUNT = new BigDecimal("70.00");
    private static final BigDecimal PERCENTAGE = new BigDecimal("70.00");
    private static final Integer BONUS_POINTS_TO_REFUND = 35;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(USER_ID).email("test@example.com").build();
        Movie movie = Movie.builder().title("Test Movie").build();
        CinemaHall hall = CinemaHall.builder().name("Hall 1").build();
        Session session = Session.builder().startTime(LocalDateTime.now().plusHours(3)).movie(movie).hall(hall).build();
        Seat seat = Seat.builder().row(5).number(10).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();
        Booking booking = Booking.builder().session(session).seatReservations(List.of(seatReservation))
                .totalPrice(new BigDecimal("100.00")).bonusPointsUsed(50).build();
        testPayment = Payment.builder().id(1L).amount(new BigDecimal("100.00")).liqpayPaymentId("PAY123")
                .liqpayOrderId("ORD_123").status(PaymentStatus.SUCCESS).build();
        TicketType ticketType = TicketType.builder().displayName("Standard").build();
        testTicket = Ticket.builder().id(TICKET_ID).user(testUser).booking(booking).ticketType(ticketType)
                .finalPrice(new BigDecimal("100.00")).uniqueCode("TKT-123456").status(TicketStatus.ACTIVE)
                .payment(testPayment).seatReservation(seatReservation)
                .purchaseTime(LocalDateTime.now().minusHours(1)).build();

        testRefund = Refund.builder().id(REFUND_ID).user(testUser).payment(testPayment).totalAmount(REFUND_AMOUNT)
                .totalBonusPointsToDeduct(BONUS_POINTS_TO_REFUND).status(RefundStatus.PROCESSING).build();
        testRefundItem = RefundItem.builder().refund(testRefund).ticket(testTicket)
                .ticketPrice(new BigDecimal("100.00")).refundPercentage(PERCENTAGE).refundAmount(REFUND_AMOUNT)
                .bonusPointsToDeduct(BONUS_POINTS_TO_REFUND).status(RefundItemStatus.PENDING).build();
        testRefund.getItems().add(testRefundItem);

        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void markProcessingShouldCreateProcessingRefund() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundCalculator.validate(testTicket)).thenReturn(null);
        when(refundRepository.existsByItemsTicketIdAndStatus(TICKET_ID, RefundStatus.PROCESSING)).thenReturn(false);
        when(refundCalculator.calculate(testTicket))
                .thenReturn(new RefundCalculator.RefundCalculation(PERCENTAGE, REFUND_AMOUNT, REFUND_AMOUNT,
                        BONUS_POINTS_TO_REFUND, BONUS_POINTS_TO_REFUND));
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> {
            Refund r = i.getArgument(0);
            r.setId(REFUND_ID);
            return r;
        });

        var context = refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason");

        assertThat(context.refundId()).isEqualTo(REFUND_ID);
        assertThat(context.ticketId()).isEqualTo(TICKET_ID);
        assertThat(context.liqpayPaymentId()).isEqualTo("PAY123");
        assertThat(context.liqpayOrderId()).isEqualTo("ORD_123");
        assertThat(context.refundAmount()).isEqualTo(REFUND_AMOUNT);

        verify(paymentRefundService).validateRefundEligibility(testPayment, REFUND_AMOUNT);
        var refundCaptor = org.mockito.ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(refundCaptor.capture());
        assertThat(refundCaptor.getValue().getStatus()).isEqualTo(RefundStatus.PROCESSING);
    }

    @Test
    void markProcessingWhenTicketNotRefundableShouldThrow() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundCalculator.validate(testTicket)).thenReturn("Ticket is not active. Current status: REFUNDED");

        assertThatThrownBy(() -> refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .isInstanceOf(TicketNotRefundableException.class);

        verify(refundRepository, never()).save(any());
    }

    @Test
    void markProcessingWhenAlreadyProcessingShouldThrowGuard() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundCalculator.validate(testTicket)).thenReturn(null);
        when(refundRepository.existsByItemsTicketIdAndStatus(TICKET_ID, RefundStatus.PROCESSING)).thenReturn(true);

        assertThatThrownBy(() -> refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .isInstanceOf(TicketNotRefundableException.class)
                .hasMessageContaining("already being processed");

        verify(paymentRefundService, never()).validateRefundEligibility(any(), any());
        verify(refundRepository, never()).save(any());
    }

    @Test
    void applySuccessShouldApplyAllDomainsAndMarkProcessed() {
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> i.getArgument(0));

        var result = refundTransactionExecutor.applySuccess(REFUND_ID, TICKET_ID);

        assertThat(result.getStatus()).isEqualTo(RefundStatus.PROCESSED);
        verify(paymentRefundService).applyRefundSuccess(eq(testPayment), eq(REFUND_AMOUNT), any(String.class), eq(testTicket));
        verify(bonusLedgerService).refundPointsForTicket(USER_ID, BONUS_POINTS_TO_REFUND, "REFUND_TICKET_" + TICKET_ID);
        verify(ticketService).markAsRefunded(testTicket, testRefund);
    }

    @Test
    void applySuccessWhenAlreadyProcessedShouldSkip() {
        testRefund.setStatus(RefundStatus.PROCESSED);
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));

        var result = refundTransactionExecutor.applySuccess(REFUND_ID, TICKET_ID);

        assertThat(result.getStatus()).isEqualTo(RefundStatus.PROCESSED);
        verifyNoInteractions(paymentRefundService, bonusLedgerService, ticketService);
        verify(refundRepository, never()).save(any());
    }

    @Test
    void applySuccessWhenNoBonusPointsShouldSkipBonusRefund() {
        testRefund.setTotalBonusPointsToDeduct(0);
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));
        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> i.getArgument(0));

        refundTransactionExecutor.applySuccess(REFUND_ID, TICKET_ID);

        verify(bonusLedgerService, never()).refundPointsForTicket(any(), any(), any());
    }

    @Test
    void markFailedShouldSetRejectedStatus() {
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> i.getArgument(0));

        refundTransactionExecutor.markFailed(REFUND_ID, new RuntimeException("LiqPay rejected"));

        assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);
        verify(refundRepository).save(testRefund);
    }
}
