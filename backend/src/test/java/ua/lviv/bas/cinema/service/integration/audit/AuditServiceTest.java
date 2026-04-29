package ua.lviv.bas.cinema.service.integration.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    @Test
    void logChange_ShouldSaveAuditLogWithDetails() {
        setupAuthentication();

        Map<String, Object> oldValues = Map.of("points", 100, "active", true);
        Map<String, Object> newValues = Map.of("points", 200, "active", true);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getEntityType()).isEqualTo("BonusRules");
        assertThat(savedLog.getEntityId()).isEqualTo(10L);
        assertThat(savedLog.getTargetInfo()).isEqualTo("WELCOME_BONUS");
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.UPDATED);
        assertThat(savedLog.getChangedBy()).isEqualTo("admin@example.com");
        assertThat(savedLog.getChangedAt()).isNotNull();
        assertThat(savedLog.getDetails()).hasSize(1);
        assertThat(savedLog.getDetails().getFirst().getFieldName()).isEqualTo("points");
        assertThat(savedLog.getDetails().getFirst().getOldValue()).isEqualTo("100");
        assertThat(savedLog.getDetails().getFirst().getNewValue()).isEqualTo("200");
    }

    @Test
    void logChange_WhenNoChanges_ShouldNotCreateDetails() {
        setupAuthentication();

        Map<String, Object> oldValues = Map.of("points", 100);
        Map<String, Object> newValues = Map.of("points", 100);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog.getDetails()).isEmpty();
    }

    @Test
    void logChange_WhenOldValuesNull_ShouldNotCreateDetails() {
        setupAuthentication();

        Map<String, Object> newValues = Map.of("points", 200);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.CREATED, null, newValues);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog.getDetails()).isEmpty();
    }

    @Test
    void logChange_WhenNewValuesNull_ShouldNotCreateDetails() {
        setupAuthentication();

        Map<String, Object> oldValues = Map.of("points", 100);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.DELETED, oldValues, null);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog.getDetails()).isEmpty();
    }

    @Test
    void logChange_WhenAuthenticationNull_ShouldUseSystem() {
        setupNullAuthentication();

        Map<String, Object> oldValues = Map.of("points", 100);
        Map<String, Object> newValues = Map.of("points", 200);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertThat(savedLog.getChangedBy()).isEqualTo("system");
    }

    @Test
    void logChange_WhenExceptionOccurs_ShouldLogErrorAndNotThrow() {
        setupAuthentication();
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> oldValues = Map.of("points", 100);
        Map<String, Object> newValues = Map.of("points", 200);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    private void setupAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupNullAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }
}