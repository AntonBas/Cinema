package ua.lviv.bas.cinema.service.integration.payment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private static final Type MAP_STRING_OBJECT_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();
    private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentLiqPayDataResponse prepareLiqPayPaymentData(Payment payment) {
        try {
            var params = buildPaymentParams(payment);
            var jsonData = gson.toJson(params);
            var data = Base64.getEncoder().encodeToString(jsonData.getBytes());
            var signature = generateSignature(data);
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

            var jsonData = gson.toJson(paymentParams);
            var data = Base64.getEncoder().encodeToString(jsonData.getBytes());
            var signature = generateSignature(data);

            return liqpayApiUrl + "3/checkout?data=" + URLEncoder.encode(data, StandardCharsets.UTF_8) + "&signature="
                    + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to create payment URL");
        }
    }

    public boolean verifyCallbackSignature(String data, String receivedSignature) {
        var calculatedSignature = generateSignature(data);
        var isValid = calculatedSignature.equals(receivedSignature);
        if (!isValid) {
            log.error("Invalid LiqPay signature");
        }
        return isValid;
    }

    public Map<String, String> processCallback(String data, String signature) {
        if (!verifyCallbackSignature(data, signature)) {
            throw new PaymentProcessingException("Invalid LiqPay signature");
        }
        var decodedData = new String(Base64.getDecoder().decode(data));
        return gson.fromJson(decodedData, MAP_STRING_STRING_TYPE);
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

            var jsonData = gson.toJson(refundParams);
            return Base64.getEncoder().encodeToString(jsonData.getBytes());
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

            var signature = generateSignature(refundData);
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

                Map<String, Object> responseMap = gson.fromJson(responseBody, MAP_STRING_OBJECT_TYPE);
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
            var decodedData = new String(Base64.getDecoder().decode(refundData));
            Map<String, Object> params = gson.fromJson(decodedData, MAP_STRING_OBJECT_TYPE);
            log.info("Sandbox refund: orderId={}, amount={}", params.get("order_id"), params.get("amount"));
        } catch (Exception e) {
            log.error("Sandbox refund error", e);
            throw new PaymentProcessingException("Sandbox refund failed: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentStatus(Long paymentId, String bookingNumber, String movieTitle, String sessionTime,
                                            String hallName, BigDecimal finalAmount, String paymentTime, String senderCardMask) {
        try {
            if (sandboxMode) {
                return getSandboxPaymentStatus(paymentId, bookingNumber, movieTitle, sessionTime, hallName, finalAmount,
                        paymentTime, senderCardMask);
            }

            Map<String, Object> statusParams = new LinkedHashMap<>();
            statusParams.put("public_key", liqpayPublicKey);
            statusParams.put("version", "3");
            statusParams.put("action", "status");
            statusParams.put("payment_id", paymentId);

            var jsonData = gson.toJson(statusParams);
            var data = Base64.getEncoder().encodeToString(jsonData.getBytes());
            var signature = generateSignature(data);

            var requestBody = "data=" + URLEncoder.encode(data, StandardCharsets.UTF_8) + "&signature="
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
                    return new PaymentResponse(paymentId, bookingNumber, movieTitle, null, hallName, finalAmount,
                            PaymentStatus.FAILED, null, senderCardMask, "Empty response from payment gateway");
                }

                Map<String, Object> responseMap = gson.fromJson(responseBody, MAP_STRING_OBJECT_TYPE);
                var result = (String) responseMap.get("result");

                if ("error".equals(result)) {
                    var errorDescription = (String) responseMap.get("err_description");
                    return new PaymentResponse(paymentId, bookingNumber, movieTitle, null, hallName, finalAmount,
                            PaymentStatus.FAILED, null, senderCardMask,
                            errorDescription != null ? errorDescription : "Unknown error");
                }

                var status = (String) responseMap.get("status");
                var paymentStatus = convertLiqPayStatus(status);
                var maskedCard = (String) responseMap.get("sender_card_mask2");
                var paymentTimeStr = (String) responseMap.get("payment_time");

                LocalDateTime sessionTimeParsed = Optional.ofNullable(sessionTime)
                        .map(s -> {
                            try {
                                return LocalDateTime.parse(s);
                            } catch (Exception ignore) {
                                return null;
                            }
                        })
                        .orElse(null);

                LocalDateTime paymentTimeParsed = Optional.ofNullable(paymentTimeStr)
                        .map(s -> {
                            try {
                                return LocalDateTime.parse(s);
                            } catch (Exception ignore) {
                                return null;
                            }
                        })
                        .orElse(null);

                var finalMaskedCard = maskedCard != null ? maskedCard : senderCardMask;

                return new PaymentResponse(paymentId, bookingNumber, movieTitle, sessionTimeParsed, hallName, finalAmount,
                        paymentStatus, paymentTimeParsed, finalMaskedCard, null);
            } else {
                return new PaymentResponse(paymentId, bookingNumber, movieTitle, null, hallName, finalAmount,
                        PaymentStatus.FAILED, null, senderCardMask, "Failed to get payment status");
            }

        } catch (RestClientException e) {
            throw new PaymentProcessingException("Network error during status check");
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to get payment status");
        }
    }

    private PaymentResponse getSandboxPaymentStatus(Long paymentId, String bookingNumber, String movieTitle,
                                                    String sessionTime, String hallName, BigDecimal finalAmount, String paymentTime, String senderCardMask) {
        log.info("Sandbox payment status");

        LocalDateTime sessionTimeParsed = Optional.ofNullable(sessionTime)
                .map(s -> {
                    try {
                        return LocalDateTime.parse(s);
                    } catch (Exception ignore) {
                        return null;
                    }
                })
                .orElse(null);

        LocalDateTime paymentTimeParsed = Optional.ofNullable(paymentTime)
                .map(s -> {
                    try {
                        return LocalDateTime.parse(s);
                    } catch (Exception ignore) {
                        return LocalDateTime.now();
                    }
                })
                .orElse(LocalDateTime.now());

        var finalMaskedCard = senderCardMask != null ? senderCardMask : "****0000";

        return new PaymentResponse(paymentId, bookingNumber, movieTitle, sessionTimeParsed, hallName, finalAmount,
                PaymentStatus.SUCCESS, paymentTimeParsed, finalMaskedCard, null);
    }

    private PaymentStatus convertLiqPayStatus(String liqpayStatus) {
        if (liqpayStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (liqpayStatus.toLowerCase()) {
            case "success", "sandbox" -> PaymentStatus.SUCCESS;
            case "failure", "error" -> PaymentStatus.FAILED;
            case "reversed", "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
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

    private String generateSignature(String data) {
        try {
            var str = liqpayPrivateKey + data + liqpayPrivateKey;
            var sha1 = MessageDigest.getInstance("SHA-1");
            var digest = sha1.digest(str.getBytes());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to generate payment signature");
        }
    }
}