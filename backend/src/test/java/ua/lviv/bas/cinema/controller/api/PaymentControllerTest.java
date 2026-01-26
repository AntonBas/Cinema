package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.PaymentService;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

	@Mock
	private PaymentService paymentService;

	@InjectMocks
	private PaymentController paymentController;

	private User testUser;
	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("user@example.com");
		testUser.setFirstName("John");
		testUser.setLastName("Doe");
		testUser.setPassword("password");
		testUser.setEnabled(true);
		testUser.setUserRole(UserRole.ROLE_USER);

		userDetails = new CustomUserDetails(testUser);
	}

	private PaymentResponse createPaymentResponse(Long id, Long bookingId, BigDecimal amount, PaymentStatus status,
			String liqpayOrderId) {
		return PaymentResponse.builder().id(id).bookingId(bookingId).amount(amount).status(status)
				.liqpayOrderId(liqpayOrderId).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
	}

	private PaymentLiqPayDataResponse createLiqPayDataResponse(String data, String signature) {
		return PaymentLiqPayDataResponse.builder().data(data).signature(signature)
				.paymentUrl("https://www.liqpay.ua/api/3/checkout").liqpayOrderId("ORDER_ABC123").build();
	}

	@Test
	void createPayment_ShouldCreateSuccessfully() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		PaymentResponse paymentResponse = createPaymentResponse(1L, 100L, new BigDecimal("150.00"),
				PaymentStatus.PENDING, "ORDER_ABC123");

		when(paymentService.createPayment(request, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.createPayment(request, userDetails);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getId());
		assertEquals(100L, response.getBody().getBookingId());
		assertEquals(PaymentStatus.PENDING, response.getBody().getStatus());
		verify(paymentService).createPayment(request, testUser);
	}

	@Test
	void createPayment_ShouldThrowException_WhenBookingNotFound() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(999L);

		when(paymentService.createPayment(request, testUser)).thenThrow(new BookingNotFoundException(999L));

		assertThrows(BookingNotFoundException.class, () -> paymentController.createPayment(request, userDetails));
		verify(paymentService).createPayment(request, testUser);
	}

	@Test
	void createPayment_ShouldThrowException_WhenPaymentAlreadyInProgress() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		when(paymentService.createPayment(request, testUser)).thenThrow(PaymentProcessingException.paymentInProgress());

		assertThrows(PaymentProcessingException.class, () -> paymentController.createPayment(request, userDetails));
		verify(paymentService).createPayment(request, testUser);
	}

	@Test
	void getLiqPayPaymentData_ShouldReturnData() {
		Long paymentId = 1L;

		PaymentLiqPayDataResponse liqPayData = createLiqPayDataResponse("encoded_data", "signature_hash");

		when(paymentService.preparePaymentData(paymentId, testUser)).thenReturn(liqPayData);

		ResponseEntity<PaymentLiqPayDataResponse> response = paymentController.getLiqPayPaymentData(paymentId,
				userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("encoded_data", response.getBody().getData());
		assertEquals("signature_hash", response.getBody().getSignature());
		verify(paymentService).preparePaymentData(paymentId, testUser);
	}

	@Test
	void getLiqPayPaymentData_ShouldThrowException_WhenNotFound() {
		Long paymentId = 999L;

		when(paymentService.preparePaymentData(paymentId, testUser)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class,
				() -> paymentController.getLiqPayPaymentData(paymentId, userDetails));
		verify(paymentService).preparePaymentData(paymentId, testUser);
	}

	@Test
	void getLiqPayPaymentData_ShouldThrowException_WhenAccessDenied() {
		Long paymentId = 1L;

		User otherUser = new User();
		otherUser.setId(999L);
		otherUser.setEmail("other@example.com");
		otherUser.setPassword("password");
		otherUser.setEnabled(true);
		otherUser.setUserRole(UserRole.ROLE_USER);
		CustomUserDetails otherUserDetails = new CustomUserDetails(otherUser);

		when(paymentService.preparePaymentData(paymentId, otherUser))
				.thenThrow(new PaymentAccessDeniedException(paymentId, 999L));

		assertThrows(PaymentAccessDeniedException.class,
				() -> paymentController.getLiqPayPaymentData(paymentId, otherUserDetails));
		verify(paymentService).preparePaymentData(paymentId, otherUser);
	}

	@Test
	void getPaymentById_ShouldReturnPayment() {
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, "ORDER_ABC123");

		when(paymentService.getPaymentStatus(paymentId, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().getId());
		assertEquals(PaymentStatus.SUCCESS, response.getBody().getStatus());
		verify(paymentService).getPaymentStatus(paymentId, testUser);
	}

	@Test
	void getPaymentById_ShouldThrowException_WhenNotFound() {
		Long paymentId = 999L;

		when(paymentService.getPaymentStatus(paymentId, testUser)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.getPaymentById(paymentId, userDetails));
		verify(paymentService).getPaymentStatus(paymentId, testUser);
	}

	@Test
	void getPaymentStatus_ShouldReturnStatus() {
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, "ORDER_ABC123");

		when(paymentService.getPaymentStatus(paymentId, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentStatus(paymentId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().getId());
		assertEquals(PaymentStatus.SUCCESS, response.getBody().getStatus());
		verify(paymentService).getPaymentStatus(paymentId, testUser);
	}

	@Test
	void getPaymentStatus_ShouldThrowException_WhenNotFound() {
		Long paymentId = 999L;

		when(paymentService.getPaymentStatus(paymentId, testUser)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.getPaymentStatus(paymentId, userDetails));
		verify(paymentService).getPaymentStatus(paymentId, testUser);
	}

	@Test
	void retryPayment_ShouldRetrySuccessfully() {
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.PENDING, "ORDER_NEW123");

		when(paymentService.retryPayment(paymentId, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.retryPayment(paymentId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().getId());
		assertEquals(PaymentStatus.PENDING, response.getBody().getStatus());
		verify(paymentService).retryPayment(paymentId, testUser);
	}

	@Test
	void retryPayment_ShouldThrowException_WhenNotFound() {
		Long paymentId = 999L;

		when(paymentService.retryPayment(paymentId, testUser)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.retryPayment(paymentId, userDetails));
		verify(paymentService).retryPayment(paymentId, testUser);
	}

	@Test
	void retryPayment_ShouldThrowException_WhenCannotRetry() {
		Long paymentId = 1L;

		when(paymentService.retryPayment(paymentId, testUser))
				.thenThrow(new PaymentProcessingException("Payment cannot be retried"));

		assertThrows(PaymentProcessingException.class, () -> paymentController.retryPayment(paymentId, userDetails));
		verify(paymentService).retryPayment(paymentId, testUser);
	}

	@Test
	void getPaymentByBookingId_ShouldReturnPayment() {
		Long bookingId = 100L;

		PaymentResponse paymentResponse = createPaymentResponse(1L, bookingId, new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, "ORDER_ABC123");

		when(paymentService.getUserPaymentByBookingId(bookingId, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentByBookingId(bookingId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(bookingId, response.getBody().getBookingId());
		assertEquals(PaymentStatus.SUCCESS, response.getBody().getStatus());
		verify(paymentService).getUserPaymentByBookingId(bookingId, testUser);
	}

	@Test
	void getPaymentByBookingId_ShouldThrowException_WhenNotFound() {
		Long bookingId = 999L;

		when(paymentService.getUserPaymentByBookingId(bookingId, testUser))
				.thenThrow(new PaymentNotFoundException("Payment for booking 999 not found"));

		assertThrows(PaymentNotFoundException.class,
				() -> paymentController.getPaymentByBookingId(bookingId, userDetails));
		verify(paymentService).getUserPaymentByBookingId(bookingId, testUser);
	}

	@Test
	void createPayment_ShouldHandleSessionTooCloseException() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		when(paymentService.createPayment(request, testUser)).thenThrow(new RuntimeException("Session is too close"));

		assertThrows(RuntimeException.class, () -> paymentController.createPayment(request, userDetails));
		verify(paymentService).createPayment(request, testUser);
	}

	@Test
	void getLiqPayPaymentData_ShouldThrowException_WhenPaymentNotPending() {
		Long paymentId = 1L;

		when(paymentService.preparePaymentData(paymentId, testUser))
				.thenThrow(new RuntimeException("Payment is not pending"));

		assertThrows(RuntimeException.class, () -> paymentController.getLiqPayPaymentData(paymentId, userDetails));
		verify(paymentService).preparePaymentData(paymentId, testUser);
	}

	@Test
	void createPayment_ShouldHandleSeatsNoLongerAvailable() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		when(paymentService.createPayment(request, testUser))
				.thenThrow(PaymentProcessingException.seatsNoLongerAvailable());

		assertThrows(PaymentProcessingException.class, () -> paymentController.createPayment(request, userDetails));
		verify(paymentService).createPayment(request, testUser);
	}
}