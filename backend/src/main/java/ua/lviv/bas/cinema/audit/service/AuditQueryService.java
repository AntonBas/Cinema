package ua.lviv.bas.cinema.audit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.audit.domain.AuditLog;
import ua.lviv.bas.cinema.audit.repository.AuditLogRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLog> findByFilters(String entityType, AuditAction action, String changedBy, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByFilters(entityType, action, changedBy, pageable);

        if (page.isEmpty()) {
            return page;
        }

        List<Long> ids = page.getContent().stream().map(AuditLog::getId).toList();
        List<AuditLog> withDetails = auditLogRepository.findByIdsWithDetails(ids);

        Map<Long, AuditLog> detailsMap = withDetails.stream()
                .collect(Collectors.toMap(AuditLog::getId, Function.identity()));

        page.getContent().forEach(log -> {
            AuditLog withDetail = detailsMap.get(log.getId());
            if (withDetail != null) {
                log.setDetails(withDetail.getDetails());
            }
        });

        return page;
    }

    public List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }
}