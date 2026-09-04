package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.payment.dto.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentGatewayUnavailableException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

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
        ReflectionTestUtils.setField(paymentGatewayService, "restTemplate", restTemplate);

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

        assertThat(result).containsExactly(Map.entry("status", "success"));
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

    @Test
    void processRefundInSandboxModeShouldNotThrow() {
        String refundData = paymentGatewayService.prepareRefundData("payment_123", "order_123",
                new BigDecimal("50.00"), "Test refund");

        assertThatCode(() -> paymentGatewayService.processRefund(refundData)).doesNotThrowAnyException();

        verify(restTemplate, never()).postForEntity(any(String.class), any(), eq(String.class));
    }

    @Test
    void processRefundWhenLiqPaySuccessShouldNotThrow() {
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", false);
        String responseBody = LiqPayDecoder.encodeToBase64(Map.of("result", "ok"));
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThatCode(() -> paymentGatewayService.processRefund("refund_data")).doesNotThrowAnyException();

        verify(restTemplate).postForEntity(any(String.class), any(), eq(String.class));
    }

    @Test
    void processRefundWhenLiqPayExplicitlyRejectsShouldThrowPaymentProcessingException() {
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", false);
        String responseBody = LiqPayDecoder
                .encodeToBase64(Map.of("result", "error", "err_code", "1", "err_description", "insufficient funds"));
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThatThrownBy(() -> paymentGatewayService.processRefund("refund_data"))
                .isInstanceOf(PaymentProcessingException.class);
    }

    @Test
    void processRefundWhenHttpStatusNot2xxShouldThrowPaymentGatewayUnavailableException() {
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", false);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        assertThatThrownBy(() -> paymentGatewayService.processRefund("refund_data"))
                .isInstanceOf(PaymentGatewayUnavailableException.class);
    }

    @Test
    void processRefundWhenResponseBodyNullShouldThrowPaymentGatewayUnavailableException() {
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", false);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertThatThrownBy(() -> paymentGatewayService.processRefund("refund_data"))
                .isInstanceOf(PaymentGatewayUnavailableException.class);
    }

    @Test
    void processRefundWhenNetworkErrorShouldThrowPaymentGatewayUnavailableException() {
        ReflectionTestUtils.setField(paymentGatewayService, "sandboxMode", false);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThatThrownBy(() -> paymentGatewayService.processRefund("refund_data"))
                .isInstanceOf(PaymentGatewayUnavailableException.class);
    }
}