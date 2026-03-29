package ua.lviv.bas.cinema.service.booking.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;

@ExtendWith(MockitoExtension.class)
public class PaymentStatusServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentProcessingService paymentProcessingService;

	@Mock
	private PaymentGatewayService paymentGatewayService;

	@InjectMocks
	private PaymentStatusService paymentStatusService;

	private Payment testPayment;
	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);

		Booking booking = new Booking();
		booking.setUser(testUser);

		testPayment = new Payment();
		testPayment.setId(1L);
		testPayment.setBooking(booking);
		testPayment.setLiqpayOrderId("ORD_TEST123456789");
		testPayment.setStatus(PaymentStatus.PENDING);
	}

	@Test
	void preparePaymentData_Success() {
		PaymentLiqPayDataResponse expectedResponse = new PaymentLiqPayDataResponse("test_data", "test_signature",
				"https://payment.url", "ORD_TEST123456789");

		when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
		when(paymentGatewayService.prepareLiqPayPaymentData(testPayment)).thenReturn(expectedResponse);

		PaymentLiqPayDataResponse response = paymentStatusService.preparePaymentData(1L);

		assertThat(response).isNotNull();
		assertThat(response.data()).isEqualTo("test_data");
		assertThat(response.signature()).isEqualTo("test_signature");
	}

	@Test
	void preparePaymentData_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentStatusService.preparePaymentData(1L))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void handleLiqPayCallback_Success() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_TEST123456789");
		decodedData.put("status", "success");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentProcessingService).processSuccessfulPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithFailedStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_TEST123456789");
		decodedData.put("status", "failure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentProcessingService).processFailedPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithProcessingStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_TEST123456789");
		decodedData.put("status", "wait_secure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentRepository).save(testPayment);
		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
	}

	@Test
	void handleLiqPayCallback_WithUnknownStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_TEST123456789");
		decodedData.put("status", "unknown");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentRepository).save(testPayment);
		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
	}

	@Test
	void handleLiqPayCallback_WhenPaymentNotFound_ShouldThrowException() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_NONEXISTENT");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_NONEXISTENT")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentStatusService.handleLiqPayCallback(data, signature))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void handleLiqPayCallback_WithRequestObject() {
		LiqPayCallbackRequest callbackRequest = new LiqPayCallbackRequest("encoded_data", "test_signature");

		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_TEST123456789");
		decodedData.put("status", "success");

		when(paymentGatewayService.processCallback("encoded_data", "test_signature")).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(callbackRequest);

		verify(paymentProcessingService).processSuccessfulPayment(testPayment, decodedData);
	}
}