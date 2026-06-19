package ua.lviv.bas.cinema.service.integration.payment;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiqPayDecoderTest {

    private static final String PRIVATE_KEY = "test_private_key";

    @Test
    void decodeCallbackShouldReturnMap() {
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
    void decodeCallbackWithNullDataShouldThrowException() {
        assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void decodeCallbackWithInvalidBase64ShouldThrowException() {
        String invalidData = "not-a-valid-base64-string";

        assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(invalidData))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void decodeCallbackWithInvalidJsonShouldThrowException() {
        String invalidData = "bm90LWEtdmFsaWQtanNvbg==";

        assertThatThrownBy(() -> LiqPayDecoder.decodeCallback(invalidData))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generateSignatureShouldReturnValidSignature() {
        String data = "test_data";
        String signature = LiqPayDecoder.generateSignature(data, PRIVATE_KEY);

        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
    }

    @Test
    void encodeToBase64ShouldReturnValidBase64() {
        Map<String, String> data = Map.of("key", "value");
        String encoded = LiqPayDecoder.encodeToBase64(data);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEmpty();
    }

    @Test
    void decodeToMapShouldReturnMap() {
        String testData = "eyJrZXkiOiJ2YWx1ZSJ9";

        Map<String, Object> result = LiqPayDecoder.decodeToMap(testData);

        assertThat(result).isNotNull();
        assertThat(result).containsKey("key");
        assertThat(result.get("key")).isEqualTo("value");
    }

    @Test
    void decodeCallbackWithNullValuesShouldHandleCorrectly() {
        String testData = "eyJwdWJsaWNfa2V5IjoidGVzdCIsImFtb3VudCI6IjEwMC4wMCIsIm51bGxfZmllbGQiOm51bGx9";

        Map<String, String> result = LiqPayDecoder.decodeCallback(testData);

        assertThat(result).isNotNull();
        assertThat(result).containsKeys("public_key", "amount", "null_field");
        assertThat(result.get("public_key")).isEqualTo("test");
        assertThat(result.get("amount")).isEqualTo("100.00");
        assertThat(result.get("null_field")).isNull();
    }
}