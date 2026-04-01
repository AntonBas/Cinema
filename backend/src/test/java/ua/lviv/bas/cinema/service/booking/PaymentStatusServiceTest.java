package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
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
	private PaymentService paymentService;

	@Mock
	private PaymentGatewayService paymentGatewayService;

	@InjectMocks
	private PaymentStatusService paymentStatusService;

	private Payment testPayment;
	private Booking testBooking;
	private User testUser;

	private static final Long PAYMENT_ID = 1L;
	private static final String ORDER_ID = "ORD_TEST123456789";

	@BeforeEach
	void setUp() {
		testUser = User.builder().id(1L).email("test@example.com").build();

		Movie movie = Movie.builder().id(1L).title("Test Movie").build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();

		Session session = Session.builder().id(1L).movie(movie).hall(hall).build();

		testBooking = Booking.builder().id(1L).user(testUser).session(session).build();

		testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).liqpayOrderId(ORDER_ID)
				.status(PaymentStatus.PENDING).build();
	}

	@Test
	void preparePaymentData_Success() {
		PaymentLiqPayDataResponse expectedResponse = new PaymentLiqPayDataResponse("test_data", "test_signature",
				"https://payment.url", ORDER_ID);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(paymentGatewayService.prepareLiqPayPaymentData(testPayment)).thenReturn(expectedResponse);

		PaymentLiqPayDataResponse response = paymentStatusService.preparePaymentData(PAYMENT_ID);

		assertThat(response).isNotNull();
		assertThat(response.data()).isEqualTo("test_data");
		assertThat(response.signature()).isEqualTo("test_signature");
		assertThat(response.paymentUrl()).isEqualTo("https://payment.url");
		assertThat(response.liqpayOrderId()).isEqualTo(ORDER_ID);
	}

	@Test
	void preparePaymentData_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentStatusService.preparePaymentData(PAYMENT_ID))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void handleLiqPayCallback_Success() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "success");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentService).processSuccessfulPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithSandboxStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "sandbox");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentService).processSuccessfulPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithFailedStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "failure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentService).processFailedPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithErrorStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "error");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		verify(paymentService).processFailedPayment(testPayment, decodedData);
	}

	@Test
	void handleLiqPayCallback_WithProcessingStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "wait_secure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
		verify(paymentRepository).save(testPayment);
		verify(paymentService, never()).processSuccessfulPayment(any(), any());
		verify(paymentService, never()).processFailedPayment(any(), any());
	}

	@Test
	void handleLiqPayCallback_WithUnknownStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "unknown");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(data, signature);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(testPayment);
		verify(paymentService, never()).processSuccessfulPayment(any(), any());
		verify(paymentService, never()).processFailedPayment(any(), any());
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
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "success");

		when(paymentGatewayService.processCallback("encoded_data", "test_signature")).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleLiqPayCallback(callbackRequest);

		verify(paymentService).processSuccessfulPayment(testPayment, decodedData);
	}
}