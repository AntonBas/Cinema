package ua.lviv.bas.cinema.service.integration.payment;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
			log.info("=== END LIQPAY DATA ===");

			return PaymentLiqPayDataResponse.builder().data(data).signature(signature)
					.paymentUrl("https://www.liqpay.ua/api/3/checkout").liqpayOrderId(payment.getLiqpayOrderId())
					.build();
		} catch (Exception e) {
			log.error("Failed to prepare LiqPay payment data for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to prepare payment data");
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

	public String prepareRefundData(String paymentId, BigDecimal amount, String description) {
		try {
			Map<String, Object> refundParams = new LinkedHashMap<>();
			refundParams.put("public_key", liqpayPublicKey);
			refundParams.put("version", "3");
			refundParams.put("action", "refund");
			refundParams.put("amount", amount.setScale(2, RoundingMode.HALF_UP).toString());
			refundParams.put("currency", "UAH");
			refundParams.put("description", description);
			refundParams.put("order_id", generateRefundOrderId());
			refundParams.put("payment_id", paymentId);

			String jsonData = gson.toJson(refundParams);
			return Base64.getEncoder().encodeToString(jsonData.getBytes());

		} catch (Exception e) {
			log.error("Failed to prepare refund data", e);
			throw new PaymentProcessingException("Failed to prepare refund data");
		}
	}

	public void processRefund(String refundData) {
		try {
			Map<String, String> requestParams = new LinkedHashMap<>();
			requestParams.put("data", refundData);
			requestParams.put("signature", generateSignature(refundData));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestParams, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);
				String status = (String) responseMap.get("status");

				if ("success".equals(status) || "sandbox".equals(status)) {
					log.info("Refund processed successfully via LiqPay API");
				} else {
					log.error("LiqPay refund failed with status: {}", status);
					throw new PaymentProcessingException("LiqPay refund failed: " + status);
				}
			} else {
				log.error("LiqPay API request failed with status: {}", response.getStatusCode());
				throw new PaymentProcessingException("LiqPay API request failed");
			}

		} catch (RestClientException e) {
			log.error("Network error during LiqPay refund", e);
			throw new PaymentProcessingException("Network error during refund");
		} catch (Exception e) {
			log.error("Failed to process refund via LiqPay", e);
			throw new PaymentProcessingException("Failed to process refund");
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

			Map<String, String> requestParams = new LinkedHashMap<>();
			requestParams.put("data", data);
			requestParams.put("signature", signature);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestParams, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(liqpayApiUrl + "request", request,
					String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				Map<String, Object> responseMap = gson.fromJson(response.getBody(), MAP_STRING_OBJECT_TYPE);

				return PaymentResponse.builder()
						.status(PaymentStatus.valueOf(((String) responseMap.get("status")).toUpperCase()))
						.liqpayOrderId((String) responseMap.get("order_id"))
						.liqpayPaymentId((String) responseMap.get("payment_id"))
						.amount(new BigDecimal(responseMap.get("amount").toString()))
						.errorCode((String) responseMap.get("err_code"))
						.errorDescription((String) responseMap.get("err_description"))
						.senderCardMask((String) responseMap.get("sender_card_mask")).build();
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
		params.put("sender_first_name", payment.getBooking().getUser().getFirstName());
		params.put("sender_last_name", payment.getBooking().getUser().getLastName());
		params.put("sender_email", payment.getBooking().getUser().getEmail());

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

	private String generateRefundOrderId() {
		return "REF_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}