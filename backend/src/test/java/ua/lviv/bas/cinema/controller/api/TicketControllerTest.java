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
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
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
		return new TicketResponse(id, code, null, status, LocalDateTime.now(), new BigDecimal("250.00"), "Adult",
				"Inception", LocalDateTime.now().plusDays(1), "Hall A", 1, 12);
	}

	@Test
	void getUserTickets_WithoutFilters_ReturnsPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);

		when(ticketRetrievalService.getUserTickets(eq(user), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, null, null,
				pageable);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(1);
		verify(ticketRetrievalService).getUserTickets(eq(user), any(), eq(pageable));
	}

	@Test
	void getUserTickets_WithStatusFilter_ReturnsPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
		TicketStatus status = TicketStatus.ACTIVE;

		when(ticketRetrievalService.getUserTickets(eq(user), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, status,
				null, pageable);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(1);
		verify(ticketRetrievalService).getUserTickets(eq(user), any(), eq(pageable));
	}

	@Test
	void getUserTickets_WithMovieTitleFilter_ReturnsPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
		String movieTitle = "Inception";

		when(ticketRetrievalService.getUserTickets(eq(user), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, null,
				movieTitle, pageable);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(1);
		verify(ticketRetrievalService).getUserTickets(eq(user), any(), eq(pageable));
	}

	@Test
	void getUserTickets_WithAllFilters_ReturnsPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);
		Page<TicketResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
		TicketStatus status = TicketStatus.ACTIVE;
		String movieTitle = "Inception";

		when(ticketRetrievalService.getUserTickets(eq(user), any(), eq(pageable))).thenReturn(page);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, status,
				movieTitle, pageable);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).hasSize(1);
		verify(ticketRetrievalService).getUserTickets(eq(user), any(), eq(pageable));
	}

	@Test
	void getUserTickets_WithEmptyPage_ReturnsEmptyPage() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		Pageable pageable = PageRequest.of(0, 10);
		Page<TicketResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(ticketRetrievalService.getUserTickets(eq(user), any(), eq(pageable))).thenReturn(emptyPage);

		ResponseEntity<PageResponse<TicketResponse>> response = ticketController.getUserTickets(userDetails, null, null,
				pageable);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().content()).isEmpty();
		assertThat(response.getBody().totalElements()).isZero();
		verify(ticketRetrievalService).getUserTickets(eq(user), any(), eq(pageable));
	}

	@Test
	void getTicketByCode_ReturnsTicket() {
		CustomUserDetails userDetails = createUserDetails();
		User user = userDetails.getUser();
		TicketResponse ticket = createTicketResponse(1L, TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketRetrievalService.getTicketByCode(TICKET_CODE, user)).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = ticketController.getTicketByCode(TICKET_CODE, userDetails);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().ticketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRetrievalService).getTicketByCode(TICKET_CODE, user);
	}

	@Test
	void getTicketQRCode_ReturnsImage() {
		byte[] qrCode = new byte[] { 1, 2, 3, 4, 5 };

		when(ticketService.generateTicketQRCode(TICKET_CODE)).thenReturn(qrCode);

		ResponseEntity<byte[]> response = ticketController.getTicketQRCode(TICKET_CODE);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
		assertThat(response.getBody()).isEqualTo(qrCode);
		verify(ticketService).generateTicketQRCode(TICKET_CODE);
	}

	@Test
	void validateTicket_ReturnsOk() {
		ResponseEntity<Void> response = ticketController.validateTicket(TICKET_CODE);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		verify(ticketService).validateTicket(TICKET_CODE);
	}
}