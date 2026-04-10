package ua.lviv.bas.cinema.service.integration.payment;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LiqPayDecoder {

	private static final Gson GSON = new Gson();
	private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
	}.getType();

	public static Map<String, String> decodeCallback(String data) {
		try {
			var decodedBytes = Base64.getDecoder().decode(data);
			var decodedString = new String(decodedBytes);
			Map<String, Object> rawMap = GSON.fromJson(decodedString, MAP_TYPE);

			Map<String, String> result = new HashMap<>();
			for (var entry : rawMap.entrySet()) {
				if (entry.getValue() != null) {
					result.put(entry.getKey(), entry.getValue().toString());
				}
			}

			return result;
		} catch (Exception e) {
			log.error("Failed to decode LiqPay data: {}", data, e);
			throw new RuntimeException("Invalid LiqPay callback data", e);
		}
	}

	public static boolean verifySignature(String data, String signature, String privateKey) {
		try {
			var str = privateKey + data + privateKey;
			var sha1 = java.security.MessageDigest.getInstance("SHA-1");
			var digest = sha1.digest(str.getBytes());
			var expectedSignature = Base64.getEncoder().encodeToString(digest);
			return expectedSignature.equals(signature);
		} catch (Exception e) {
			log.error("Failed to verify LiqPay signature", e);
			return false;
		}
	}
}