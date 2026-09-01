package ua.lviv.bas.cinema.service.integration.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class AuditLogWriter {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(String entityType, Long entityId, String targetInfo, AuditAction action, String changedBy,
                      LocalDateTime changedAt, Map<String, Object> oldValues, Map<String, Object> newValues) {
        try {
            var auditLog = AuditLog.builder().entityType(entityType).entityId(entityId).targetInfo(targetInfo)
                    .action(action).changedBy(changedBy).changedAt(changedAt).build();

            List<AuditLogDetail> details = new ArrayList<>();

            if (oldValues != null && newValues != null) {
                for (var entry : newValues.entrySet()) {
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

            auditLog.setDetails(details);
            auditLogRepository.save(auditLog);

            log.debug("Audit log saved: {} {} {}", entityType, entityId, action);

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private boolean areEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }
}
