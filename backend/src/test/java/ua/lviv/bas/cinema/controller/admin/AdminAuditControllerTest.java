package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;
import ua.lviv.bas.cinema.mapper.audit.AuditLogMapper;
import ua.lviv.bas.cinema.service.integration.audit.AuditQueryService;

@ExtendWith(MockitoExtension.class)
public class AdminAuditControllerTest {

	@Mock
	private AuditQueryService auditQueryService;

	@Mock
	private AuditLogMapper auditLogMapper;

	@InjectMocks
	private AdminAuditController adminAuditController;

	@Test
	void getAuditLogs_ShouldReturnPageOfAuditLogs() {
		Pageable pageable = PageRequest.of(0, 20);
		String entityType = "BonusRules";
		AuditAction action = AuditAction.UPDATED;
		String changedBy = "admin@example.com";

		AuditLog auditLog = createAuditLog(1L, entityType, 10L);
		AuditLogResponse response = createAuditLogResponse(1L, entityType);

		Page<AuditLog> auditLogPage = new PageImpl<>(List.of(auditLog), pageable, 1);

		when(auditQueryService.findByFilters(entityType, action, changedBy, pageable)).thenReturn(auditLogPage);
		when(auditLogMapper.toResponse(auditLog)).thenReturn(response);

		ResponseEntity<PageResponse<AuditLogResponse>> result = adminAuditController.getAuditLogs(entityType, action,
				changedBy, pageable);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().content()).hasSize(1);
		assertThat(result.getBody().content().get(0).id()).isEqualTo(1L);
		assertThat(result.getBody().content().get(0).entityType()).isEqualTo(entityType);
	}

	@Test
	void getAuditLogs_WithNullFilters_ShouldReturnAllAuditLogs() {
		Pageable pageable = PageRequest.of(0, 20);
		AuditLog auditLog = createAuditLog(1L, "User", 5L);
		AuditLogResponse response = createAuditLogResponse(1L, "User");

		Page<AuditLog> auditLogPage = new PageImpl<>(List.of(auditLog), pageable, 1);

		when(auditQueryService.findByFilters(null, null, null, pageable)).thenReturn(auditLogPage);
		when(auditLogMapper.toResponse(auditLog)).thenReturn(response);

		ResponseEntity<PageResponse<AuditLogResponse>> result = adminAuditController.getAuditLogs(null, null, null,
				pageable);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().content()).hasSize(1);
	}

	@Test
	void getAuditLogs_WhenNoLogs_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<AuditLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(auditQueryService.findByFilters(null, null, null, pageable)).thenReturn(emptyPage);

		ResponseEntity<PageResponse<AuditLogResponse>> result = adminAuditController.getAuditLogs(null, null, null,
				pageable);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().content()).isEmpty();
		assertThat(result.getBody().totalElements()).isZero();
	}

	@Test
	void getEntityHistory_WhenLogsExist_ShouldReturnList() {
		String entityType = "BonusRules";
		Long entityId = 10L;

		AuditLog auditLog1 = createAuditLog(1L, entityType, entityId);
		AuditLog auditLog2 = createAuditLog(2L, entityType, entityId);
		AuditLogResponse response1 = createAuditLogResponse(1L, entityType);
		AuditLogResponse response2 = createAuditLogResponse(2L, entityType);

		when(auditQueryService.findByEntityTypeAndEntityId(entityType, entityId))
				.thenReturn(List.of(auditLog1, auditLog2));
		when(auditLogMapper.toResponse(auditLog1)).thenReturn(response1);
		when(auditLogMapper.toResponse(auditLog2)).thenReturn(response2);

		ResponseEntity<List<AuditLogResponse>> result = adminAuditController.getEntityHistory(entityType, entityId);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody()).hasSize(2);
		assertThat(result.getBody().get(0).id()).isEqualTo(1L);
		assertThat(result.getBody().get(1).id()).isEqualTo(2L);
	}

	@Test
	void getEntityHistory_WhenNoLogs_ShouldReturnNotFound() {
		String entityType = "BonusRules";
		Long entityId = 999L;

		when(auditQueryService.findByEntityTypeAndEntityId(entityType, entityId)).thenReturn(List.of());

		ResponseEntity<List<AuditLogResponse>> result = adminAuditController.getEntityHistory(entityType, entityId);

		assertThat(result.getStatusCode().is4xxClientError()).isTrue();
		assertThat(result.getStatusCode().value()).isEqualTo(404);
		assertThat(result.getBody()).isNull();
	}

	@Test
	void getEntityHistory_WithDifferentEntityTypes_ShouldReturnCorrectLogs() {
		String entityType = "User";
		Long entityId = 5L;

		AuditLog auditLog = createAuditLog(1L, entityType, entityId);
		AuditLogResponse response = createAuditLogResponse(1L, entityType);

		when(auditQueryService.findByEntityTypeAndEntityId(entityType, entityId)).thenReturn(List.of(auditLog));
		when(auditLogMapper.toResponse(auditLog)).thenReturn(response);

		ResponseEntity<List<AuditLogResponse>> result = adminAuditController.getEntityHistory(entityType, entityId);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).hasSize(1);
		assertThat(result.getBody().get(0).entityType()).isEqualTo(entityType);
	}

	private AuditLog createAuditLog(Long id, String entityType, Long entityId) {
		return AuditLog.builder().id(id).entityType(entityType).entityId(entityId).targetInfo("target")
				.action(AuditAction.CREATED).changedBy("admin@example.com").changedAt(LocalDateTime.now()).build();
	}

	private AuditLogResponse createAuditLogResponse(Long id, String entityType) {
		return new AuditLogResponse(id, entityType, 10L, "target", AuditAction.CREATED, "admin@example.com",
				LocalDateTime.now(), List.of());
	}
}