package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.TicketService;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

	@Mock
	private TicketService ticketService;

	@InjectMocks
	private TicketController ticketController;

	private CustomUserDetails createUserDetails(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setFirstName("John");
		user.setLastName("Doe");

		return new CustomUserDetails(user);
	}

	private void setupSecurityContext(CustomUserDetails userDetails) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private TicketResponse createTicketResponse(Long id, String ticketCode, TicketStatus status, String movieTitle) {
		return TicketResponse.builder().id(id).ticketCode(ticketCode).qrCodeUrl("/api/tickets/" + ticketCode + "/qr")
				.status(status).purchaseTime(LocalDateTime.now()).price(new BigDecimal("250.00")).ticketType("Adult")
				.movieTitle(movieTitle).sessionTime(LocalDateTime.now().plusDays(1)).hallName("Hall A").row(1)
				.seatNumber(12).build();
	}

	@Test
	void getTicket_ShouldReturnTicket_WhenExistsAndAuthorized() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 1L;

		TicketResponse ticketResponse = createTicketResponse(ticketId, "TKT-20240115-ABC123", TicketStatus.ACTIVE,
				"Inception");

		when(ticketService.getTicketById(ticketId, userDetails.getUser())).thenReturn(ticketResponse);

		ResponseEntity<TicketResponse> response = ticketController.getTicket(ticketId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(ticketId, response.getBody().getId());
		assertEquals("TKT-20240115-ABC123", response.getBody().getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getBody().getStatus());
		verify(ticketService).getTicketById(ticketId, userDetails.getUser());
	}

	@Test
	void getTicket_ShouldThrowException_WhenNotFound() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 999L;

		when(ticketService.getTicketById(ticketId, userDetails.getUser()))
				.thenThrow(new TicketNotFoundException(ticketId));

		assertThrows(TicketNotFoundException.class, () -> ticketController.getTicket(ticketId, userDetails));
		verify(ticketService).getTicketById(ticketId, userDetails.getUser());
	}

	@Test
	void getUserTickets_ShouldReturnTickets_WhenNoStatusFilter() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE, "Movie 1");
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", TicketStatus.USED, "Movie 2");
		List<TicketResponse> tickets = Arrays.asList(ticket1, ticket2);

		when(ticketService.getUserTickets(userDetails.getUser(), null)).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(null, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		verify(ticketService).getUserTickets(userDetails.getUser(), null);
	}

	@Test
	void getUserTickets_ShouldReturnFilteredTickets_WhenStatusFiltered() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		TicketStatus status = TicketStatus.ACTIVE;

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", status, "Movie 1");
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", status, "Movie 2");
		List<TicketResponse> tickets = Arrays.asList(ticket1, ticket2);

		when(ticketService.getUserTickets(userDetails.getUser(), status)).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(status, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		response.getBody().forEach(ticket -> assertEquals(status, ticket.getStatus()));
		verify(ticketService).getUserTickets(userDetails.getUser(), status);
	}

	@Test
	void getUserTickets_ShouldReturnEmptyList_WhenNoTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);

		List<TicketResponse> emptyList = Arrays.asList();

		when(ticketService.getUserTickets(userDetails.getUser(), null)).thenReturn(emptyList);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(null, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(ticketService).getUserTickets(userDetails.getUser(), null);
	}

	@Test
	void getBookingTickets_ShouldReturnTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long bookingId = 100L;

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE, "Movie 1");
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", TicketStatus.ACTIVE, "Movie 1");
		List<TicketResponse> tickets = Arrays.asList(ticket1, ticket2);

		when(ticketService.getBookingTickets(bookingId, userDetails.getUser())).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getBookingTickets(bookingId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		verify(ticketService).getBookingTickets(bookingId, userDetails.getUser());
	}

	@Test
	void getBookingTickets_ShouldThrowException_WhenBookingNotFound() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long bookingId = 999L;

		when(ticketService.getBookingTickets(bookingId, userDetails.getUser()))
				.thenThrow(new RuntimeException("Booking not found"));

		assertThrows(RuntimeException.class, () -> ticketController.getBookingTickets(bookingId, userDetails));
		verify(ticketService).getBookingTickets(bookingId, userDetails.getUser());
	}

	@Test
	void validateTicket_ShouldValidateSuccessfully() {
		String ticketCode = "TKT-20240115-ABC123";

		doNothing().when(ticketService).validateTicket(ticketCode);

		ResponseEntity<String> response = ticketController.validateTicket(ticketCode);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Ticket validated successfully", response.getBody());
		verify(ticketService).validateTicket(ticketCode);
	}

	@Test
	void validateTicket_ShouldThrowException_WhenValidationFailed() {
		String ticketCode = "TKT-20240115-ABC123";

		doThrow(new RuntimeException("Ticket validation failed")).when(ticketService).validateTicket(ticketCode);

		assertThrows(RuntimeException.class, () -> ticketController.validateTicket(ticketCode));
		verify(ticketService).validateTicket(ticketCode);
	}

	@Test
	void validateTicket_ShouldThrowException_WhenTicketNotFound() {
		String ticketCode = "TKT-99999999-ZZZ999";

		doThrow(new TicketNotFoundException("Ticket not found")).when(ticketService).validateTicket(ticketCode);

		assertThrows(TicketNotFoundException.class, () -> ticketController.validateTicket(ticketCode));
		verify(ticketService).validateTicket(ticketCode);
	}

	@Test
	void getTicketQrCode_ShouldReturnQRCode() {
		String ticketCode = "TKT-20240115-ABC123";
		byte[] qrCodeData = new byte[] { 1, 2, 3, 4, 5 };

		when(ticketService.generateTicketQRCode(ticketCode)).thenReturn(qrCodeData);

		ResponseEntity<Resource> response = ticketController.getTicketQrCode(ticketCode);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertEquals("inline; filename=\"ticket-qr.png\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		verify(ticketService).generateTicketQRCode(ticketCode);
	}

	@Test
	void getTicketQrCode_ShouldThrowException_WhenTicketNotFound() {
		String ticketCode = "TKT-99999999-ZZZ999";

		when(ticketService.generateTicketQRCode(ticketCode)).thenThrow(new TicketNotFoundException("Ticket not found"));

		assertThrows(TicketNotFoundException.class, () -> ticketController.getTicketQrCode(ticketCode));
		verify(ticketService).generateTicketQRCode(ticketCode);
	}

	@Test
	void voidTicket_ShouldVoidSuccessfully() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 1L;

		doNothing().when(ticketService).voidTicket(ticketId, userDetails.getUser());

		ResponseEntity<Void> response = ticketController.voidTicket(ticketId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(ticketService).voidTicket(ticketId, userDetails.getUser());
	}

	@Test
	void voidTicket_ShouldThrowException_WhenCannotVoid() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 1L;

		doThrow(new RuntimeException("Ticket cannot be voided")).when(ticketService).voidTicket(ticketId,
				userDetails.getUser());

		assertThrows(RuntimeException.class, () -> ticketController.voidTicket(ticketId, userDetails));
		verify(ticketService).voidTicket(ticketId, userDetails.getUser());
	}

	@Test
	void voidTicket_ShouldThrowException_WhenNotFound() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 999L;

		doThrow(new TicketNotFoundException(ticketId)).when(ticketService).voidTicket(ticketId, userDetails.getUser());

		assertThrows(TicketNotFoundException.class, () -> ticketController.voidTicket(ticketId, userDetails));
		verify(ticketService).voidTicket(ticketId, userDetails.getUser());
	}

	@Test
	void checkTicketStatus_ShouldReturnStatus() {
		String ticketCode = "TKT-20240115-ABC123";
		TicketStatus status = TicketStatus.ACTIVE;

		when(ticketService.checkTicketStatus(ticketCode)).thenReturn(status);

		ResponseEntity<String> response = ticketController.checkTicketStatus(ticketCode);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("ACTIVE", response.getBody());
		verify(ticketService).checkTicketStatus(ticketCode);
	}

	@Test
	void checkTicketStatus_ShouldReturnNotFound() {
		String ticketCode = "TKT-99999999-ZZZ999";

		when(ticketService.checkTicketStatus(ticketCode)).thenReturn(null);

		ResponseEntity<String> response = ticketController.checkTicketStatus(ticketCode);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("NOT_FOUND", response.getBody());
		verify(ticketService).checkTicketStatus(ticketCode);
	}

	@Test
	void checkTicketStatus_ShouldThrowException_WhenTicketNotFound() {
		String ticketCode = "TKT-99999999-ZZZ999";

		when(ticketService.checkTicketStatus(ticketCode)).thenThrow(new TicketNotFoundException("Ticket not found"));

		assertThrows(TicketNotFoundException.class, () -> ticketController.checkTicketStatus(ticketCode));
		verify(ticketService).checkTicketStatus(ticketCode);
	}

	@Test
	void getUserTickets_ShouldHandleMixedStatusTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);

		TicketResponse ticket1 = createTicketResponse(1L, "TKT-20240115-ABC123", TicketStatus.ACTIVE, "Movie 1");
		TicketResponse ticket2 = createTicketResponse(2L, "TKT-20240115-DEF456", TicketStatus.USED, "Movie 2");
		TicketResponse ticket3 = createTicketResponse(3L, "TKT-20240115-GHI789", TicketStatus.CANCELLED, "Movie 3");
		List<TicketResponse> tickets = Arrays.asList(ticket1, ticket2, ticket3);

		when(ticketService.getUserTickets(userDetails.getUser(), null)).thenReturn(tickets);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(null, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(3, response.getBody().size());
		verify(ticketService).getUserTickets(userDetails.getUser(), null);
	}

	@Test
	void validateTicket_ShouldHandleAlreadyUsedTicket() {
		String ticketCode = "TKT-20240115-ABC123";

		doThrow(new RuntimeException("Ticket already used")).when(ticketService).validateTicket(ticketCode);

		assertThrows(RuntimeException.class, () -> ticketController.validateTicket(ticketCode));
		verify(ticketService).validateTicket(ticketCode);
	}

	@Test
	void getTicketQrCode_ShouldHandleQRCodeGenerationError() {
		String ticketCode = "TKT-20240115-ABC123";

		when(ticketService.generateTicketQRCode(ticketCode))
				.thenThrow(new RuntimeException("QR code generation failed"));

		assertThrows(RuntimeException.class, () -> ticketController.getTicketQrCode(ticketCode));
		verify(ticketService).generateTicketQRCode(ticketCode);
	}

	@Test
	void getBookingTickets_ShouldReturnEmptyList_WhenNoTickets() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long bookingId = 100L;

		List<TicketResponse> emptyList = Arrays.asList();

		when(ticketService.getBookingTickets(bookingId, userDetails.getUser())).thenReturn(emptyList);

		ResponseEntity<List<TicketResponse>> response = ticketController.getBookingTickets(bookingId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, response.getBody().size());
		verify(ticketService).getBookingTickets(bookingId, userDetails.getUser());
	}

	@Test
	void getUserTickets_ShouldHandleStatusNotFound() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		TicketStatus nonExistentStatus = TicketStatus.CANCELLED;

		List<TicketResponse> emptyList = Arrays.asList();

		when(ticketService.getUserTickets(userDetails.getUser(), nonExistentStatus)).thenReturn(emptyList);

		ResponseEntity<List<TicketResponse>> response = ticketController.getUserTickets(nonExistentStatus, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, response.getBody().size());
		verify(ticketService).getUserTickets(userDetails.getUser(), nonExistentStatus);
	}

	@Test
	void getTicketQrCode_ShouldHandleEmptyQRCode() {
		String ticketCode = "TKT-20240115-ABC123";
		byte[] emptyQrCodeData = new byte[0];

		when(ticketService.generateTicketQRCode(ticketCode)).thenReturn(emptyQrCodeData);

		ResponseEntity<Resource> response = ticketController.getTicketQrCode(ticketCode);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertNotNull(response.getBody());
		verify(ticketService).generateTicketQRCode(ticketCode);
	}

	@Test
	void validateTicket_ShouldHandleInvalidTicketCode() {
		String invalidTicketCode = "INVALID-CODE";

		doThrow(new IllegalArgumentException("Invalid ticket code format")).when(ticketService)
				.validateTicket(invalidTicketCode);

		assertThrows(IllegalArgumentException.class, () -> ticketController.validateTicket(invalidTicketCode));
		verify(ticketService).validateTicket(invalidTicketCode);
	}

	@Test
	void checkTicketStatus_ShouldHandleEmptyTicketCode() {
		String emptyTicketCode = "";

		when(ticketService.checkTicketStatus(emptyTicketCode))
				.thenThrow(new IllegalArgumentException("Ticket code cannot be empty"));

		assertThrows(IllegalArgumentException.class, () -> ticketController.checkTicketStatus(emptyTicketCode));
		verify(ticketService).checkTicketStatus(emptyTicketCode);
	}

	@Test
	void getTicket_ShouldHandleDifferentUserTryingToAccessTicket() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long ticketId = 1L;

		when(ticketService.getTicketById(ticketId, userDetails.getUser()))
				.thenThrow(new SecurityException("Access denied to ticket"));

		assertThrows(SecurityException.class, () -> ticketController.getTicket(ticketId, userDetails));
		verify(ticketService).getTicketById(ticketId, userDetails.getUser());
	}

	@Test
	void getBookingTickets_ShouldHandleDifferentUserTryingToAccessBooking() {
		CustomUserDetails userDetails = createUserDetails(1L, "user@example.com");
		setupSecurityContext(userDetails);
		Long bookingId = 100L;

		when(ticketService.getBookingTickets(bookingId, userDetails.getUser()))
				.thenThrow(new SecurityException("Access denied to booking"));

		assertThrows(SecurityException.class, () -> ticketController.getBookingTickets(bookingId, userDetails));
		verify(ticketService).getBookingTickets(bookingId, userDetails.getUser());
	}
}