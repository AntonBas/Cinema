package ua.lviv.bas.cinema.service.integration.payment;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;

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
			Map<String, Object> params = buildPaymentParams(payment);
			String jsonData = gson.toJson(params);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());
			String signature = generateSignature(data);
			String paymentUrl = createPayment(payment);

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

			String jsonData = gson.toJson(paymentParams);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());
			String signature = generateSignature(data);

			return liqpayApiUrl + "3/checkout?data=" + URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
					+ "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.toString());
		} catch (Exception e) {
			throw new PaymentProcessingException("Failed to create payment URL");
		}
	}

	public boolean verifyCallbackSignature(String data, String receivedSignature) {
		String calculatedSignature = generateSignature(data);
		boolean isValid = calculatedSignature.equals(receivedSignature);
		if (!isValid) {
			log.error("Invalid LiqPay signature");
		}
		return isValid;
	}

	public Map<String, String> processCallback(String data, String signature) {
		if (!verifyCallbackSignature(data, signature)) {
			throw new PaymentProcessingException("Invalid LiqPay signature");
		}
		String decodedData = new String(Base64.getDecoder().decode(data));
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

			String jsonData = gson.toJson(refundParams);
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

			String signature = generateSignature(refundData);
			String requestBody = "data=" + URLEncoder.encode(refundData, StandardCharsets.UTF_8.toString())
					+ "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.toString());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);
				String result = (String) responseMap.get("result");
				String status = (String) responseMap.get("status");

				boolean isSuccess = "ok".equals(result) || "success".equals(status);

				if (!isSuccess) {
					String errorCode = (String) responseMap.get("err_code");
					String errorDescription = (String) responseMap.get("err_description");
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
			String decodedData = new String(Base64.getDecoder().decode(refundData));
			Map<String, Object> params = gson.fromJson(decodedData, MAP_STRING_OBJECT_TYPE);
			log.info("Sandbox refund: orderId={}, amount={}", params.get("order_id"), params.get("amount"));
		} catch (Exception e) {
			log.error("Sandbox refund error", e);
			throw new PaymentProcessingException("Sandbox refund failed: " + e.getMessage());
		}
	}

	public PaymentResponse getPaymentStatus(String paymentId) {
		try {
			if (sandboxMode) {
				return getSandboxPaymentStatus(paymentId);
			}

			Map<String, Object> statusParams = new LinkedHashMap<>();
			statusParams.put("public_key", liqpayPublicKey);
			statusParams.put("version", "3");
			statusParams.put("action", "status");
			statusParams.put("payment_id", paymentId);

			String jsonData = gson.toJson(statusParams);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());
			String signature = generateSignature(data);

			String requestBody = "data=" + URLEncoder.encode(data, StandardCharsets.UTF_8.toString()) + "&signature="
					+ URLEncoder.encode(signature, StandardCharsets.UTF_8.toString());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);
				String result = (String) responseMap.get("result");
				String status = (String) responseMap.get("status");

				if ("error".equals(result)) {
					String errorCode = (String) responseMap.get("err_code");
					String errorDescription = (String) responseMap.get("err_description");

					return new PaymentResponse(null, null, null, null, null, null, null, null, null,
							PaymentStatus.FAILED, null, null, null, errorCode, errorDescription, null, null, null, null,
							null);
				}

				boolean isRefundableViaApi = "success".equals(status);

				Object paymentIdObj = responseMap.get("payment_id");
				String liqpayPaymentId = paymentIdObj != null ? paymentIdObj.toString() : paymentId;

				Object amountObj = responseMap.get("amount");
				BigDecimal amount = BigDecimal.ZERO;
				if (amountObj != null) {
					if (amountObj instanceof Double) {
						amount = BigDecimal.valueOf((Double) amountObj);
					} else if (amountObj instanceof Integer) {
						amount = BigDecimal.valueOf((Integer) amountObj);
					} else if (amountObj instanceof BigDecimal) {
						amount = (BigDecimal) amountObj;
					} else if (amountObj instanceof String) {
						amount = new BigDecimal((String) amountObj);
					} else {
						amount = new BigDecimal(amountObj.toString());
					}
				}

				return new PaymentResponse(null, null, null, null, null, null, null, amount, null,
						convertLiqPayStatus(status), (String) responseMap.get("order_id"), liqpayPaymentId, null,
						(String) responseMap.get("err_code"), (String) responseMap.get("err_description"),
						(String) responseMap.get("sender_card_mask2"), (String) responseMap.get("action"),
						isRefundableViaApi, null, null);
			} else {
				throw new PaymentProcessingException("Failed to get payment status");
			}

		} catch (RestClientException e) {
			throw new PaymentProcessingException("Network error during status check");
		} catch (Exception e) {
			throw new PaymentProcessingException("Failed to get payment status");
		}
	}

	private PaymentResponse getSandboxPaymentStatus(String paymentId) {
		log.info("Sandbox payment status: paymentId={}", paymentId);
		return new PaymentResponse(null, null, null, null, null, null, null, BigDecimal.ZERO, null,
				PaymentStatus.SUCCESS, "SANDBOX_ORDER_" + paymentId, paymentId, null, null, null, null, "pay", true,
				null, null);
	}

	private PaymentStatus convertLiqPayStatus(String liqpayStatus) {
		if (liqpayStatus == null) {
			return PaymentStatus.PENDING;
		}

		switch (liqpayStatus.toLowerCase()) {
		case "success":
		case "sandbox":
			return PaymentStatus.SUCCESS;
		case "failure":
		case "error":
			return PaymentStatus.FAILED;
		case "wait_secure":
		case "processing":
			return PaymentStatus.PENDING;
		case "reversed":
		case "refunded":
			return PaymentStatus.REFUNDED;
		default:
			return PaymentStatus.PENDING;
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

	private String generateSignature(String data) {
		try {
			String str = liqpayPrivateKey + data + liqpayPrivateKey;
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] digest = sha1.digest(str.getBytes());
			return Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			throw new PaymentProcessingException("Failed to generate payment signature");
		}
	}
}