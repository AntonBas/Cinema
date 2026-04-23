package ua.lviv.bas.cinema.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.audit.AuditAction;

import java.time.LocalDateTime;
import java.util.List;

public record AuditLogResponse(
        @Schema(description = "Unique identifier of the audit log entry", example = "1")
        Long id,

        @Schema(description = "Type of the entity that was changed", example = "User")
        String entityType,

        @Schema(description = "ID of the entity that was changed", example = "15")
        Long entityId,

        @Schema(description = "Target entity identifier (email, title, etc.)", example = "user@example.com")
        String targetInfo,

        @Schema(description = "Action that was performed", example = "CREATED")
        AuditAction action,

        @Schema(description = "Email of the user who performed the action", example = "admin@example.com")
        String changedBy,

        @Schema(description = "Date and time when the action was performed", example = "2024-01-15T10:30:00")
        LocalDateTime changedAt,

        @Schema(description = "List of field changes")
        List<AuditLogDetailResponse> details
) {
    public record AuditLogDetailResponse(
            @Schema(description = "Field name that was changed", example = "role")
            String fieldName,

            @Schema(description = "Old value", example = "ROLE_USER")
            String oldValue,

            @Schema(description = "New value", example = "ROLE_ADMIN")
            String newValue
    ) {
    }
}