package ua.lviv.bas.cinema.dto.audit;

import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.audit.AuditAction;

public record AuditLogResponse(Long id, String entityType, Long entityId, AuditAction action, String oldValue,
		String newValue, String changedBy, LocalDateTime changedAt) {
}