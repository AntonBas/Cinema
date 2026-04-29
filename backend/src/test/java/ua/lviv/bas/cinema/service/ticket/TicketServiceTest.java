package ua.lviv.bas.cinema.service.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;
import ua.lviv.bas.cinema.service.common.NumberGeneratorService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketSpecification ticketSpecification;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private NumberGeneratorService numberGenerator;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Booking testBooking;
    private Payment testPayment;
    private SeatReservation seatReservation;
    private Ticket testTicket;
    private TicketResponse testTicketResponse;
    private TicketCashierResponse testTicketCashierResponse;
    private Session testSession;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TICKET_ID = 100L;
    private static final Long BOOKING_ID = 1L;
    private static final String TICKET_CODE = "TICKET-123";
    private static final String GENERATED_CODE = "TICKET-GEN-456";
    private static final BigDecimal PRICE = new BigDecimal("150.00");
    private static final int QR_CODE_SIZE = 200;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ticketService, "qrCodeSize", QR_CODE_SIZE);
        ReflectionTestUtils.setField(ticketService, "ticketBaseUrl", BASE_URL);

        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail("test@example.com");

        Seat testSeat = new Seat();
        testSeat.setRow(5);
        testSeat.setNumber(12);

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        CinemaHall hall = new CinemaHall();
        hall.setName("Hall A");

        testSession = new Session();
        testSession.setId(1L);
        testSession.setMovie(movie);
        testSession.setHall(hall);
        testSession.setStartTime(LocalDateTime.now().minusHours(1));
        testSession.setStatus(CinemaSessionStatus.SCHEDULED);

        testBooking = new Booking();
        testBooking.setId(BOOKING_ID);
        testBooking.setUser(testUser);
        testBooking.setSession(testSession);

        testPayment = new Payment();
        testPayment.setId(1L);

        TicketType ticketType = new TicketType();
        ticketType.setDisplayName("Adult");
        ticketType.setRequiresDocument(false);

        seatReservation = new SeatReservation();
        seatReservation.setSeat(testSeat);
        seatReservation.setTicketType(ticketType);
        seatReservation.setSeatPrice(PRICE);

        testTicket = new Ticket();
        testTicket.setId(TICKET_ID);
        testTicket.setUser(testUser);
        testTicket.setBooking(testBooking);
        testTicket.setSeatReservation(seatReservation);
        testTicket.setUniqueCode(TICKET_CODE);
        testTicket.setStatus(TicketStatus.ACTIVE);
        testTicket.setOriginalPrice(PRICE);
        testTicket.setFinalPrice(PRICE);
        testTicket.setPurchaseTime(LocalDateTime.now());

        testTicketResponse = new TicketResponse(TICKET_ID, TICKET_CODE, "/api/tickets/" + TICKET_CODE + "/qr",
                TicketStatus.ACTIVE, LocalDateTime.now(), PRICE, "Adult", "Test Movie", testSession.getStartTime(),
                "Hall A", 5, 12);

        testTicketCashierResponse = new TicketCashierResponse(TICKET_ID, TICKET_CODE, TicketStatus.ACTIVE,
                "Test Movie", testSession.getStartTime(), "Hall A", "5", 12,
                "Adult", false, null, "test@example.com", PRICE);
    }

    @Test
    void createTicketsForBookingShouldSucceed() {
        testBooking.setSeatReservations(List.of(seatReservation));

        when(numberGenerator.generateTicketCode()).thenReturn(GENERATED_CODE);
        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.createTicketsForBooking(testBooking, testPayment);

        verify(ticketRepository).saveAll(anyList());
        verify(auditService).logChange(eq("Ticket"), any(), any(), eq(ua.lviv.bas.cinema.domain.audit.AuditAction.CREATED), eq(null), any());
    }

    @Test
    void getTicketByCodeShouldSucceed() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
        when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

        TicketResponse result = ticketService.getTicket(TICKET_CODE, testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TICKET_ID);
        assertThat(result.ticketCode()).isEqualTo(TICKET_CODE);
    }

    @Test
    void getTicketByCodeWhenNotFoundShouldThrowException() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, testUser))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void getTicketByCodeWithWrongUserShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(OTHER_USER_ID);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, otherUser))
                .isInstanceOf(TicketValidationException.class);
    }

    @Test
    void getTicketForCashierShouldSucceed() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
        when(ticketMapper.toTicketCashierResponse(testTicket)).thenReturn(testTicketCashierResponse);

        TicketCashierResponse result = ticketService.getTicketForCashier(TICKET_CODE);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TICKET_ID);
        assertThat(result.uniqueCode()).isEqualTo(TICKET_CODE);
        assertThat(result.userEmail()).isEqualTo("test@example.com");
        assertThat(result.movieTitle()).isEqualTo("Test Movie");
        assertThat(result.hallName()).isEqualTo("Hall A");
        assertThat(result.seatRow()).isEqualTo("5");
        assertThat(result.seatNumber()).isEqualTo(12);
        assertThat(result.ticketType()).isEqualTo("Adult");
        assertThat(result.requiresDocument()).isFalse();
        assertThat(result.documentType()).isNull();
    }

    @Test
    void getTicketForCashierWhenNotFoundShouldThrowException() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketForCashier(TICKET_CODE))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void getTicketsShouldSucceed() {
        TicketStatus status = TicketStatus.ACTIVE;
        String movieTitle = "Test Movie";
        Pageable pageable = Pageable.unpaged();

        @SuppressWarnings("unchecked")
        Specification<Ticket> specification = (Specification<Ticket>) mock(Specification.class);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

        when(ticketSpecification.buildForUser(eq(USER_ID), eq(status), eq(movieTitle)))
                .thenReturn(specification);
        when(ticketRepository.findAll(eq(specification), eq(pageable))).thenReturn(ticketPage);
        when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

        Page<TicketResponse> result = ticketService.getTickets(testUser, status, movieTitle, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(TICKET_ID);
    }

    @Test
    void getTicketsWithNullFiltersShouldSucceed() {
        Pageable pageable = Pageable.unpaged();

        @SuppressWarnings("unchecked")
        Specification<Ticket> specification = (Specification<Ticket>) mock(Specification.class);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

        when(ticketSpecification.buildForUser(eq(USER_ID), eq(null), eq(null)))
                .thenReturn(specification);
        when(ticketRepository.findAll(eq(specification), eq(pageable))).thenReturn(ticketPage);
        when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

        Page<TicketResponse> result = ticketService.getTickets(testUser, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(TICKET_ID);
    }

    @Test
    void getTicketsWithStatusOnlyShouldSucceed() {
        Pageable pageable = Pageable.unpaged();
        testTicket.setStatus(TicketStatus.USED);

        @SuppressWarnings("unchecked")
        Specification<Ticket> specification = (Specification<Ticket>) mock(Specification.class);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

        when(ticketSpecification.buildForUser(eq(USER_ID), eq(TicketStatus.USED), eq(null)))
                .thenReturn(specification);
        when(ticketRepository.findAll(eq(specification), eq(pageable))).thenReturn(ticketPage);
        when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

        Page<TicketResponse> result = ticketService.getTickets(testUser, TicketStatus.USED, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getTicketsWithMovieTitleOnlyShouldSucceed() {
        Pageable pageable = Pageable.unpaged();

        @SuppressWarnings("unchecked")
        Specification<Ticket> specification = (Specification<Ticket>) mock(Specification.class);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

        when(ticketSpecification.buildForUser(eq(USER_ID), eq(null), eq("Test")))
                .thenReturn(specification);
        when(ticketRepository.findAll(eq(specification), eq(pageable))).thenReturn(ticketPage);
        when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

        Page<TicketResponse> result = ticketService.getTickets(testUser, null, "Test", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void validateShouldSucceed() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(testTicket)).thenReturn(testTicket);
        when(ticketMapper.toTicketCashierResponse(testTicket)).thenReturn(testTicketCashierResponse);

        TicketCashierResponse result = ticketService.validate(TICKET_CODE);

        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(result).isNotNull();
        assertThat(result.uniqueCode()).isEqualTo(TICKET_CODE);
        verify(ticketRepository).save(testTicket);
        verify(auditService).logChange(eq("Ticket"), eq(TICKET_ID), any(), eq(ua.lviv.bas.cinema.domain.audit.AuditAction.VALIDATED), any(), any());
    }

    @Test
    void validateWhenNotFoundShouldThrowException() {
        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class);
    }

    @Test
    void validateWhenSessionNotStartedShouldThrowException() {
        testSession.setStartTime(LocalDateTime.now().plusHours(2));
        testTicket.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class)
                .hasMessageContaining("Session has not started yet");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void validateWhenSessionEndedMoreThanTwoHoursAgoShouldThrowException() {
        testSession.setStartTime(LocalDateTime.now().minusHours(3));
        testTicket.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class)
                .hasMessageContaining("Session ended more than 2 hours ago");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void validateWhenSessionCancelledShouldThrowException() {
        testSession.setStatus(CinemaSessionStatus.CANCELLED);
        testTicket.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class)
                .hasMessageContaining("Session has been cancelled");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void validateWhenTicketAlreadyUsedShouldThrowException() {
        testTicket.setStatus(TicketStatus.USED);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class)
                .hasMessageContaining("already been used");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void validateWhenTicketRefundedShouldThrowException() {
        testTicket.setStatus(TicketStatus.REFUNDED);

        when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.validate(TICKET_CODE))
                .isInstanceOf(TicketValidationException.class)
                .hasMessageContaining("refunded");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void generateQRShouldSucceed() {
        String expectedQrContent = BASE_URL + "/cashier/scan/" + TICKET_CODE;
        byte[] expectedQrCode = new byte[]{1, 2, 3};

        when(qrCodeService.generateQRCode(expectedQrContent, QR_CODE_SIZE)).thenReturn(expectedQrCode);

        byte[] result = ticketService.generateQR(TICKET_CODE);

        assertThat(result).isEqualTo(expectedQrCode);
        verify(qrCodeService).generateQRCode(expectedQrContent, QR_CODE_SIZE);
    }
}