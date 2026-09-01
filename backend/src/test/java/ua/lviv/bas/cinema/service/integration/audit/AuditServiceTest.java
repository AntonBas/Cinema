package ua.lviv.bas.cinema.service.integration.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.lviv.bas.cinema.domain.audit.AuditAction;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogWriter auditLogWriter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logChange_ShouldResolveCurrentUserAndDelegateToWriter() {
        setupAuthentication();

        Map<String, Object> oldValues = Map.of("points", 100, "active", true);
        Map<String, Object> newValues = Map.of("points", 200, "active", true);

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

        verify(auditLogWriter).write(eq("BonusRules"), eq(10L), eq("WELCOME_BONUS"), eq(AuditAction.UPDATED),
                eq("admin@example.com"), any(LocalDateTime.class), eq(oldValues), eq(newValues));
    }

    @Test
    void logChange_WhenAuthenticationNull_ShouldResolveSystemUser() {
        setupNullAuthentication();

        auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, null, null);

        verify(auditLogWriter).write(eq("BonusRules"), eq(10L), eq("WELCOME_BONUS"), eq(AuditAction.UPDATED),
                eq("system"), any(LocalDateTime.class), isNull(), isNull());
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
