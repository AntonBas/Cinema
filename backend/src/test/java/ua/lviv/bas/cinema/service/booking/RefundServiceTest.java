package ua.lviv.bas.cinema.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentGatewayUnavailableException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.RefundProcessingException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.booking.RefundMapper;
import ua.lviv.bas.cinema.service.common.NumberGeneratorService;
import ua.lviv.bas.cinema.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

    @Mock
    private TicketService ticketService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RefundTransactionExecutor refundTransactionExecutor;
    @Mock
    private RefundRules refundRules;
    @Mock
    private RefundMapper refundMapper;
    @Mock
    private RefundItemMapper refundItemMapper;
    @Mock
    private NumberGeneratorService numberGenerator;

    private RefundCalculator refundCalculator;
    private RefundService refundService;

    private User testUser;
    private Ticket testTicket;
    private Payment testPayment;
    private Session testSession;
    private RefundPreviewRequest previewRequest;
    private Refund testRefund;

    private static final Long USER_ID = 1L;
    private static final Long TICKET_ID = 2L;
    private static final Long REFUND_ID = 1L;
    private static final BigDecimal TICKET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal REFUND_AMOUNT = new BigDecimal("70.00");
    private static final BigDecimal PERCENTAGE = new BigDecimal("70.00");
    private static final Integer BONUS_POINTS_USED = 50;
    private static final Integer BONUS_POINTS_TO_REFUND = 35;

    @BeforeEach
    void setUp() {
        refundCalculator = new RefundCalculator(refundRules);
        refundService = new RefundService(ticketService, paymentService, refundCalculator,
                refundTransactionExecutor, refundRules, refundMapper, refundItemMapper, numberGenerator);

        testUser = User.builder().id(USER_ID).email("test@example.com").build();
        Movie movie = Movie.builder().title("Test Movie").build();
        CinemaHall hall = CinemaHall.builder().name("Hall 1").build();
        testSession = Session.builder().startTime(LocalDateTime.now().plusHours(3)).movie(movie).hall(hall).build();
        Seat seat = Seat.builder().row(5).number(10).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();
        Booking booking = Booking.builder()
                .session(testSession)
                .seatReservations(List.of(seatReservation))
                .totalPrice(TICKET_PRICE)
                .bonusPointsUsed(BONUS_POINTS_USED)
                .build();
        testPayment = Payment.builder().id(1L).amount(TICKET_PRICE).liqpayPaymentId("PAY123")
                .liqpayOrderId("ORD_123").status(PaymentStatus.SUCCESS).build();
        TicketType ticketType = TicketType.builder().displayName("Standard").build();
        testTicket = Ticket.builder().id(TICKET_ID).user(testUser).booking(booking).ticketType(ticketType)
                .finalPrice(TICKET_PRICE).originalPrice(TICKET_PRICE).uniqueCode("TKT-123456")
                .status(TicketStatus.ACTIVE).payment(testPayment).bonusPointsUsed(BONUS_POINTS_USED)
                .purchaseTime(LocalDateTime.now().minusHours(1)).seatReservation(seatReservation).build();
        testRefund = Refund.builder().id(REFUND_ID).user(testUser).payment(testPayment).totalAmount(REFUND_AMOUNT)
                .totalBonusPointsToDeduct(BONUS_POINTS_TO_REFUND).build();
        previewRequest = new RefundPreviewRequest(TICKET_ID);
    }

    @Test
    void getPreviewShouldSucceed() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
        when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(PERCENTAGE);
        when(refundRules.getPolicyName(testSession.getStartTime())).thenReturn("Standard Refund");
        when(refundRules.getPolicyDescription(testSession.getStartTime())).thenReturn("70% refund before 3 hours");

        RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.ticketId()).isEqualTo(TICKET_ID);
        assertThat(response.isRefundable()).isTrue();
        assertThat(response.refundAmount()).isEqualTo(REFUND_AMOUNT);
        assertThat(response.bonusPointsToRefund()).isEqualTo(BONUS_POINTS_TO_REFUND);
    }

    @Test
    void getPreviewWhenPaymentNotSuccessShouldReturnNonRefundable() {
        testPayment.setStatus(PaymentStatus.PENDING);
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);

        RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.isRefundable()).isFalse();
        assertThat(response.nonRefundableReason()).contains("Payment cannot be refunded via API");
    }

    @Test
    void getPreviewWhenTicketNotActiveShouldReturnNonRefundable() {
        testTicket.setStatus(TicketStatus.REFUNDED);
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);

        RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.isRefundable()).isFalse();
        assertThat(response.nonRefundableReason()).contains("Ticket is not active");
    }

    @Test
    void getPreviewWhenRefundNotAvailableShouldReturnNonRefundable() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID)).thenReturn(testTicket);
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

        RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.isRefundable()).isFalse();
        assertThat(response.nonRefundableReason()).contains("Refund is not available for this session");
    }

    @Test
    void getPreviewWhenTicketNotFoundShouldThrowException() {
        when(ticketService.findActiveTicketForUser(TICKET_ID, USER_ID))
                .thenThrow(new TicketNotFoundException("Ticket not found or not active. Ticket ID: " + TICKET_ID));

        assertThatThrownBy(() -> refundService.getPreview(previewRequest, USER_ID))
                .isInstanceOf(TicketNotFoundException.class);
    }

    private RefundTransactionExecutor.RefundProcessingContext defaultContext() {
        return new RefundTransactionExecutor.RefundProcessingContext(REFUND_ID, TICKET_ID, "TKT-123456", "PAY123",
                "ORD_123", REFUND_AMOUNT);
    }

    @Test
    void refundShouldSucceed() {
        RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

        when(refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .thenReturn(defaultContext());
        when(refundTransactionExecutor.applySuccess(REFUND_ID, TICKET_ID)).thenReturn(testRefund);
        when(numberGenerator.generateRefundNumber(testRefund)).thenReturn("RF-2024-00001");

        RefundResponse mockResponse = new RefundResponse(1L, "RF-2024-00001", "PROCESSED", REFUND_AMOUNT,
                BONUS_POINTS_TO_REFUND, "Test reason", "System", LocalDateTime.now(), LocalDateTime.now(), 1L, "CARD",
                null, "Refund processed successfully", "3-5 business days");
        when(refundMapper.toResponse(testRefund)).thenReturn(mockResponse);

        RefundResponse response = refundService.refund(refundRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(paymentService).callLiqPayRefund(eq("PAY123"), eq("ORD_123"), eq(REFUND_AMOUNT), any(String.class));
        verify(refundTransactionExecutor).applySuccess(REFUND_ID, TICKET_ID);
        verify(refundTransactionExecutor, never()).markFailed(any(), any());
    }

    @Test
    void refundWhenTicketNotRefundableShouldThrowException() {
        RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

        when(refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .thenThrow(new TicketNotRefundableException("Ticket is not active. Current status: REFUNDED"));

        assertThatThrownBy(() -> refundService.refund(refundRequest, USER_ID))
                .isInstanceOf(TicketNotRefundableException.class);

        verify(paymentService, never()).callLiqPayRefund(any(), any(), any(), any());
    }

    @Test
    void refundWhenLiqPayExplicitlyRejectsShouldMarkFailedAndNotApplySuccess() {
        RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

        when(refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .thenReturn(defaultContext());
        doThrow(new PaymentProcessingException("LiqPay refund failed: error - 1 - insufficient funds"))
                .when(paymentService).callLiqPayRefund(eq("PAY123"), eq("ORD_123"), eq(REFUND_AMOUNT), any());

        assertThatThrownBy(() -> refundService.refund(refundRequest, USER_ID))
                .isInstanceOf(RefundProcessingException.class);

        verify(refundTransactionExecutor).markFailed(eq(REFUND_ID), any(PaymentProcessingException.class));
        verify(refundTransactionExecutor, never()).applySuccess(any(), any());
    }

    @Test
    void refundWhenLiqPayOutcomeIsAmbiguousShouldNotMarkFailedAndLeaveProcessing() {
        RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

        when(refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .thenReturn(defaultContext());
        doThrow(new PaymentGatewayUnavailableException("Network error during refund: timeout", null))
                .when(paymentService).callLiqPayRefund(eq("PAY123"), eq("ORD_123"), eq(REFUND_AMOUNT), any());

        assertThatThrownBy(() -> refundService.refund(refundRequest, USER_ID))
                .isInstanceOf(RefundProcessingException.class);

        verify(refundTransactionExecutor, never()).markFailed(any(), any());
        verify(refundTransactionExecutor, never()).applySuccess(any(), any());
    }

    @Test
    void refundWhenApplySuccessFailsAfterSuccessfulLiqPayShouldRetryOnceThenLeaveProcessing() {
        RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

        when(refundTransactionExecutor.markProcessing(TICKET_ID, USER_ID, "Test reason"))
                .thenReturn(defaultContext());
        when(refundTransactionExecutor.applySuccess(REFUND_ID, TICKET_ID))
                .thenThrow(new RuntimeException("DB connection lost"));

        assertThatThrownBy(() -> refundService.refund(refundRequest, USER_ID))
                .isInstanceOf(RefundProcessingException.class);

        verify(paymentService, times(1)).callLiqPayRefund(eq("PAY123"), eq("ORD_123"), eq(REFUND_AMOUNT), any());
        verify(refundTransactionExecutor, times(2)).applySuccess(REFUND_ID, TICKET_ID);
        verify(refundTransactionExecutor, never()).markFailed(any(), any());
    }
}
