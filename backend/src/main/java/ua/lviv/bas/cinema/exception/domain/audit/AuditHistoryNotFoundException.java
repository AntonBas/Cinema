package ua.lviv.bas.cinema.exception.domain.audit;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class AuditHistoryNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuditHistoryNotFoundException(String entityType, Long entityId) {
        super("No audit logs found for this entity", "AUDIT_HISTORY_NOT_FOUND",
                "No audit history exists for " + entityType + " with id " + entityId);
    }
}
