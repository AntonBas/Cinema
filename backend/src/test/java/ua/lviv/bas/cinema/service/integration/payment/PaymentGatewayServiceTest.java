package ua.lviv.bas.cinema.service.integration.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @InjectMocks
    private PaymentGatewayService paymentGatewayService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentGatewayService, "liqpayPublicKey", "test_public_key");
        ReflectionTestUtils.setField(paymentGatewayService, "liqpayPrivateKey", "test_private_key");
        ReflectionTestUtils.setField(paymentGatewayService, "liqpayCallbackUrl", "https://example.com/callback");
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", true);
        ReflectionTestUtils.setField(paymentGatewayService, "frontendUrl", "https://example.com");
        ReflectionTestUtils.setField(paymentGatewayService, "liqpayApiUrl", "https://www.liqpay.ua/api/");

        User user = User.builder().id(1L).email("test@example.com").build();
        Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();
        Session session = Session.builder().id(1L).movie(movie).hall(hall).startTime(LocalDateTime.now()).build();
        Booking booking = Booking.builder().id(1L).user(user).session(session).build();
        payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("100.00")).liqpayOrderId("ORDER_123")
                .status(PaymentStatus.PENDING).build();
    }

    @Test
    void prepareLiqPayPaymentDataShouldReturnValidResponse() {
        PaymentLiqPayDataResponse response = paymentGatewayService.prepareLiqPayPaymentData(payment);

        assertThat(response).isNotNull();
        assertThat(response.data()).isNotBlank();
        assertThat(response.signature()).isNotBlank();
        assertThat(response.paymentUrl()).isNotBlank();
        assertThat(response.liqpayOrderId()).isEqualTo("ORDER_123");
    }

    @Test
    void processCallbackWithValidSignatureShouldReturnData() {
        String data = "eyJzdGF0dXMiOiJzdWNjZXNzIn0=";
        String signature = LiqPayDecoder.generateSignature(data, "test_private_key");

        Map<String, String> result = paymentGatewayService.processCallback(data, signature);

        assertThat(result).isNotNull();
    }

    @Test
    void processCallbackWithInvalidSignatureShouldThrowException() {
        assertThatThrownBy(() -> paymentGatewayService.processCallback("test_data", "invalid_signature"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void prepareRefundDataShouldReturnValidBase64() {
        String result = paymentGatewayService.prepareRefundData("payment_123", "order_123", new BigDecimal("50.00"),
                "Test refund");

        assertThat(result).isNotBlank();
    }
}