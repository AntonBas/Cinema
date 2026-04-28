package ua.lviv.bas.cinema.service.integration.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    @Value("${payment.liqpay.public_key}")
    private String liqpayPublicKey;

    @Value("${payment.liqpay.private_key}")
    private String liqpayPrivateKey;

    @Value("${payment.liqpay.callback_url}")
    private String liqpayCallbackUrl;

    @Value("${payment.liqpay.sandbox_mode:true}")
    private boolean sandboxMode;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${payment.liqpay.api_url:https://www.liqpay.ua/api/}")
    private String liqpayApiUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentLiqPayDataResponse prepareLiqPayPaymentData(Payment payment) {
        try {
            var params = buildPaymentParams(payment);
            var data = LiqPayDecoder.encodeToBase64(params);
            var signature = LiqPayDecoder.generateSignature(data, liqpayPrivateKey);
            var paymentUrl = createPayment(payment);

            return new PaymentLiqPayDataResponse(data, signature, paymentUrl, payment.getLiqpayOrderId());
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to prepare payment data: " + e.getMessage());
        }
    }

    private String createPayment(Payment payment) {
        try {
            Map<String, Object> paymentParams = new LinkedHashMap<>();
            paymentParams.put("public_key", liqpayPublicKey);
            paymentParams.put("version", "3");
            paymentParams.put("action", "pay");
            paymentParams.put("amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            paymentParams.put("currency", "UAH");
            paymentParams.put("description", buildPaymentDescription(payment));
            paymentParams.put("order_id", payment.getLiqpayOrderId());
            paymentParams.put("result_url", buildResultUrl(payment));
            paymentParams.put("server_url", liqpayCallbackUrl);
            paymentParams.put("language", "uk");
            paymentParams.put("email", payment.getBooking().getUser().getEmail());

            if (sandboxMode) {
                paymentParams.put("sandbox", "1");
            }

            var data = LiqPayDecoder.encodeToBase64(paymentParams);
            var signature = LiqPayDecoder.generateSignature(data, liqpayPrivateKey);

            return liqpayApiUrl + "3/checkout?data=" + URLEncoder.encode(data, StandardCharsets.UTF_8) + "&signature="
                    + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to create payment URL");
        }
    }

    public Map<String, String> processCallback(String data, String signature) {
        var calculatedSignature = LiqPayDecoder.generateSignature(data, liqpayPrivateKey);
        if (!calculatedSignature.equals(signature)) {
            log.error("Invalid LiqPay signature");
            throw new PaymentProcessingException("Invalid LiqPay signature");
        }
        return LiqPayDecoder.decodeCallback(data);
    }

    public String prepareRefundData(String originalLiqpayPaymentId, String originalOrderId, BigDecimal amount,
                                    String description) {
        try {
            Map<String, Object> refundParams = new LinkedHashMap<>();
            refundParams.put("public_key", liqpayPublicKey);
            refundParams.put("version", "3");
            refundParams.put("action", "refund");
            refundParams.put("amount", amount.setScale(2, RoundingMode.HALF_UP).toString());
            refundParams.put("currency", "UAH");
            refundParams.put("description", description);
            refundParams.put("order_id", originalOrderId);

            if (originalLiqpayPaymentId != null && !originalLiqpayPaymentId.trim().isEmpty()) {
                refundParams.put("payment_id", originalLiqpayPaymentId);
            }

            if (sandboxMode) {
                refundParams.put("sandbox", "1");
            }

            return LiqPayDecoder.encodeToBase64(refundParams);
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to prepare refund data");
        }
    }

    public void processRefund(String refundData) {
        try {
            if (sandboxMode) {
                processSandboxRefund(refundData);
                return;
            }

            var signature = LiqPayDecoder.generateSignature(refundData, liqpayPrivateKey);
            var requestBody = "data=" + URLEncoder.encode(refundData, StandardCharsets.UTF_8) + "&signature="
                    + URLEncoder.encode(signature, StandardCharsets.UTF_8);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                var responseBody = response.getBody();
                if (responseBody == null) {
                    throw new PaymentProcessingException("Empty response from LiqPay API");
                }

                Map<String, Object> responseMap = LiqPayDecoder.decodeToMap(responseBody);
                var result = (String) responseMap.get("result");
                var status = (String) responseMap.get("status");

                var isSuccess = "ok".equals(result) || "success".equals(status);

                if (!isSuccess) {
                    var errorCode = (String) responseMap.get("err_code");
                    var errorDescription = (String) responseMap.get("err_description");
                    throw new PaymentProcessingException(
                            String.format("LiqPay refund failed: %s - %s - %s", result, errorCode, errorDescription));
                }
            } else {
                throw new PaymentProcessingException("LiqPay API request failed");
            }

        } catch (RestClientException e) {
            throw new PaymentProcessingException("Network error during refund: " + e.getMessage());
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
        }
    }

    private void processSandboxRefund(String refundData) {
        try {
            Map<String, Object> params = LiqPayDecoder.decodeToMap(refundData);
            log.info("Sandbox refund: orderId={}, amount={}", params.get("order_id"), params.get("amount"));
        } catch (Exception e) {
            log.error("Sandbox refund error", e);
            throw new PaymentProcessingException("Sandbox refund failed: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPaymentParams(Payment payment) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("public_key", liqpayPublicKey);
        params.put("version", "3");
        params.put("action", "pay");
        params.put("amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        params.put("currency", "UAH");
        params.put("description", buildPaymentDescription(payment));
        params.put("order_id", payment.getLiqpayOrderId());
        params.put("result_url", buildResultUrl(payment));
        params.put("server_url", liqpayCallbackUrl);
        params.put("language", "uk");
        params.put("email", payment.getBooking().getUser().getEmail());

        if (sandboxMode) {
            params.put("sandbox", "1");
        }

        return params;
    }

    private String buildPaymentDescription(Payment payment) {
        return String.format("Tickets for %s, hall %s, %s", payment.getBooking().getSession().getMovie().getTitle(),
                payment.getBooking().getSession().getHall().getName(),
                payment.getBooking().getSession().getStartTime().format(DATE_FORMATTER));
    }

    private String buildResultUrl(Payment payment) {
        return frontendUrl + "/booking/success?bookingId=" + payment.getBooking().getId() + "&paymentId="
                + payment.getId();
    }
}