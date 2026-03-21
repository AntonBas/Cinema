package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

	@Mock
	private ControllerFacade controllerFacade;

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

	private PaymentResponse createPaymentResponse(Long id) {
		return new PaymentResponse(id, 100L, "BK-123456", "user@example.com", "Test Movie", LocalDateTime.now(),
				"Hall A", new BigDecimal("150.00"), new BigDecimal("150.00"), PaymentStatus.PENDING, "ORDER_ABC123",
				null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
	}

	private PaymentLiqPayDataResponse createLiqPayDataResponse() {
		return new PaymentLiqPayDataResponse("encoded_data", "signature_hash", "https://www.liqpay.ua/api/3/checkout",
				"ORDER_ABC123");
	}

	@Test
	void createPayment_ShouldCreateSuccessfully() {
		PaymentCreateRequest request = new PaymentCreateRequest(100L);

		PaymentResponse paymentResponse = createPaymentResponse(1L);

		when(controllerFacade.createPayment(request, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.createPayment(request, userDetails);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().id());
	}

	@Test
	void getLiqPayPaymentData_ShouldReturnData() {
		Long paymentId = 1L;

		PaymentLiqPayDataResponse liqPayData = createLiqPayDataResponse();

		when(controllerFacade.preparePaymentData(paymentId)).thenReturn(liqPayData);

		ResponseEntity<PaymentLiqPayDataResponse> response = paymentController.getLiqPayPaymentData(paymentId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("encoded_data", response.getBody().data());
	}

	@Test
	void getPaymentById_ShouldReturnPayment() {
		Long paymentId = 1L;

		PaymentResponse paymentResponse = createPaymentResponse(paymentId);

		when(controllerFacade.getPaymentStatus(paymentId, testUser)).thenReturn(paymentResponse);

		ResponseEntity<PaymentResponse> response = paymentController.getPaymentById(paymentId, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(paymentId, response.getBody().id());
	}
}