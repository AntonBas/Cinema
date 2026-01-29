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
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;

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

			log.info("=== LIQPAY PAYMENT DATA ===");
			log.info("Payment ID: {}", payment.getId());
			log.info("Order ID: {}", payment.getLiqpayOrderId());
			log.info("Amount: {}", payment.getAmount());
			log.info("Sandbox mode: {}", sandboxMode);
			log.info("Action: pay");
			log.info("=== END LIQPAY DATA ===");

			String paymentUrl = createPayment(payment);

			return PaymentLiqPayDataResponse.builder().data(data).signature(signature).paymentUrl(paymentUrl)
					.liqpayOrderId(payment.getLiqpayOrderId()).build();
		} catch (Exception e) {
			log.error("Failed to prepare LiqPay payment data for payment {}", payment.getId(), e);
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

			log.info("LiqPay request data: {}", data);

			String paymentUrl = liqpayApiUrl + "3/checkout?data="
					+ URLEncoder.encode(data, StandardCharsets.UTF_8.toString()) + "&signature="
					+ URLEncoder.encode(signature, StandardCharsets.UTF_8.toString());

			return paymentUrl;
		} catch (Exception e) {
			log.error("Failed to create payment URL", e);
			throw new PaymentProcessingException("Failed to create payment URL");
		}
	}

	public boolean verifyCallbackSignature(String data, String receivedSignature) {
		String calculatedSignature = generateSignature(data);
		boolean isValid = calculatedSignature.equals(receivedSignature);

		if (!isValid) {
			log.error("Invalid LiqPay signature! Received: {}, Calculated: {}", receivedSignature, calculatedSignature);
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
			refundParams.put("payment_id", originalLiqpayPaymentId);

			if (sandboxMode) {
				refundParams.put("sandbox", "1");
			}

			String jsonData = gson.toJson(refundParams);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());

			log.info("=== LIQPAY REFUND DATA ===");
			log.info("Original Payment ID: {}", originalLiqpayPaymentId);
			log.info("Original Order ID: {}", originalOrderId);
			log.info("Refund amount: {}", amount);
			log.info("Description: {}", description);
			log.info("JSON Data: {}", jsonData);
			log.info("Base64 Data: {}", data);
			log.info("=== END REFUND DATA ===");

			return data;

		} catch (Exception e) {
			log.error("Failed to prepare refund data", e);
			throw new PaymentProcessingException("Failed to prepare refund data");
		}
	}

	public void processRefund(String refundData) {
		try {
			String signature = generateSignature(refundData);

			log.info("=== LIQPAY REFUND REQUEST ===");
			log.info("Refund data: {}", refundData);
			log.info("Signature: {}", signature);
			log.info("API URL: {}", liqpayApiUrl + "request");

			try {
				String decodedData = new String(Base64.getDecoder().decode(refundData));
				log.info("Decoded refund params: {}", decodedData);
			} catch (Exception e) {
				log.warn("Could not decode refund data for logging: {}", e.getMessage());
			}

			String requestBody = "data=" + URLEncoder.encode(refundData, StandardCharsets.UTF_8.toString())
					+ "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.toString());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

			log.info("Request body: {}", requestBody);

			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			log.info("=== LIQPAY REFUND RESPONSE ===");
			log.info("HTTP Status: {}", response.getStatusCode());
			log.info("Response body: {}", response.getBody());

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);
				String result = (String) responseMap.get("result");
				String status = (String) responseMap.get("status");

				log.info("Refund result: {}", result);
				log.info("Refund status: {}", status);
				log.info("Full response: {}", responseMap);

				if ("success".equals(result) || "success".equals(status) || "sandbox".equals(status)) {
					log.info("Refund processed successfully via LiqPay API");
				} else {
					String errorCode = (String) responseMap.get("err_code");
					String errorDescription = (String) responseMap.get("err_description");

					log.error("LiqPay refund failed with result: {}, status: {}, error_code: {}, error_description: {}",
							result, status, errorCode, errorDescription);
					throw new PaymentProcessingException(
							String.format("LiqPay refund failed: %s - %s - %s", result, errorCode, errorDescription));
				}
			} else {
				log.error("LiqPay API request failed with status: {}", response.getStatusCode());
				throw new PaymentProcessingException("LiqPay API request failed");
			}

		} catch (RestClientException e) {
			log.error("Network error during LiqPay refund", e);
			throw new PaymentProcessingException("Network error during refund: " + e.getMessage());
		} catch (Exception e) {
			log.error("Failed to process refund via LiqPay", e);
			throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
		}
	}

	public PaymentResponse getPaymentStatus(String paymentId) {
		try {
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

			log.info("=== LIQPAY STATUS CHECK ===");
			log.info("Payment ID: {}", paymentId);
			log.info("Request data: {}", data);

			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			log.info("Status check response: {}", response.getBody());

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);

				String result = (String) responseMap.get("result");
				String status = (String) responseMap.get("status");

				if ("error".equals(result)) {
					String errorCode = (String) responseMap.get("err_code");
					String errorDescription = (String) responseMap.get("err_description");

					log.warn("LiqPay status check error: {} - {}", errorCode, errorDescription);

					if ("payment_not_found".equals(errorCode) || "err_missing".equals(errorCode)) {
						return PaymentResponse.builder().status(PaymentStatus.FAILED).errorCode(errorCode)
								.errorDescription(errorDescription).refundableViaApi(false).build();
					}

					throw new PaymentProcessingException("LiqPay status check error: " + errorDescription);
				}

				String actionType = (String) responseMap.get("action");
				boolean isRefundableViaApi = isPaymentRefundableViaApi(status, actionType);

				log.info("Payment action: {}, status: {}, isRefundableViaApi: {}", actionType, status,
						isRefundableViaApi);

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

				return PaymentResponse.builder().status(convertLiqPayStatus(status))
						.liqpayOrderId((String) responseMap.get("order_id")).liqpayPaymentId(liqpayPaymentId)
						.amount(amount).errorCode((String) responseMap.get("err_code"))
						.errorDescription((String) responseMap.get("err_description"))
						.senderCardMask((String) responseMap.get("sender_card_mask2")).actionType(actionType)
						.refundableViaApi(isRefundableViaApi).build();
			} else {
				log.error("Failed to get payment status from LiqPay API");
				throw new PaymentProcessingException("Failed to get payment status");
			}

		} catch (RestClientException e) {
			log.error("Network error during LiqPay status check", e);
			throw new PaymentProcessingException("Network error during status check");
		} catch (Exception e) {
			log.error("Failed to get payment status for ID: {}", paymentId, e);
			throw new PaymentProcessingException("Failed to get payment status");
		}
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

	private boolean isPaymentRefundableViaApi(String status, String actionType) {
		if (status == null || actionType == null) {
			return false;
		}

		if (!"success".equals(status) && !"sandbox".equals(status)) {
			return false;
		}

		if ("pay".equals(actionType)) {
			return false;
		}

		if ("invoice_bot".equals(actionType) || "p2p".equals(actionType)) {
			return true;
		}

		return false;
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
			log.error("Failed to generate LiqPay signature", e);
			throw new PaymentProcessingException("Failed to generate payment signature");
		}
	}
}