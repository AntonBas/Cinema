package ua.lviv.bas.cinema.service.shared;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;
import ua.lviv.bas.cinema.repository.audit.AuditLogDetailRepository;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;
	private final AuditLogDetailRepository auditLogDetailRepository;

	@Transactional
	public void logChange(String entityType, Long entityId, String targetInfo, AuditAction action,
			Map<String, Object> oldValues, Map<String, Object> newValues) {
		try {
			AuditLog auditLog = AuditLog.builder().entityType(entityType).entityId(entityId).targetInfo(targetInfo)
					.action(action).changedBy(getCurrentUser()).changedAt(LocalDateTime.now()).build();

			auditLog = auditLogRepository.save(auditLog);

			List<AuditLogDetail> details = new ArrayList<>();

			if (oldValues != null && newValues != null) {
				for (Map.Entry<String, Object> entry : newValues.entrySet()) {
					String field = entry.getKey();
					Object newVal = entry.getValue();
					Object oldVal = oldValues.get(field);

					if (!areEqual(oldVal, newVal)) {
						details.add(AuditLogDetail.builder().auditLog(auditLog).fieldName(field)
								.oldValue(oldVal != null ? oldVal.toString() : null)
								.newValue(newVal != null ? newVal.toString() : null).build());
					}
				}
			}

			if (!details.isEmpty()) {
				auditLogDetailRepository.saveAll(details);
			}

			log.debug("Audit log saved: {} {} {}", entityType, entityId, action);

		} catch (Exception e) {
			log.error("Failed to save audit log", e);
		}
	}

	@Transactional
	public void logSimpleChange(String entityType, Long entityId, String targetInfo, AuditAction action,
			Object oldValue, Object newValue) {
		Map<String, Object> oldMap = oldValue != null ? Map.of("value", oldValue) : null;
		Map<String, Object> newMap = newValue != null ? Map.of("value", newValue) : null;
		logChange(entityType, entityId, targetInfo, action, oldMap, newMap);
	}

	private boolean areEqual(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	private String getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return "system";
		}
		return authentication.getName();
	}
}