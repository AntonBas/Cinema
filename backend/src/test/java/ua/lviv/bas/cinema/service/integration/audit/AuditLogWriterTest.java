package ua.lviv.bas.cinema.service.integration.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditLogWriterTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogWriter auditLogWriter;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    void write_ShouldSaveAuditLogWithDetails() {
        Map<String, Object> oldValues = Map.of("points", 100, "active", true);
        Map<String, Object> newValues = Map.of("points", 200, "active", true);

        auditLogWriter.write("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, "admin@example.com",
                CHANGED_AT, oldValues, newValues);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog.getEntityType()).isEqualTo("BonusRules");
        assertThat(savedLog.getEntityId()).isEqualTo(10L);
        assertThat(savedLog.getTargetInfo()).isEqualTo("WELCOME_BONUS");
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.UPDATED);
        assertThat(savedLog.getChangedBy()).isEqualTo("admin@example.com");
        assertThat(savedLog.getChangedAt()).isEqualTo(CHANGED_AT);
        assertThat(savedLog.getDetails()).hasSize(1);
        assertThat(savedLog.getDetails().getFirst().getFieldName()).isEqualTo("points");
        assertThat(savedLog.getDetails().getFirst().getOldValue()).isEqualTo("100");
        assertThat(savedLog.getDetails().getFirst().getNewValue()).isEqualTo("200");
    }

    @Test
    void write_WhenNoChanges_ShouldNotCreateDetails() {
        auditLogWriter.write("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, "admin@example.com",
                CHANGED_AT, Map.of("points", 100), Map.of("points", 100));

        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertThat(auditLogCaptor.getValue().getDetails()).isEmpty();
    }

    @Test
    void write_WhenOldValuesNull_ShouldNotCreateDetails() {
        auditLogWriter.write("BonusRules", 10L, "WELCOME_BONUS", AuditAction.CREATED, "admin@example.com",
                CHANGED_AT, null, Map.of("points", 200));

        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertThat(auditLogCaptor.getValue().getDetails()).isEmpty();
    }

    @Test
    void write_WhenNewValuesNull_ShouldNotCreateDetails() {
        auditLogWriter.write("BonusRules", 10L, "WELCOME_BONUS", AuditAction.DELETED, "admin@example.com",
                CHANGED_AT, Map.of("points", 100), null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertThat(auditLogCaptor.getValue().getDetails()).isEmpty();
    }

    @Test
    void write_WhenExceptionOccurs_ShouldLogErrorAndNotThrow() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        auditLogWriter.write("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, "admin@example.com",
                CHANGED_AT, Map.of("points", 100), Map.of("points", 200));

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
