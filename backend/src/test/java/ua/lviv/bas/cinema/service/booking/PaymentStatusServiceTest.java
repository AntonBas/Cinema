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
	void preparePaymentDataShouldSucceed() {
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
	void preparePaymentDataWhenPaymentNotFoundShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentStatusService.preparePaymentData(PAYMENT_ID))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void handleCallbackWithSuccessStatusShouldSucceed() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "success");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		verify(paymentService).processSuccess(testPayment, decodedData);
	}

	@Test
	void handleCallbackWithSandboxStatusShouldSucceed() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "sandbox");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		verify(paymentService).processSuccess(testPayment, decodedData);
	}

	@Test
	void handleCallbackWithFailureStatusShouldSucceed() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "failure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		verify(paymentService).processFailure(testPayment, decodedData);
	}

	@Test
	void handleCallbackWithErrorStatusShouldSucceed() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "error");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		verify(paymentService).processFailure(testPayment, decodedData);
	}

	@Test
	void handleCallbackWithProcessingStatusShouldUpdateStatus() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "wait_secure");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
		verify(paymentRepository).save(testPayment);
		verify(paymentService, never()).processSuccess(any(), any());
		verify(paymentService, never()).processFailure(any(), any());
	}

	@Test
	void handleCallbackWithUnknownStatusShouldMarkAsFailed() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", ORDER_ID);
		decodedData.put("status", "unknown");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

		paymentStatusService.handleCallback(data, signature);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(testPayment);
		verify(paymentService, never()).processSuccess(any(), any());
		verify(paymentService, never()).processFailure(any(), any());
	}

	@Test
	void handleCallbackWhenPaymentNotFoundShouldThrowException() {
		String data = "encoded_data";
		String signature = "test_signature";
		Map<String, String> decodedData = new HashMap<>();
		decodedData.put("order_id", "ORD_NONEXISTENT");

		when(paymentGatewayService.processCallback(data, signature)).thenReturn(decodedData);
		when(paymentRepository.findByLiqpayOrderId("ORD_NONEXISTENT")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentStatusService.handleCallback(data, signature))
				.isInstanceOf(PaymentNotFoundException.class);
	}
}