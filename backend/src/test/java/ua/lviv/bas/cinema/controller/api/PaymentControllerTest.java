package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.service.booking.PaymentService;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

	@Mock
	private PaymentService paymentService;

	@InjectMocks
	private PaymentController paymentController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setFirstName("John");
		user.setLastName("Doe");
		return user;
	}

	private PaymentResponse createPaymentResponse(Long id, Long bookingId, BigDecimal amount, PaymentStatus status,
			String liqpayOrderId) {
		PaymentResponse response = new PaymentResponse();
		response.setId(id);
		response.setBookingId(bookingId);
		response.setAmount(amount);
		response.setStatus(status);
		response.setLiqpayOrderId(liqpayOrderId);
		response.setCreatedAt(LocalDateTime.now());
		response.setUpdatedAt(LocalDateTime.now());
		return response;
	}

	private PaymentLiqPayDataResponse createLiqPayDataResponse(String data, String signature) {
		PaymentLiqPayDataResponse response = new PaymentLiqPayDataResponse();
		response.setData(data);
		response.setSignature(signature);
		return response;
	}

	@Test
	void createPayment_ShouldCreateSuccessfully() {
		User user = createUser(1L, "user@example.com");
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		PaymentResponse paymentResponse = createPaymentResponse(1L, 100L, new BigDecimal("150.00"),
				PaymentStatus.PENDING, "ORDER_ABC123");

		when(paymentService.createPayment(request, user)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.createPayment(request, user);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getId());
		assertEquals(100L, response.getBody().getBookingId());
		assertEquals(PaymentStatus.PENDING, response.getBody().getStatus());
		verify(paymentService).createPayment(request, user);
	}

	@Test
	void createPayment_ShouldThrowException_WhenInvalidRequest() {
		User user = createUser(1L, "user@example.com");
		PaymentCreateRequest request = new PaymentCreateRequest();

		when(paymentService.createPayment(request, user)).thenThrow(new IllegalArgumentException("Invalid request"));

		assertThrows(IllegalArgumentException.class, () -> paymentController.createPayment(request, user));
		verify(paymentService).createPayment(request, user);
	}

	@Test
	void createPayment_ShouldThrowException_WhenBookingNotFound() {
		User user = createUser(1L, "user@example.com");
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(999L);

		when(paymentService.createPayment(request, user)).thenThrow(new RuntimeException("Booking not found"));

		assertThrows(RuntimeException.class, () -> paymentController.createPayment(request, user));
		verify(paymentService).createPayment(request, user);
	}

	@Test
	void processLiqPayCallback_ShouldProcessSuccessfully() {
		LiqPayCallbackRequest callbackRequest = new LiqPayCallbackRequest();
		callbackRequest.setOrderId("ORDER_ABC123");
		callbackRequest.setStatus("success");

		ResponseEntity<String> response = paymentController.processLiqPayCallback(callbackRequest);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("OK", response.getBody());
		verify(paymentService).processLiqPayCallback(callbackRequest);
	}

	@Test
	void processLiqPayCallback_ShouldThrowException_WhenInvalidCallback() {
		LiqPayCallbackRequest callbackRequest = new LiqPayCallbackRequest();

		doThrow(new IllegalArgumentException("Invalid callback data")).when(paymentService)
				.processLiqPayCallback(callbackRequest);

		assertThrows(IllegalArgumentException.class, () -> paymentController.processLiqPayCallback(callbackRequest));
		verify(paymentService).processLiqPayCallback(callbackRequest);
	}

	@Test
	void getLiqPayPaymentData_ShouldReturnData() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		PaymentLiqPayDataResponse liqPayData = createLiqPayDataResponse("encoded_data", "signature_hash");

		when(paymentService.preparePaymentData(paymentId, user)).thenReturn(liqPayData);

		ResponseEntity<PaymentLiqPayDataResponse> response = paymentController.getLiqPayPaymentData(paymentId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("encoded_data", response.getBody().getData());
		assertEquals("signature_hash", response.getBody().getSignature());
		verify(paymentService).preparePaymentData(paymentId, user);
	}

	@Test
	void getLiqPayPaymentData_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 999L;

		when(paymentService.preparePaymentData(paymentId, user)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.getLiqPayPaymentData(paymentId, user));
		verify(paymentService).preparePaymentData(paymentId, user);
	}

	@Test
	void getPaymentStatus_ShouldReturnStatus() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, "ORDER_ABC123");

		when(paymentService.getPaymentStatus(paymentId, user)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentStatus(paymentId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().getId());
		assertEquals(PaymentStatus.SUCCESS, response.getBody().getStatus());
		verify(paymentService).getPaymentStatus(paymentId, user);
	}

	@Test
	void getPaymentStatus_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 999L;

		when(paymentService.getPaymentStatus(paymentId, user)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.getPaymentStatus(paymentId, user));
		verify(paymentService).getPaymentStatus(paymentId, user);
	}

	@Test
	void retryPayment_ShouldRetrySuccessfully() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.PENDING, "ORDER_NEW123");

		when(paymentService.retryPayment(paymentId, user)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.retryPayment(paymentId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().getId());
		assertEquals(PaymentStatus.PENDING, response.getBody().getStatus());
		verify(paymentService).retryPayment(paymentId, user);
	}

	@Test
	void retryPayment_ShouldThrowException_WhenCannotRetry() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		when(paymentService.retryPayment(paymentId, user)).thenThrow(new RuntimeException("Payment cannot be retried"));

		assertThrows(RuntimeException.class, () -> paymentController.retryPayment(paymentId, user));
		verify(paymentService).retryPayment(paymentId, user);
	}

	@Test
	void retryPayment_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 999L;

		when(paymentService.retryPayment(paymentId, user)).thenThrow(new PaymentNotFoundException(paymentId));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.retryPayment(paymentId, user));
		verify(paymentService).retryPayment(paymentId, user);
	}

	@Test
	void getPaymentByBookingId_ShouldReturnPayment() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 100L;

		PaymentResponse paymentResponse = createPaymentResponse(1L, bookingId, new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, "ORDER_ABC123");

		when(paymentService.getUserPaymentByBookingId(bookingId, user)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentByBookingId(bookingId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(bookingId, response.getBody().getBookingId());
		assertEquals(PaymentStatus.SUCCESS, response.getBody().getStatus());
		verify(paymentService).getUserPaymentByBookingId(bookingId, user);
	}

	@Test
	void getPaymentByBookingId_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 999L;

		when(paymentService.getUserPaymentByBookingId(bookingId, user))
				.thenThrow(new PaymentNotFoundException("Payment not found for booking"));

		assertThrows(PaymentNotFoundException.class, () -> paymentController.getPaymentByBookingId(bookingId, user));
		verify(paymentService).getUserPaymentByBookingId(bookingId, user);
	}

	@Test
	void createPayment_ShouldHandlePaymentAlreadyExists() {
		User user = createUser(1L, "user@example.com");
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		when(paymentService.createPayment(request, user)).thenThrow(new RuntimeException("Payment already exists"));

		assertThrows(RuntimeException.class, () -> paymentController.createPayment(request, user));
		verify(paymentService).createPayment(request, user);
	}

	@Test
	void getPaymentStatus_ShouldHandleDifferentStatuses() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId, 100L, new BigDecimal("150.00"),
				PaymentStatus.FAILED, "ORDER_ABC123");

		when(paymentService.getPaymentStatus(paymentId, user)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentStatus(paymentId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(PaymentStatus.FAILED, response.getBody().getStatus());
		verify(paymentService).getPaymentStatus(paymentId, user);
	}

	@Test
	void processLiqPayCallback_ShouldHandleFailedPayment() {
		LiqPayCallbackRequest callbackRequest = new LiqPayCallbackRequest();
		callbackRequest.setOrderId("ORDER_FAILED");
		callbackRequest.setStatus("failure");

		ResponseEntity<String> response = paymentController.processLiqPayCallback(callbackRequest);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("OK", response.getBody());
		verify(paymentService).processLiqPayCallback(callbackRequest);
	}

	@Test
	void getLiqPayPaymentData_ShouldThrowException_WhenPaymentNotPending() {
		User user = createUser(1L, "user@example.com");
		Long paymentId = 1L;

		when(paymentService.preparePaymentData(paymentId, user))
				.thenThrow(new RuntimeException("Payment is not pending"));

		assertThrows(RuntimeException.class, () -> paymentController.getLiqPayPaymentData(paymentId, user));
		verify(paymentService).preparePaymentData(paymentId, user);
	}

	@Test
	void createPayment_ShouldHandleConcurrentPaymentCreation() {
		User user = createUser(1L, "user@example.com");
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setBookingId(100L);

		when(paymentService.createPayment(request, user))
				.thenThrow(new RuntimeException("Payment already in progress"));

		assertThrows(RuntimeException.class, () -> paymentController.createPayment(request, user));
		verify(paymentService).createPayment(request, user);
	}
}