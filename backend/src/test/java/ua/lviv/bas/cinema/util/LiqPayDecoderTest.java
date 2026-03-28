package ua.lviv.bas.cinema.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.service.integration.payment.LiqPayDecoder;

public class LiqPayDecoderTest {

	@Test
	void decodeCallback_ValidData_ShouldReturnMap() {
		String testData = "eyJwdWJsaWNfa2V5IjoidGVzdF9wdWJsaWNfa2V5IiwidmVyc2lvbiI6IjMiLCJhY3Rpb24iOiJwYXkiLCJhbW91bnQiOiIzODAuMDAiLCJjdXJyZW5jeSI6IlVBSCIsImRlc2NyaXB0aW9uIjoiVGVzdCBwYXltZW50Iiwib3JkZXJfaWQiOiJPUkRfMTIzNDU2Iiwic3RhdHVzIjoic3VjY2VzcyIsInBheW1lbnRfaWQiOiIxMjM0NTY3ODkifQ==";

		Map<String, String> result = LiqPayDecoder.decodeCallback(testData);

		assertThat(result).isNotNull();
		assertThat(result).containsKeys("public_key", "version", "action", "amount", "currency", "description",
				"order_id", "status", "payment_id");
		assertThat(result.get("public_key")).isEqualTo("test_public_key");
		assertThat(result.get("version")).isEqualTo("3");
		assertThat(result.get("action")).isEqualTo("pay");
		assertThat(result.get("amount")).isEqualTo("380.00");
		assertThat(result.get("currency")).isEqualTo("UAH");
		assertThat(result.get("description")).isEqualTo("Test payment");
		assertThat(result.get("order_id")).isEqualTo("ORD_123456");
		assertThat(result.get("status")).isEqualTo("success");
		assertThat(result.get("payment_id")).isEqualTo("123456789");
	}

	@Test
	void decodeCallback_NullData_ShouldThrowException() {
		assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(null)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Invalid LiqPay callback data");
	}

	@Test
	void decodeCallback_InvalidBase64Data_ShouldThrowException() {
		String invalidData = "not-a-valid-base64-string";

		assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(invalidData)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Invalid LiqPay callback data");
	}

	@Test
	void decodeCallback_InvalidJsonData_ShouldThrowException() {
		String invalidData = "bm90LWEtdmFsaWQtanNvbg==";

		assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(invalidData)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Invalid LiqPay callback data");
	}

	@Test
	void verifySignature_ValidSignature_ShouldReturnTrue() {
		String testData = "test_data";
		String privateKey = "test_private_key";

		String expectedSignature = calculateSignature(testData, privateKey);

		boolean result = LiqPayDecoder.verifySignature(testData, expectedSignature, privateKey);

		assertThat(result).isTrue();
	}

	@Test
	void verifySignature_InvalidSignature_ShouldReturnFalse() {
		String testData = "test_data";
		String privateKey = "test_private_key";
		String wrongSignature = "wrong_signature";

		boolean result = LiqPayDecoder.verifySignature(testData, wrongSignature, privateKey);

		assertThat(result).isFalse();
	}

	@Test
	void verifySignature_NullData_ShouldReturnFalse() {
		String privateKey = "test_private_key";
		String signature = "some_signature";

		boolean result = LiqPayDecoder.verifySignature(null, signature, privateKey);

		assertThat(result).isFalse();
	}

	@Test
	void verifySignature_NullSignature_ShouldReturnFalse() {
		String testData = "test_data";
		String privateKey = "test_private_key";

		boolean result = LiqPayDecoder.verifySignature(testData, null, privateKey);

		assertThat(result).isFalse();
	}

	@Test
	void verifySignature_NullPrivateKey_ShouldReturnFalse() {
		String testData = "test_data";
		String signature = "some_signature";

		boolean result = LiqPayDecoder.verifySignature(testData, signature, null);

		assertThat(result).isFalse();
	}

	private String calculateSignature(String data, String privateKey) {
		try {
			String str = privateKey + data + privateKey;
			java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
			byte[] digest = sha1.digest(str.getBytes());
			return java.util.Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate signature", e);
		}
	}

	@Test
	void decodeCallback_WithNullValues_ShouldHandleCorrectly() {
		String testData = "eyJwdWJsaWNfa2V5IjoidGVzdCIsImFtb3VudCI6IjEwMC4wMCIsIm51bGxfZmllbGQiOm51bGx9";

		Map<String, String> result = LiqPayDecoder.decodeCallback(testData);

		assertThat(result).isNotNull();
		assertThat(result).containsKeys("public_key", "amount");
		assertThat(result.get("public_key")).isEqualTo("test");
		assertThat(result.get("amount")).isEqualTo("100.00");
		assertThat(result).doesNotContainKey("null_field");
	}
}