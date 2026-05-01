package ua.lviv.bas.cinema.service.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private AuditService auditService;

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
    }

    @Nested
    class ValidateTests {

        @Test
        void validateShouldSucceed() {
            when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
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
}