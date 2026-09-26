package ua.lviv.bas.cinema.payment.service;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Slf4j
@UtilityClass
public class LiqPayDecoder {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    public static String encodeToBase64(Object data) {
        return Base64.getEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(data));
    }

    public static Map<String, String> decodeCallback(String data) {
        var decoded = Base64.getDecoder().decode(data);
        return OBJECT_MAPPER.readValue(decoded, new TypeReference<Map<String, String>>() {
        });
    }

    public static Map<String, Object> decodeToMap(String data) {
        var decoded = Base64.getDecoder().decode(data);
        return OBJECT_MAPPER.readValue(decoded, new TypeReference<Map<String, Object>>() {
        });
    }

    public static String generateSignature(String data, String privateKey) {
        try {
            var str = privateKey + data + privateKey;
            var sha1 = MessageDigest.getInstance("SHA-1");
            return Base64.getEncoder().encodeToString(sha1.digest(str.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
