package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

	@Mock
	private TicketService ticketService;

	@InjectMocks
	private TicketController ticketController;

	private final Long USER_ID = 1L;
	private final String EMAIL = "user@example.com";
	private final String TICKET_CODE = "TKT-20240115-ABC123";

	private User createUser() {
		User user = new User();
		user.setId(USER_ID);
		user.setEmail(EMAIL);
		return user;
	}

	private CustomUserDetails createUserDetails() {
		return new CustomUserDetails(createUser());
	}

	private TicketResponse createTicketResponse(Long id, String code, TicketStatus status) {
		return new TicketResponse(id, code, "/api/tickets/code/" + code + "/qr", status, LocalDateTime.now(),
				new BigDecimal("250.00"), "Adult", "Inception", LocalDateTime.now().plusDays(1), "Hall A", 1, 12);
	}

	@Test
	void getTicketsWithoutFiltersShouldReturnPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);

		when(ticketService.getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable))).thenReturn(page);

		PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		verify(ticketService).getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable));
	}

	@Test
	void getTicketsWithStatusFilterShouldReturnPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
		TicketStatus status = TicketStatus.ACTIVE;

		when(ticketService.getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable))).thenReturn(page);

		PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, status, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		verify(ticketService).getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable));
	}

	@Test
	void getTicketsWithMovieTitleFilterShouldReturnPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
		String movieTitle = "Inception";

		when(ticketService.getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable))).thenReturn(page);

		PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, movieTitle, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		verify(ticketService).getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable));
	}

	@Test
	void getTicketsWithEmptyPageShouldReturnEmptyPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		Page<TicketResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(ticketService.getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable))).thenReturn(emptyPage);

		PageResponse<TicketResponse> response = ticketController.getTickets(userDetails, null, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).isEmpty();
		assertThat(response.totalElements()).isZero();
		verify(ticketService).getTickets(eq(user), any(TicketFilterRequest.class), eq(pageable));
	}

	@Test
	void getTicketByCodeShouldReturnTicket() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketService.getTicket(TICKET_CODE, user)).thenReturn(ticket);

		TicketResponse response = ticketController.getTicket(TICKET_CODE, userDetails);

		assertThat(response).isNotNull();
		assertThat(response.ticketCode()).isEqualTo(TICKET_CODE);
		verify(ticketService).getTicket(TICKET_CODE, user);
	}

	@Test
	void getQRShouldReturnImage() {
		byte[] qrCode = new byte[] { 1, 2, 3, 4, 5 };

		when(ticketService.generateQR(TICKET_CODE)).thenReturn(qrCode);

		byte[] response = ticketController.getQR(TICKET_CODE);

		assertThat(response).isEqualTo(qrCode);
		verify(ticketService).generateQR(TICKET_CODE);
	}

	@Test
	void validateShouldCallService() {
		ticketController.validate(TICKET_CODE);

		verify(ticketService).validate(TICKET_CODE);
	}
}