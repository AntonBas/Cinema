package ua.lviv.bas.cinema.ticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.common.SeatInfoFormatter;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.notification.EmailAttachment;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.notification.TicketsEmail;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.user.domain.User;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketsEmailServiceTest {

    private static final Long BOOKING_ID = 2L;
    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketService ticketService;
    @Mock
    private EmailService emailService;
    @Mock
    private DateTimeFormatterService dateTimeFormatter;
    @Mock
    private NumberGeneratorService numberGenerator;
    @Mock
    private SeatInfoFormatter seatInfoFormatter;
    @Mock
    private PlatformTransactionManager transactionManager;

    private TicketsEmailService ticketsEmailService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        ticketsEmailService = new TicketsEmailService(bookingRepository, ticketRepository, ticketService, emailService,
                dateTimeFormatter, numberGenerator, seatInfoFormatter, transactionManager);

        var user = User.builder().id(1L).email("test@example.com").build();
        var session = CinemaTestFixtures.session(CinemaTestFixtures.movie(), CinemaTestFixtures.hall());
        var seatReservation = SeatReservation.builder().seat(Seat.builder().row(1).number(1).build())
                .status(ReservationStatus.CONFIRMED).build();
        booking = Booking.builder().id(BOOKING_ID).user(user).session(session).status(BookingStatus.CONFIRMED)
                .totalPrice(AMOUNT).finalPrice(AMOUNT).expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .seatReservations(List.of(seatReservation)).build();
        booking.setPayment(Payment.builder().booking(booking).amount(AMOUNT).status(PaymentStatus.SUCCESS).build());

        lenient().when(bookingRepository.findWithDetailsById(BOOKING_ID)).thenReturn(Optional.of(booking));
        var ticket = Ticket.builder().uniqueCode("TKT-AAA").build();
        lenient().when(ticketRepository.findByBookingId(BOOKING_ID)).thenReturn(List.of(ticket));
        lenient().when(ticketService.generateQR(ticket)).thenReturn(new byte[]{1});
        lenient().when(seatInfoFormatter.format(1, 1)).thenReturn("Row 1, Seat 1");
        lenient().when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        lenient().when(numberGenerator.generateBookingNumber(booking)).thenReturn("BK-2024-00001");
    }

    @Test
    void sendAndMarkSentShouldSendTicketsEmailAndMarkBookingAsSent() {
        ticketsEmailService.sendAndMarkSent(BOOKING_ID);

        var emailCaptor = ArgumentCaptor.forClass(TicketsEmail.class);
        verify(emailService).sendTicketsEmail(emailCaptor.capture());
        var email = emailCaptor.getValue();
        assertThat(email.toEmail()).isEqualTo("test@example.com");
        assertThat(email.bookingNumber()).isEqualTo("BK-2024-00001");
        assertThat(email.seatInfo()).isEqualTo("Row 1, Seat 1");
        assertThat(email.amountPaid()).isEqualTo(AMOUNT);
        assertThat(email.ticketCodes()).containsExactly("TKT-AAA");
        assertThat(email.qrCodes()).extracting(EmailAttachment::name).containsExactly("TKT-AAA.png");
        verify(bookingRepository).markTicketsEmailSent(eq(BOOKING_ID), any(Instant.class));
    }

    @Test
    void sendAndMarkSentWhenEmailFailsShouldNotMarkBookingAsSent() {
        doThrow(new ExternalServiceException("Email Service", new RuntimeException("Brevo down")))
                .when(emailService).sendTicketsEmail(any());

        assertThatThrownBy(() -> ticketsEmailService.sendAndMarkSent(BOOKING_ID))
                .isInstanceOf(ExternalServiceException.class);

        verify(bookingRepository, never()).markTicketsEmailSent(any(), any());
    }

    @Test
    void sendAndMarkSentWhenAlreadySentShouldSkipEmail() {
        booking.setTicketsEmailSentAt(Instant.now());

        ticketsEmailService.sendAndMarkSent(BOOKING_ID);

        verify(emailService, never()).sendTicketsEmail(any());
        verify(bookingRepository, never()).markTicketsEmailSent(any(), any());
    }
}
