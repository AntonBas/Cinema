package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private final String TICKET_CODE = "TKT-20240115-ABC123";

    private User createUser() {
        User user = new User();
        Long USER_ID = 1L;
        user.setId(USER_ID);
        String EMAIL = "user@example.com";
        user.setEmail(EMAIL);
        return user;
    }

    private CustomUserDetails createUserDetails() {
        return new CustomUserDetails(createUser());
    }

    private TicketResponse createTicketResponse() {
        return new TicketResponse(1L, "TKT-20240115-ABC123", "/api/tickets/code/" + "TKT-20240115-ABC123" + "/qr", TicketStatus.ACTIVE, LocalDateTime.now(),
                new BigDecimal("250.00"), "Adult", "Inception", LocalDateTime.now().plusDays(1), "Hall A", 1, 12);
    }

    @Test
    void getTicketsWithoutFiltersShouldReturnPage() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 10);
        TicketResponse ticket = createTicketResponse();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);

        when(ticketService.getTickets(eq(user), isNull(), isNull(), eq(pageable))).thenReturn(page);

        PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        verify(ticketService).getTickets(eq(user), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getTicketsWithStatusFilterShouldReturnPage() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 10);
        TicketResponse ticket = createTicketResponse();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
        TicketStatus status = TicketStatus.ACTIVE;

        when(ticketService.getTickets(eq(user), eq(status), isNull(), eq(pageable))).thenReturn(page);

        PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, status, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        verify(ticketService).getTickets(eq(user), eq(status), isNull(), eq(pageable));
    }

    @Test
    void getTicketsWithMovieTitleFilterShouldReturnPage() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 10);
        TicketResponse ticket = createTicketResponse();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
        String movieTitle = "Inception";

        when(ticketService.getTickets(eq(user), isNull(), eq(movieTitle), eq(pageable))).thenReturn(page);

        PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, movieTitle, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        verify(ticketService).getTickets(eq(user), isNull(), eq(movieTitle), eq(pageable));
    }

    @Test
    void getTicketsWithBothFiltersShouldReturnPage() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 10);
        TicketResponse ticket = createTicketResponse();
        Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
        TicketStatus status = TicketStatus.ACTIVE;
        String movieTitle = "Inception";

        when(ticketService.getTickets(eq(user), eq(status), eq(movieTitle), eq(pageable))).thenReturn(page);

        PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, status, movieTitle, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        verify(ticketService).getTickets(eq(user), eq(status), eq(movieTitle), eq(pageable));
    }

    @Test
    void getTicketsWithEmptyPageShouldReturnEmptyPage() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TicketResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(ticketService.getTickets(eq(user), isNull(), isNull(), eq(pageable))).thenReturn(emptyPage);

        PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        verify(ticketService).getTickets(eq(user), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getTicketByCodeShouldReturnTicket() {
        CustomUserDetails userDetails = createUserDetails();
        User user = userDetails.getUser();
        TicketResponse ticket = createTicketResponse();

        when(ticketService.getTicket(TICKET_CODE, user)).thenReturn(ticket);

        TicketResponse response = ticketController.getTicket(TICKET_CODE, userDetails);

        assertThat(response).isNotNull();
        assertThat(response.ticketCode()).isEqualTo(TICKET_CODE);
        verify(ticketService).getTicket(TICKET_CODE, user);
    }

    @Test
    void getQRShouldReturnImage() {
        byte[] qrCode = new byte[]{1, 2, 3, 4, 5};

        when(ticketService.generateQR(TICKET_CODE)).thenReturn(qrCode);

        byte[] response = ticketController.getQR(TICKET_CODE);

        assertThat(response).isEqualTo(qrCode);
        verify(ticketService).generateQR(TICKET_CODE);
    }
}