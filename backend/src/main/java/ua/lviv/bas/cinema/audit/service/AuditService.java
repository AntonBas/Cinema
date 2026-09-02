package ua.lviv.bas.cinema.audit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.audit.domain.AuditAction;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogWriter auditLogWriter;

    public void logChange(String entityType, Long entityId, String targetInfo, AuditAction action,
                          Map<String, Object> oldValues, Map<String, Object> newValues) {
        auditLogWriter.write(entityType, entityId, targetInfo, action, getCurrentUser(), LocalDateTime.now(),
                oldValues, newValues);
    }

    private String getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}
