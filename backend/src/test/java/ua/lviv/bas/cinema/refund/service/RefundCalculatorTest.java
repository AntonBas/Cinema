package ua.lviv.bas.cinema.refund.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundCalculatorTest {

    @Mock
    private RefundRules refundRules;

    private RefundCalculator refundCalculator;

    private Session testSession;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        refundCalculator = new RefundCalculator(refundRules);

        var movie = CinemaTestFixtures.movie();
        var hall = CinemaTestFixtures.hall();
        testSession = CinemaTestFixtures.session(movie, hall);

        Seat seat = Seat.builder().row(1).number(1).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();
        Booking booking = Booking.builder().session(testSession).seatReservations(List.of(seatReservation))
                .totalPrice(new BigDecimal("100.00")).bonusPointsUsed(0).build();
        Payment payment = Payment.builder().amount(new BigDecimal("100.00")).build();
        TicketType ticketType = TicketType.builder().displayName("Standard").build();
        testTicket = Ticket.builder().booking(booking).payment(payment).ticketType(ticketType)
                .finalPrice(new BigDecimal("100.00")).status(TicketStatus.ACTIVE)
                .purchaseTime(LocalDateTime.now().minusHours(1)).seatReservation(seatReservation).build();
    }

    @Test
    void validateWhenTicketNotActiveShouldReturnReason() {
        testTicket.setStatus(TicketStatus.REFUNDED);

        String reason = refundCalculator.validate(testTicket);

        assertThat(reason).contains("Ticket is not active");
    }

    @Test
    void validateWhenSessionNotRefundableShouldReturnReason() {
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

        String reason = refundCalculator.validate(testTicket);

        assertThat(reason).isEqualTo("Refund is not available for this session");
    }

    @Test
    void validateWhenAlreadyRefundedShouldReturnReason() {
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
        testTicket.setRefund(Refund.builder().build());

        String reason = refundCalculator.validate(testTicket);

        assertThat(reason).isEqualTo("Ticket has already been refunded");
    }

    @Test
    void validateWhenSessionAlreadyStartedShouldReturnReason() {
        testSession.setStartTime(LocalDateTime.now().minusHours(1));
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);

        String reason = refundCalculator.validate(testTicket);

        assertThat(reason).isEqualTo("Session has already started or finished");
    }

    @Test
    void validateWhenEligibleShouldReturnNull() {
        testSession.setStartTime(LocalDateTime.now().plusHours(3));
        when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);

        String reason = refundCalculator.validate(testTicket);

        assertThat(reason).isNull();
    }

    @Test
    void calculateCashAmountWhenTotalBookingPriceZeroShouldFallBackToEqualSplit() {
        testTicket.getBooking().setTotalPrice(BigDecimal.ZERO);
        testTicket.getBooking().setSeatReservations(
                List.of(SeatReservation.builder().build(), SeatReservation.builder().build()));
        testTicket.getPayment().setAmount(new BigDecimal("100.00"));

        BigDecimal cashAmount = refundCalculator.calculateCashAmount(testTicket);

        assertThat(cashAmount).isEqualByComparingTo("50.00");
    }

    @Test
    void calculateCashAmountWhenTotalBookingPriceNegativeShouldFallBackToEqualSplit() {
        testTicket.getBooking().setTotalPrice(new BigDecimal("-10.00"));
        testTicket.getBooking().setSeatReservations(
                List.of(SeatReservation.builder().build(), SeatReservation.builder().build()));
        testTicket.getPayment().setAmount(new BigDecimal("100.00"));

        BigDecimal cashAmount = refundCalculator.calculateCashAmount(testTicket);

        assertThat(cashAmount).isEqualByComparingTo("50.00");
    }

    @Test
    void calculateCashAmountWhenTotalBookingPriceZeroAndNoSeatsShouldReturnFullPaymentAmount() {
        testTicket.getBooking().setTotalPrice(BigDecimal.ZERO);
        testTicket.getBooking().setSeatReservations(Collections.emptyList());
        testTicket.getPayment().setAmount(new BigDecimal("100.00"));

        BigDecimal cashAmount = refundCalculator.calculateCashAmount(testTicket);

        assertThat(cashAmount).isEqualByComparingTo("100.00");
    }

    @Test
    void calculateCashAmountWhenTotalBookingPricePositiveShouldProrateByFinalPrice() {
        testTicket.getBooking().setTotalPrice(new BigDecimal("200.00"));
        testTicket.setFinalPrice(new BigDecimal("40.00"));
        testTicket.getPayment().setAmount(new BigDecimal("100.00"));

        BigDecimal cashAmount = refundCalculator.calculateCashAmount(testTicket);

        assertThat(cashAmount).isEqualByComparingTo("20.00");
    }

    @Test
    void calculateRefundAmountAt100PercentShouldReturnFullPrice() {
        BigDecimal refundAmount = refundCalculator.calculateRefundAmount(new BigDecimal("100.00"),
                new BigDecimal("100"));

        assertThat(refundAmount).isEqualByComparingTo("100.00");
    }

    @Test
    void calculateRefundAmountAt0PercentShouldReturnZero() {
        BigDecimal refundAmount = refundCalculator.calculateRefundAmount(new BigDecimal("100.00"), BigDecimal.ZERO);

        assertThat(refundAmount).isEqualByComparingTo("0.00");
    }

    @Test
    void calculateRefundAmountShouldApplyPercentage() {
        BigDecimal refundAmount = refundCalculator.calculateRefundAmount(new BigDecimal("100.00"),
                new BigDecimal("70"));

        assertThat(refundAmount).isEqualByComparingTo("70.00");
    }

    @Test
    void calculateBonusRefundWhenNullShouldReturnZero() {
        Integer bonusRefund = refundCalculator.calculateBonusRefund(null, new BigDecimal("70"));

        assertThat(bonusRefund).isZero();
    }

    @Test
    void calculateBonusRefundWhenZeroShouldReturnZero() {
        Integer bonusRefund = refundCalculator.calculateBonusRefund(0, new BigDecimal("70"));

        assertThat(bonusRefund).isZero();
    }

    @Test
    void calculateBonusRefundAt100PercentShouldReturnFullPoints() {
        Integer bonusRefund = refundCalculator.calculateBonusRefund(50, new BigDecimal("100"));

        assertThat(bonusRefund).isEqualTo(50);
    }

    @Test
    void calculateBonusRefundAt0PercentShouldReturnZero() {
        Integer bonusRefund = refundCalculator.calculateBonusRefund(50, BigDecimal.ZERO);

        assertThat(bonusRefund).isZero();
    }

    @Test
    void calculateWhenNoSeatReservationsShouldReturnZeroBonusPointsUsed() {
        testTicket.getBooking().setSeatReservations(Collections.emptyList());
        testTicket.getBooking().setBonusPointsUsed(100);
        when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(new BigDecimal("70"));

        RefundCalculator.RefundCalculation result = refundCalculator.calculate(testTicket);

        assertThat(result.bonusPointsUsed()).isZero();
        assertThat(result.bonusPointsToRefund()).isZero();
    }

    @Test
    void calculateShouldReturnFullBreakdown() {
        SeatReservation otherSeatReservation = SeatReservation.builder().build();
        testTicket.getBooking().setSeatReservations(List.of(testTicket.getSeatReservation(), otherSeatReservation));
        testTicket.getBooking().setTotalPrice(new BigDecimal("200.00"));
        testTicket.getBooking().setBonusPointsUsed(100);
        testTicket.setFinalPrice(new BigDecimal("100.00"));
        testTicket.getPayment().setAmount(new BigDecimal("200.00"));
        when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(new BigDecimal("70"));

        RefundCalculator.RefundCalculation result = refundCalculator.calculate(testTicket);

        assertThat(result.percentage()).isEqualByComparingTo("70");
        assertThat(result.cashAmount()).isEqualByComparingTo("100.00");
        assertThat(result.refundAmount()).isEqualByComparingTo("70.00");
        assertThat(result.bonusPointsUsed()).isEqualTo(50);
        assertThat(result.bonusPointsToRefund()).isEqualTo(35);
    }
}
