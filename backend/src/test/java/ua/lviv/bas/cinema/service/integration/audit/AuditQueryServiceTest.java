package ua.lviv.bas.cinema.service.integration.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditQueryServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditQueryService auditQueryService;

    @Test
    void findByFilters_ShouldReturnPage() {
        String entityType = "BonusRules";
        AuditAction action = AuditAction.UPDATED;
        String changedBy = "admin@example.com";
        Pageable pageable = Pageable.ofSize(10);

        List<AuditLog> expectedLogs = List.of(new AuditLog(), new AuditLog());
        Page<AuditLog> expectedPage = new PageImpl<>(expectedLogs, pageable, 2);

        when(auditLogRepository.findByFilters(entityType, action, changedBy, pageable)).thenReturn(expectedPage);

        Page<AuditLog> result = auditQueryService.findByFilters(entityType, action, changedBy, pageable);

        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        verify(auditLogRepository).findByFilters(entityType, action, changedBy, pageable);
    }

    @Test
    void findByFilters_WithNullParameters_ShouldPassNullToRepository() {
        Pageable pageable = Pageable.ofSize(10);
        Page<AuditLog> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(auditLogRepository.findByFilters(null, null, null, pageable)).thenReturn(expectedPage);

        Page<AuditLog> result = auditQueryService.findByFilters(null, null, null, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(auditLogRepository).findByFilters(null, null, null, pageable);
    }

    @Test
    void findByFilters_WithOnlyEntityType_ShouldPassOnlyEntityType() {
        String entityType = "User";
        Pageable pageable = Pageable.ofSize(10);
        Page<AuditLog> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(auditLogRepository.findByFilters(eq(entityType), eq(null), eq(null), eq(pageable)))
                .thenReturn(expectedPage);

        Page<AuditLog> result = auditQueryService.findByFilters(entityType, null, null, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(auditLogRepository).findByFilters(entityType, null, null, pageable);
    }

    @Test
    void findByFilters_WithOnlyAction_ShouldPassOnlyAction() {
        AuditAction action = AuditAction.CREATED;
        Pageable pageable = Pageable.ofSize(10);
        Page<AuditLog> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(auditLogRepository.findByFilters(eq(null), eq(action), eq(null), eq(pageable))).thenReturn(expectedPage);

        Page<AuditLog> result = auditQueryService.findByFilters(null, action, null, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(auditLogRepository).findByFilters(null, action, null, pageable);
    }

    @Test
    void findByFilters_WithOnlyChangedBy_ShouldPassOnlyChangedBy() {
        String changedBy = "system";
        Pageable pageable = Pageable.ofSize(10);
        Page<AuditLog> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(auditLogRepository.findByFilters(eq(null), eq(null), eq(changedBy), eq(pageable)))
                .thenReturn(expectedPage);

        Page<AuditLog> result = auditQueryService.findByFilters(null, null, changedBy, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(auditLogRepository).findByFilters(null, null, changedBy, pageable);
    }

    @Test
    void findByEntityTypeAndEntityId_ShouldReturnList() {
        String entityType = "BonusRules";
        Long entityId = 10L;

        List<AuditLog> expectedLogs = List.of(createAuditLog(1L, entityType, entityId),
                createAuditLog(2L, entityType, entityId));

        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId))
                .thenReturn(expectedLogs);

        List<AuditLog> result = auditQueryService.findByEntityTypeAndEntityId(entityType, entityId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedLogs);
        verify(auditLogRepository).findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }

    @Test
    void findByEntityTypeAndEntityId_WhenNoLogs_ShouldReturnEmptyList() {
        String entityType = "BonusRules";
        Long entityId = 999L;

        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId))
                .thenReturn(List.of());

        List<AuditLog> result = auditQueryService.findByEntityTypeAndEntityId(entityType, entityId);

        assertThat(result).isEmpty();
        verify(auditLogRepository).findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }

    private AuditLog createAuditLog(Long id, String entityType, Long entityId) {
        return AuditLog.builder().id(id).entityType(entityType).entityId(entityId).build();
    }
}