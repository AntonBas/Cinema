package ua.lviv.bas.cinema.service.integration.payment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Slf4j
@UtilityClass
public class LiqPayDecoder {

    private static final Gson GSON = new Gson();
    private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final Type MAP_STRING_OBJECT_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    public static String encodeToBase64(Object data) {
        return Base64.getEncoder().encodeToString(GSON.toJson(data).getBytes());
    }

    public static Map<String, String> decodeCallback(String data) {
        var decoded = new String(Base64.getDecoder().decode(data));
        return GSON.fromJson(decoded, MAP_STRING_STRING_TYPE);
    }

    public static Map<String, Object> decodeToMap(String data) {
        var decoded = new String(Base64.getDecoder().decode(data));
        return GSON.fromJson(decoded, MAP_STRING_OBJECT_TYPE);
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