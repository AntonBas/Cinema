package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ticket.TicketRetrievalService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

	@Mock
	private TicketRetrievalService ticketRetrievalService;

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

	private CustomUserDetails createUserDetails(Long id, String email) {
		User user = createUser(id, email);
		return new CustomUserDetails(user);
	}

	private TicketResponse createTicketResponse(Long id, String ticketCode, TicketStatus status) {
		return TicketResponse.builder().id(id).ticketCode(ticketCode).status(status).purchaseTime(LocalDateTime.now())
				.price(new BigDecimal("250.00")).ticketType("Adult").movieTitle("Inception")
				.sessionTime(LocalDateTime.now().plusDays(1)).hallName("Hall A").row(1).seatNumber(12).build();
	}

	@Test
	void getUserTickets_ShouldReturnTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 10);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE);
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", TicketStatus.USED);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket1, ticket2), pageable, 2);

		when(ticketRetrievalService.getUserTickets(any(User.class), any(TicketFilterRequest.class),
				any(Pageable.class))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, null, null,
				null, null, null, null, pageable);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getContent().size());
	}

	@Test
	void getUpcomingTickets_ShouldReturnTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 10);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket1), pageable, 1);

		when(ticketRetrievalService.getUserTickets(any(User.class), any(TicketFilterRequest.class),
				any(Pageable.class))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUpcomingTickets(userDetails,
				pageable);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().getContent().size());
	}

	@Test
	void getTicketById_ShouldReturnTicket() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		Long ticketId = 1L;

		TicketResponse ticket = createTicketResponse(ticketId, "TKT-20240115-ABC123", TicketStatus.ACTIVE);

		when(ticketRetrievalService.getTicketById(ticketId, userDetails.getUser())).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = ticketController.getTicketById(ticketId, userDetails);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(ticketId, response.getBody().getId());
	}

	@Test
	void getTicketByCode_ShouldReturnTicket() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		String ticketCode = "TKT-20240115-ABC123";

		TicketResponse ticket = createTicketResponse(1L, ticketCode, TicketStatus.ACTIVE);

		when(ticketRetrievalService.getTicketByCode(ticketCode, userDetails.getUser())).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = ticketController.getTicketByCode(ticketCode, userDetails);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(ticketCode, response.getBody().getTicketCode());
	}

	@Test
	void getTicketQRCode_ShouldReturnQRCode() {
		String ticketCode = "TKT-20240115-ABC123";
		byte[] qrCode = new byte[] { 1, 2, 3, 4, 5 };

		when(ticketService.generateTicketQRCode(ticketCode)).thenReturn(qrCode);

		ResponseEntity<byte[]> response = ticketController.getTicketQRCode(ticketCode);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(qrCode, response.getBody());
	}

	@Test
	void validateTicket_ShouldReturnOk() {
		String ticketCode = "TKT-20240115-ABC123";

		ResponseEntity<Void> response = ticketController.validateTicket(ticketCode);

		assertEquals(200, response.getStatusCode().value());
	}
}