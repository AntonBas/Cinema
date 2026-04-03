package ua.lviv.bas.cinema.service.integration.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private PaymentGatewayService paymentGatewayService;

	private Payment payment;
	private Booking booking;
	private Session session;
	private Movie movie;
	private CinemaHall hall;
	private User user;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(paymentGatewayService, "liqpayPublicKey", "test_public_key");
		ReflectionTestUtils.setField(paymentGatewayService, "liqpayPrivateKey", "test_private_key");
		ReflectionTestUtils.setField(paymentGatewayService, "liqpayCallbackUrl", "https://example.com/callback");
		ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", true);
		ReflectionTestUtils.setField(paymentGatewayService, "frontendUrl", "https://example.com");
		ReflectionTestUtils.setField(paymentGatewayService, "liqpayApiUrl", "https://www.liqpay.ua/api/");

		user = User.builder().id(1L).email("test@example.com").build();
		movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();
		hall = CinemaHall.builder().id(1L).name("Hall A").build();
		session = Session.builder().id(1L).movie(movie).hall(hall).startTime(LocalDateTime.now()).build();
		booking = Booking.builder().id(1L).user(user).session(session).build();
		payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("100.00")).liqpayOrderId("ORDER_123")
				.status(PaymentStatus.PENDING).build();
	}

	@Test
	void prepareLiqPayPaymentData_Success() {
		PaymentLiqPayDataResponse response = paymentGatewayService.prepareLiqPayPaymentData(payment);

		assertThat(response).isNotNull();
		assertThat(response.data()).isNotBlank();
		assertThat(response.signature()).isNotBlank();
		assertThat(response.paymentUrl()).isNotBlank();
		assertThat(response.liqpayOrderId()).isEqualTo("ORDER_123");
	}

	@Test
	void verifyCallbackSignature_ValidSignature_ReturnsTrue() {
		String data = "test_data";
		String signature = generateSignature(data);

		boolean result = paymentGatewayService.verifyCallbackSignature(data, signature);

		assertThat(result).isTrue();
	}

	@Test
	void verifyCallbackSignature_InvalidSignature_ReturnsFalse() {
		boolean result = paymentGatewayService.verifyCallbackSignature("test_data", "invalid_signature");

		assertThat(result).isFalse();
	}

	@Test
	void processCallback_ValidSignature_ReturnsData() {
		String data = "eyJzdGF0dXMiOiJzdWNjZXNzIn0=";
		String signature = generateSignature(data);

		Map<String, String> result = paymentGatewayService.processCallback(data, signature);

		assertThat(result).isNotNull();
	}

	@Test
	void processCallback_InvalidSignature_ThrowsException() {
		assertThatThrownBy(() -> paymentGatewayService.processCallback("test_data", "invalid_signature"))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Invalid LiqPay signature");
	}

	@Test
	void prepareRefundData_Success() {
		String result = paymentGatewayService.prepareRefundData("payment_123", "order_123", new BigDecimal("50.00"),
				"Test refund");

		assertThat(result).isNotBlank();
	}

	@Test
	void getPaymentStatus_SandboxMode_Success() {
		PaymentResponse response = paymentGatewayService.getPaymentStatus("payment_123", "BK-12345", "Test Movie",
				LocalDateTime.now().toString(), "Hall A", new BigDecimal("100.00"), LocalDateTime.now().toString(),
				"****1234");

		assertThat(response).isNotNull();
		assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(response.bookingNumber()).isEqualTo("BK-12345");
		assertThat(response.movieTitle()).isEqualTo("Test Movie");
		assertThat(response.hallName()).isEqualTo("Hall A");
	}

	@Test
	void convertLiqPayStatus_ReturnsCorrectStatus() {
		PaymentStatus status = getPaymentStatusFromLiqPay("success");
		assertThat(status).isEqualTo(PaymentStatus.SUCCESS);
	}

	private String generateSignature(String data) {
		try {
			ReflectionTestUtils.setField(paymentGatewayService, "liqpayPrivateKey", "test_private_key");
			java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
			String str = "test_private_key" + data + "test_private_key";
			byte[] digest = sha1.digest(str.getBytes());
			return java.util.Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private PaymentStatus getPaymentStatusFromLiqPay(String liqpayStatus) {
		try {
			var method = PaymentGatewayService.class.getDeclaredMethod("convertLiqPayStatus", String.class);
			method.setAccessible(true);
			return (PaymentStatus) method.invoke(paymentGatewayService, liqpayStatus);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}