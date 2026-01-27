package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

	@Mock
	private ControllerFacade controllerFacade;

	@Mock
	private TicketService ticketService;

	@InjectMocks
	private TicketController ticketController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		return user;
	}

	private TicketResponse createTicketResponse(Long id, String ticketCode, TicketStatus status) {
		return TicketResponse.builder().id(id).ticketCode(ticketCode).status(status).purchaseTime(LocalDateTime.now())
				.price(new BigDecimal("250.00")).ticketType("Adult").movieTitle("Inception")
				.sessionTime(LocalDateTime.now().plusDays(1)).hallName("Hall A").row(1).seatNumber(12).build();
	}

	@Test
	void getUserTickets_ShouldReturnTickets() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = new CustomUserDetails(user);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE);
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", TicketStatus.USED);
		List<TicketResponse> tickets = Arrays.asList(ticket1, ticket2);

		when(controllerFacade.getUserTickets(user, null)).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(null, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
	}

	@Test
	void getUpcomingTickets_ShouldReturnTickets() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = new CustomUserDetails(user);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE);
		List<TicketResponse> tickets = Arrays.asList(ticket1);

		when(controllerFacade.getUpcomingTickets(user)).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUpcomingTickets(userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().size());
	}

	@Test
	void getTicketById_ShouldReturnTicket() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = new CustomUserDetails(user);
		Long ticketId = 1L;

		TicketResponse ticket = createTicketResponse(ticketId, "TKT-20240115-ABC123", TicketStatus.ACTIVE);

		when(controllerFacade.getTicketById(ticketId, user)).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = ticketController.getTicketById(ticketId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(ticketId, response.getBody().getId());
	}

	@Test
	void getTicketByCode_ShouldReturnTicket() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String ticketCode = "TKT-20240115-ABC123";

		TicketResponse ticket = createTicketResponse(1L, ticketCode, TicketStatus.ACTIVE);

		when(controllerFacade.getTicketByCode(ticketCode, user)).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = ticketController.getTicketByCode(ticketCode, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(ticketCode, response.getBody().getTicketCode());
	}
}