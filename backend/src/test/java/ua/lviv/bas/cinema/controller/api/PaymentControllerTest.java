package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.service.booking.PaymentService;
import ua.lviv.bas.cinema.service.booking.PaymentStatusService;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

	@Mock
	private PaymentService paymentService;

	@Mock
	private PaymentStatusService paymentStatusService;

	@InjectMocks
	private PaymentController paymentController;

	private User testUser;
	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("user@example.com");

		userDetails = new CustomUserDetails(testUser);
	}

	private PaymentResponse createPaymentResponse() {
		return new PaymentResponse("BK-123456", "Test Movie", LocalDateTime.now(), "Hall A", new BigDecimal("150.00"),
				PaymentStatus.PENDING, null, null, null);
	}

	private PaymentResponse createPaymentResponseWithSuccess() {
		return new PaymentResponse("BK-123456", "Test Movie", LocalDateTime.now(), "Hall A", new BigDecimal("150.00"),
				PaymentStatus.SUCCESS, LocalDateTime.now(), "****1234", null);
	}

	private PaymentLiqPayDataResponse createLiqPayDataResponse() {
		return new PaymentLiqPayDataResponse("encoded_data", "signature_hash", "https://www.liqpay.ua/api/3/checkout",
				"ORDER_ABC123");
	}

	@Test
	void createPayment_ShouldCreateSuccessfully() {
		PaymentCreateRequest request = new PaymentCreateRequest(100L);

		PaymentResponse paymentResponse = createPaymentResponse();

		when(paymentService.createPayment(any(PaymentCreateRequest.class), eq(testUser))).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.createPayment(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().bookingNumber()).isEqualTo("BK-123456");
		assertThat(response.getBody().movieTitle()).isEqualTo("Test Movie");
		assertThat(response.getBody().hallName()).isEqualTo("Hall A");
		assertThat(response.getBody().status()).isEqualTo(PaymentStatus.PENDING);
	}

	@Test
	void getLiqPayPaymentData_ShouldReturnData() {
		Long paymentId = 1L;

		PaymentLiqPayDataResponse liqPayData = createLiqPayDataResponse();

		when(paymentStatusService.preparePaymentData(paymentId)).thenReturn(liqPayData);

		ResponseEntity<PaymentLiqPayDataResponse> response = paymentController.getLiqPayPaymentData(paymentId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().data()).isEqualTo("encoded_data");
		assertThat(response.getBody().liqpayOrderId()).isEqualTo("ORDER_ABC123");
	}

	@Test
	void getPaymentById_ShouldReturnPayment() {
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponseWithSuccess();

		when(paymentService.getPaymentStatus(eq(paymentId), eq(testUser))).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().bookingNumber()).isEqualTo("BK-123456");
		assertThat(response.getBody().movieTitle()).isEqualTo("Test Movie");
		assertThat(response.getBody().hallName()).isEqualTo("Hall A");
		assertThat(response.getBody().status()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(response.getBody().senderCardMask()).isEqualTo("****1234");
	}
}