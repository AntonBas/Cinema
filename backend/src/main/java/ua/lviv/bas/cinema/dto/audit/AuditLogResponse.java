package ua.lviv.bas.cinema.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String entityType, Long entityId, String action, String oldValue,
		String newValue, String changedBy, LocalDateTime changedAt) {
}