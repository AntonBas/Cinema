package ua.lviv.bas.cinema.payment.service;

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
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.dto.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentGatewayUnavailableException;
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

    private final RestTemplate restTemplate;

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
            var paymentParams = buildPaymentParams(payment);
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
        if (sandboxMode) {
            processSandboxRefund(refundData);
            return;
        }

        var response = sendApiRequest(refundData);
        var responseBody = extractResponseBody(response);
        var responseMap = decodeRefundResponse(responseBody);
        checkRefundResult(responseMap);
    }

    public RefundGatewayStatus checkRefundStatus(String orderId) {
        if (sandboxMode) {
            log.debug("Sandbox mode - treating refund for order {} as confirmed by gateway", orderId);
            return RefundGatewayStatus.CONFIRMED;
        }
        try {
            var status = (String) fetchOrderStatus(orderId).get("status");

            if ("reversed".equals(status)) {
                return RefundGatewayStatus.CONFIRMED;
            }
            if ("failure".equals(status) || "error".equals(status)) {
                return RefundGatewayStatus.NOT_CONFIRMED;
            }
            log.warn("Ambiguous LiqPay status '{}' for order {} while reconciling a stuck refund", status, orderId);
            return RefundGatewayStatus.UNKNOWN;
        } catch (Exception e) {
            log.warn("Failed to check LiqPay refund status for order {}: {}", orderId, e.getMessage());
            return RefundGatewayStatus.UNKNOWN;
        }
    }

    public PaymentGatewayCheckResult checkPaymentStatus(String orderId) {
        if (sandboxMode) {
            log.debug("Sandbox mode - payment status for order {} cannot be reconciled automatically", orderId);
            return new PaymentGatewayCheckResult(PaymentGatewayStatus.UNKNOWN, Map.of());
        }
        try {
            var responseMap = fetchOrderStatus(orderId);
            var status = (String) responseMap.get("status");
            return new PaymentGatewayCheckResult(mapPaymentStatus(status), toStringMap(responseMap));
        } catch (Exception e) {
            log.warn("Failed to check LiqPay payment status for order {}: {}", orderId, e.getMessage());
            return new PaymentGatewayCheckResult(PaymentGatewayStatus.UNKNOWN, Map.of());
        }
    }

    private PaymentGatewayStatus mapPaymentStatus(String status) {
        if (status == null) {
            return PaymentGatewayStatus.UNKNOWN;
        }
        return switch (status) {
            case "success", "sandbox" -> PaymentGatewayStatus.SUCCESS;
            case "failure", "error" -> PaymentGatewayStatus.FAILED;
            case "wait_secure", "wait_accept", "processing", "wait_reserve" -> PaymentGatewayStatus.STILL_PROCESSING;
            default -> PaymentGatewayStatus.UNKNOWN;
        };
    }

    private Map<String, String> toStringMap(Map<String, Object> raw) {
        Map<String, String> result = new LinkedHashMap<>();
        raw.forEach((key, value) -> result.put(key, value != null ? value.toString() : null));
        return result;
    }

    private Map<String, Object> fetchOrderStatus(String orderId) {
        Map<String, Object> statusParams = new LinkedHashMap<>();
        statusParams.put("public_key", liqpayPublicKey);
        statusParams.put("version", "3");
        statusParams.put("action", "status");
        statusParams.put("order_id", orderId);

        var data = LiqPayDecoder.encodeToBase64(statusParams);
        var response = sendApiRequest(data);
        var responseBody = extractResponseBody(response);
        return decodeRefundResponse(responseBody);
    }

    private ResponseEntity<String> sendApiRequest(String data) {
        try {
            var signature = LiqPayDecoder.generateSignature(data, liqpayPrivateKey);
            var requestBody = "data=" + URLEncoder.encode(data, StandardCharsets.UTF_8) + "&signature="
                    + URLEncoder.encode(signature, StandardCharsets.UTF_8);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            return restTemplate.postForEntity(liqpayApiUrl + "request", request, String.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException("Network error during LiqPay API request: " + e.getMessage(),
                    e);
        }
    }

    private String extractResponseBody(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new PaymentGatewayUnavailableException(
                    "LiqPay API request failed with status " + response.getStatusCode(), null);
        }

        var responseBody = response.getBody();
        if (responseBody == null) {
            throw new PaymentGatewayUnavailableException("Empty response from LiqPay API", null);
        }

        return responseBody;
    }

    private Map<String, Object> decodeRefundResponse(String responseBody) {
        try {
            return LiqPayDecoder.decodeToMap(responseBody);
        } catch (Exception e) {
            throw new PaymentGatewayUnavailableException("Failed to decode LiqPay response: " + e.getMessage(), e);
        }
    }

    private void checkRefundResult(Map<String, Object> responseMap) {
        var result = (String) responseMap.get("result");
        var status = (String) responseMap.get("status");
        var isSuccess = "ok".equals(result) || "success".equals(status);

        if (!isSuccess) {
            var errorCode = (String) responseMap.get("err_code");
            var errorDescription = (String) responseMap.get("err_description");
            throw new PaymentProcessingException(
                    String.format("LiqPay refund failed: %s - %s - %s", result, errorCode, errorDescription));
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