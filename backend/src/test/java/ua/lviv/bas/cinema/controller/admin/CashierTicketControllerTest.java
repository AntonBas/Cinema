package ua.lviv.bas.cinema.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CashierTicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private CashierTicketController controller;

    private static final String TICKET_CODE = "TKT-B1F0445950FE";

    private final TicketCashierResponse testResponse = new TicketCashierResponse(
            123L, TICKET_CODE, TicketStatus.ACTIVE,
            "Dune: Part Two", LocalDateTime.of(2026, 4, 17, 20, 0),
            "Hall №1", "4", 8,
            "Student", true, "Student ID",
            "basantonoleg@gmail.com", new BigDecimal("500.00")
    );

    @Test
    void getTicketShouldReturnTicketInfo() {
        when(ticketService.getTicketForCashier(TICKET_CODE)).thenReturn(testResponse);

        ResponseEntity<TicketCashierResponse> result = controller.getTicket(TICKET_CODE);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(123L);
        assertThat(result.getBody().uniqueCode()).isEqualTo(TICKET_CODE);
    }

    @Test
    void getTicketWhenNotFoundShouldThrowException() {
        when(ticketService.getTicketForCashier(TICKET_CODE))
                .thenThrow(new TicketNotFoundException("Ticket not found"));

        assertThatThrownBy(() -> controller.getTicket(TICKET_CODE))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void validateTicketShouldReturnValidatedTicket() {
        when(ticketService.validate(TICKET_CODE)).thenReturn(testResponse);

        ResponseEntity<TicketCashierResponse> result = controller.validateTicket(TICKET_CODE);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().status()).isEqualTo(TicketStatus.ACTIVE);
    }

    @Test
    void validateTicketWhenNotFoundShouldThrowException() {
        when(ticketService.validate(TICKET_CODE))
                .thenThrow(new TicketNotFoundException("Ticket not found"));

        assertThatThrownBy(() -> controller.validateTicket(TICKET_CODE))
                .isInstanceOf(TicketNotFoundException.class);
    }
}