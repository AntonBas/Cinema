package ua.lviv.bas.cinema.service.integration.audit;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryService {

	private final AuditLogRepository auditLogRepository;

	public Page<AuditLog> findByFilters(String entityType, AuditAction action, String changedBy, Pageable pageable) {
		return auditLogRepository.findByFilters(entityType, action, changedBy, pageable);
	}

	public List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId) {
		return auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
	}
}