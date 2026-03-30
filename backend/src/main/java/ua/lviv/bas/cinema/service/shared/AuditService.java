package ua.lviv.bas.cinema.service.shared;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;

	public void logChange(String entityType, Long entityId, AuditAction action, Object oldValue, Object newValue) {
		try {
			log.debug("=== AUDIT LOG START ===");
			log.debug("EntityType: {}, EntityId: {}, Action: {}", entityType, entityId, action);
			log.debug("OldValue class: {}, OldValue: {}",
					oldValue != null ? oldValue.getClass().getSimpleName() : "null", oldValue);
			log.debug("NewValue class: {}, NewValue: {}",
					newValue != null ? newValue.getClass().getSimpleName() : "null", newValue);

			ObjectMapper cleanMapper = createCleanObjectMapper();

			String oldJson = null;
			String newJson = null;

			if (oldValue != null && newValue != null) {
				Map<String, Object> oldMap = extractRelevantDataAsMap(oldValue);
				Map<String, Object> newMap = extractRelevantDataAsMap(newValue);

				log.debug("OldMap: {}", oldMap);
				log.debug("NewMap: {}", newMap);

				Map<String, Object> changedOldFields = new HashMap<>();
				Map<String, Object> changedNewFields = new HashMap<>();

				for (Map.Entry<String, Object> entry : newMap.entrySet()) {
					String key = entry.getKey();
					Object newVal = entry.getValue();
					Object oldVal = oldMap.get(key);

					if (!areEqual(oldVal, newVal)) {
						log.debug("Changed field: {} (old: {}, new: {})", key, oldVal, newVal);
						changedOldFields.put(key, oldVal);
						changedNewFields.put(key, newVal);
					}
				}

				if (!changedOldFields.isEmpty()) {
					oldJson = cleanMapper.writeValueAsString(changedOldFields);
				}
				if (!changedNewFields.isEmpty()) {
					newJson = cleanMapper.writeValueAsString(changedNewFields);
				}

				log.debug("Changed fields detected: {}", !changedOldFields.isEmpty());
			} else {
				if (oldValue != null) {
					Object cleanOldValue = extractRelevantData(oldValue);
					oldJson = cleanMapper.writeValueAsString(cleanOldValue);
				}
				if (newValue != null) {
					Object cleanNewValue = extractRelevantData(newValue);
					newJson = cleanMapper.writeValueAsString(cleanNewValue);
				}
			}

			log.debug("Final oldJson: {}", oldJson);
			log.debug("Final newJson: {}", newJson);

			AuditLog auditLog = AuditLog.builder().entityType(entityType).entityId(entityId).action(action)
					.oldValue(oldJson).newValue(newJson).changedBy(getCurrentUser()).changedAt(LocalDateTime.now())
					.build();

			auditLogRepository.save(auditLog);
			log.debug("Audit log saved: {} {} {}", entityType, entityId, action);
			log.debug("=== AUDIT LOG END ===");
		} catch (Exception e) {
			log.error("Failed to save audit log", e);
		}
	}

	private boolean areEqual(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	private Map<String, Object> extractRelevantDataAsMap(Object obj) {
		if (obj == null)
			return new HashMap<>();

		if (obj instanceof String) {
			Map<String, Object> map = new HashMap<>();
			map.put("value", obj);
			return map;
		}
		if (obj instanceof Number) {
			Map<String, Object> map = new HashMap<>();
			map.put("value", obj);
			return map;
		}
		if (obj instanceof Boolean) {
			Map<String, Object> map = new HashMap<>();
			map.put("value", obj);
			return map;
		}
		if (obj instanceof Enum) {
			Map<String, Object> map = new HashMap<>();
			map.put("value", obj.toString());
			return map;
		}

		try {
			ObjectMapper tempMapper = new ObjectMapper();
			tempMapper.registerModule(new JavaTimeModule());
			tempMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			Map<String, Object> allFields = tempMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
			});
			Map<String, Object> relevantFields = new HashMap<>();

			for (Map.Entry<String, Object> entry : allFields.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();

				if (shouldExcludeField(key)) {
					continue;
				}

				if (value != null) {
					relevantFields.put(key, value);
				}
			}

			return relevantFields;
		} catch (Exception e) {
			log.warn("Failed to extract relevant data from object: {}", e.getMessage());
			Map<String, Object> fallback = new HashMap<>();
			fallback.put("value", obj.toString());
			return fallback;
		}
	}

	private Object extractRelevantData(Object obj) {
		if (obj == null)
			return null;

		if (obj instanceof String)
			return obj;
		if (obj instanceof Number)
			return obj;
		if (obj instanceof Boolean)
			return obj;
		if (obj instanceof Enum)
			return obj.toString();

		try {
			Map<String, Object> allFields = new ObjectMapper().convertValue(obj,
					new TypeReference<Map<String, Object>>() {
					});
			Map<String, Object> relevantFields = new HashMap<>();

			for (Map.Entry<String, Object> entry : allFields.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();

				if (shouldExcludeField(key)) {
					continue;
				}

				if (value != null) {
					relevantFields.put(key, value);
				}
			}

			return relevantFields;
		} catch (Exception e) {
			log.warn("Failed to extract relevant data from object: {}", e.getMessage());
			return obj;
		}
	}

	private boolean shouldExcludeField(String fieldName) {
		return fieldName.equals("createdBy") || fieldName.equals("createdDate") || fieldName.equals("lastModifiedBy")
				|| fieldName.equals("lastModifiedDate") || fieldName.equals("id") || fieldName.equals("class")
				|| fieldName.startsWith("hibernateLazyInitializer");
	}

	private ObjectMapper createCleanObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	private String getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return "system";
		}
		return authentication.getName();
	}
}