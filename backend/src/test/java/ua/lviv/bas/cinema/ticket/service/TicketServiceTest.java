package ua.lviv.bas.cinema.ticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.ticket.dto.response.TicketCashierResponse;
import ua.lviv.bas.cinema.ticket.dto.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.ticket.mapper.TicketMapper;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.integration.QRCodeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private TicketService ticketService;

    private static final String TICKET_CODE = "TKT-TEST123";
    private static final Long USER_ID = 1L;

    private Ticket testTicket;
    private User testUser;
    private Session testSession;
    private TicketCashierResponse cashierResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);

        testSession = new Session();
        testSession.setId(69L);
        testSession.setStartTime(LocalDateTime.now().minusMinutes(30));
        testSession.setStatus(CinemaSessionStatus.ONGOING);

        CinemaHall hall = new CinemaHall();
        hall.setId(1L);
        hall.setName("Hall A");
        testSession.setHall(hall);

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        testSession.setMovie(movie);

        Booking testBooking = new Booking();
        testBooking.setId(72L);
        testBooking.setSession(testSession);
        testBooking.setUser(testUser);

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setUniqueCode(TICKET_CODE);
        testTicket.setStatus(TicketStatus.ACTIVE);
        testTicket.setBooking(testBooking);
        testTicket.setUser(testUser);

        cashierResponse = new TicketCashierResponse(
                1L, TICKET_CODE, TicketStatus.USED, "Test Movie",
                LocalDateTime.now(), "Hall A", "5", 10, "Standard",
                false, null, "test@email.com", BigDecimal.ZERO
        );

        lenient().doNothing().when(auditService).logChange(any(), any(), any(), any(), any(), any());
        ReflectionTestUtils.setField(ticketService, "ticketBaseUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(ticketService, "qrCodeSize", 200);
    }

    @Nested
    class ValidateTests {

        @Test
        void validateShouldSucceed() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
            when(ticketRepository.updateStatusIfCurrent(eq(1L), eq(TicketStatus.ACTIVE), eq(TicketStatus.USED)))
                    .thenReturn(1);
            when(ticketRepository.save(any())).thenReturn(testTicket);
            when(ticketMapper.toTicketCashierResponse(any())).thenReturn(cashierResponse);

            TicketCashierResponse result = ticketService.validate(TICKET_CODE);

            assertThat(result).isEqualTo(cashierResponse);
            assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
            verify(ticketRepository).save(testTicket);
        }

        @Test
        void validateWhenNotFoundShouldThrowException() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class);
        }

        @Test
        void validateWhenAlreadyUsedShouldThrowException() {
            testTicket.setStatus(TicketStatus.USED);
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("already been used");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        void validateWhenRefundedShouldThrowException() {
            testTicket.setStatus(TicketStatus.REFUNDED);
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("refunded");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        void validateWhenExpiredShouldThrowException() {
            testTicket.setStatus(TicketStatus.EXPIRED);
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("not active");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        void validateWhenTooEarlyShouldThrowException() {
            testSession.setStartTime(LocalDateTime.now().plusHours(2));
            testTicket.setStatus(TicketStatus.ACTIVE);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("Too early");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        void validateWithin1HourBeforeSessionShouldSucceed() {
            testSession.setStartTime(LocalDateTime.now().plusMinutes(30));
            testSession.setStatus(CinemaSessionStatus.SCHEDULED);
            testTicket.setStatus(TicketStatus.ACTIVE);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
            when(ticketRepository.updateStatusIfCurrent(eq(1L), eq(TicketStatus.ACTIVE), eq(TicketStatus.USED)))
                    .thenReturn(1);
            when(ticketRepository.save(any())).thenReturn(testTicket);
            when(ticketMapper.toTicketCashierResponse(any())).thenReturn(cashierResponse);

            TicketCashierResponse result = ticketService.validate(TICKET_CODE);

            assertThat(result).isEqualTo(cashierResponse);
            verify(ticketRepository).save(testTicket);
        }

        @Test
        void validateWhenSessionEndedMoreThan2HoursAgoShouldThrowException() {
            testSession.setStartTime(LocalDateTime.now().minusHours(3));
            testTicket.setStatus(TicketStatus.ACTIVE);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("ended more than 2 hours ago");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        void validateWhenSessionCancelledShouldThrowException() {
            testSession.setStartTime(LocalDateTime.now().minusMinutes(10));
            testSession.setStatus(CinemaSessionStatus.CANCELLED);
            testTicket.setStatus(TicketStatus.ACTIVE);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                    .isInstanceOf(TicketValidationException.class)
                    .hasMessageContaining("cancelled");

            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    class GetTicketTests {

        @Test
        void getTicketShouldSucceed() {
            TicketResponse mockResponse = new TicketResponse(1L, TICKET_CODE, "/qr", TicketStatus.ACTIVE,
                    LocalDateTime.now(), BigDecimal.TEN, "Standard", "Test Movie",
                    LocalDateTime.now(), "Hall A", 5, 10);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
            when(ticketMapper.toTicketResponse(testTicket)).thenReturn(mockResponse);

            var result = ticketService.getTicket(TICKET_CODE, testUser);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        void getTicketWhenNotFoundShouldThrowException() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, testUser))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        void getTicketWhenWrongUserShouldThrowException() {
            User otherUser = new User();
            otherUser.setId(999L);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, otherUser))
                    .isInstanceOf(TicketValidationException.class);
        }
    }

    @Nested
    class GenerateQRTests {

        @Test
        void generateQRShouldSucceed() {
            byte[] qrCode = new byte[]{1, 2, 3, 4, 5};

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
            when(qrCodeService.generateQRCode(anyString(), eq(200))).thenReturn(qrCode);

            var result = ticketService.generateQR(TICKET_CODE, testUser);

            assertThat(result).isEqualTo(qrCode);
        }

        @Test
        void generateQRWhenNotFoundShouldThrowException() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.generateQR(TICKET_CODE, testUser))
                    .isInstanceOf(TicketValidationException.class);
        }

        @Test
        void generateQRWhenWrongUserShouldThrowException() {
            User otherUser = new User();
            otherUser.setId(999L);

            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

            assertThatThrownBy(() -> ticketService.generateQR(TICKET_CODE, otherUser))
                    .isInstanceOf(TicketValidationException.class);

            verify(qrCodeService, never()).generateQRCode(anyString(), anyInt());
        }
    }

    @Nested
    class GetTicketForCashierTests {

        @Test
        void getTicketForCashierShouldSucceed() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
            when(ticketMapper.toTicketCashierResponse(testTicket)).thenReturn(cashierResponse);

            var result = ticketService.getTicketForCashier(TICKET_CODE);

            assertThat(result).isEqualTo(cashierResponse);
        }

        @Test
        void getTicketForCashierWhenNotFoundShouldThrowException() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getTicketForCashier(TICKET_CODE))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    @Nested
    class MarkAsRefundedTests {

        @Test
        void markAsRefundedShouldSetStatusAndRefund() {
            var refund = Refund.builder().id(5L).build();

            ticketService.markAsRefunded(testTicket, refund);

            assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.REFUNDED);
            assertThat(testTicket.getRefund()).isEqualTo(refund);
            verify(ticketRepository).save(testTicket);
        }

        @Test
        void markAsRefundedWhenAlreadyRefundedShouldSkip() {
            testTicket.setStatus(TicketStatus.REFUNDED);
            var refund = Refund.builder().id(5L).build();

            ticketService.markAsRefunded(testTicket, refund);

            assertThat(testTicket.getRefund()).isNull();
            verify(ticketRepository, never()).save(any());
        }
    }
}