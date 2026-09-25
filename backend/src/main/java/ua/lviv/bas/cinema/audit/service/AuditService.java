package ua.lviv.bas.cinema.audit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ua.lviv.bas.cinema.audit.domain.AuditAction;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogWriter auditLogWriter;

    public void logChange(String entityType, Long entityId, String targetInfo, AuditAction action,
                          Map<String, Object> oldValues, Map<String, Object> newValues) {
        String changedBy = getCurrentUser();
        Instant changedAt = Instant.now();
        Runnable write = () -> auditLogWriter.write(entityType, entityId, targetInfo, action, changedBy, changedAt,
                oldValues, newValues);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            write.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                write.run();
            }
        });
    }

    private String getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "system";
        }
        return authentication.getName();
    }
}
