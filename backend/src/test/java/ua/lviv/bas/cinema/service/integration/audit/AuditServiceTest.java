package ua.lviv.bas.cinema.service.integration.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

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
import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

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
		setupAuthentication("admin@example.com");

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
		assertThat(savedLog.getDetails().get(0).getFieldName()).isEqualTo("points");
		assertThat(savedLog.getDetails().get(0).getOldValue()).isEqualTo("100");
		assertThat(savedLog.getDetails().get(0).getNewValue()).isEqualTo("200");
	}

	@Test
	void logChange_WhenNoChanges_ShouldNotCreateDetails() {
		setupAuthentication("admin@example.com");

		Map<String, Object> oldValues = Map.of("points", 100);
		Map<String, Object> newValues = Map.of("points", 100);

		auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).isEmpty();
	}

	@Test
	void logChange_WhenOldValuesNull_ShouldNotCreateDetails() {
		setupAuthentication("admin@example.com");

		Map<String, Object> newValues = Map.of("points", 200);

		auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.CREATED, null, newValues);

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).isEmpty();
	}

	@Test
	void logChange_WhenNewValuesNull_ShouldNotCreateDetails() {
		setupAuthentication("admin@example.com");

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
		setupAuthentication("admin@example.com");
		when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

		Map<String, Object> oldValues = Map.of("points", 100);
		Map<String, Object> newValues = Map.of("points", 200);

		auditService.logChange("BonusRules", 10L, "WELCOME_BONUS", AuditAction.UPDATED, oldValues, newValues);

		verify(auditLogRepository).save(any(AuditLog.class));
	}

	@Test
	void logSimpleChange_WithBothValues_ShouldCreateMap() {
		setupAuthentication("admin@example.com");

		auditService.logSimpleChange("User", 5L, "user@example.com", AuditAction.UPDATED, "ROLE_USER", "ROLE_ADMIN");

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).hasSize(1);
		AuditLogDetail detail = savedLog.getDetails().get(0);
		assertThat(detail.getFieldName()).isEqualTo("value");
		assertThat(detail.getOldValue()).isEqualTo("ROLE_USER");
		assertThat(detail.getNewValue()).isEqualTo("ROLE_ADMIN");
	}

	@Test
	void logSimpleChange_WithNullOldValue_ShouldCreateMap() {
		setupAuthentication("admin@example.com");

		auditService.logSimpleChange("User", 5L, "user@example.com", AuditAction.CREATED, null, "ROLE_USER");

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).hasSize(1);
		AuditLogDetail detail = savedLog.getDetails().get(0);
		assertThat(detail.getFieldName()).isEqualTo("value");
		assertThat(detail.getOldValue()).isNull();
		assertThat(detail.getNewValue()).isEqualTo("ROLE_USER");
	}

	@Test
	void logSimpleChange_WithNullNewValue_ShouldCreateMap() {
		setupAuthentication("admin@example.com");

		auditService.logSimpleChange("User", 5L, "user@example.com", AuditAction.DELETED, "ROLE_USER", null);

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).hasSize(1);
		AuditLogDetail detail = savedLog.getDetails().get(0);
		assertThat(detail.getFieldName()).isEqualTo("value");
		assertThat(detail.getOldValue()).isEqualTo("ROLE_USER");
		assertThat(detail.getNewValue()).isNull();
	}

	@Test
	void logSimpleChange_WithBothValuesNull_ShouldNotCreateLog() {
		setupAuthentication("admin@example.com");

		auditService.logSimpleChange("User", 5L, "user@example.com", AuditAction.UPDATED, null, null);

		verify(auditLogRepository).save(auditLogCaptor.capture());
		AuditLog savedLog = auditLogCaptor.getValue();

		assertThat(savedLog.getDetails()).isEmpty();
	}

	private void setupAuthentication(String username) {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn(username);
		SecurityContextHolder.setContext(securityContext);
	}

	private void setupNullAuthentication() {
		when(securityContext.getAuthentication()).thenReturn(null);
		SecurityContextHolder.setContext(securityContext);
	}
}