package ua.lviv.bas.cinema.service.shared;

import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;
	private final ObjectMapper objectMapper;

	public void logChange(String entityType, Long entityId, String action, Object oldValue, Object newValue) {
		try {
			AuditLog auditLog = AuditLog.builder().entityType(entityType).entityId(entityId).action(action)
					.oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
					.newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
					.changedBy(getCurrentUser()).changedAt(LocalDateTime.now()).build();

			auditLogRepository.save(auditLog);
			log.debug("Autdit log saved: {} {} {}", entityType, entityId, action);
		} catch (Exception e) {
			log.error("Failed to save audit log", e);
		}
	}

	private String getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return "system";
		}
		return authentication.getName();
	}
}
