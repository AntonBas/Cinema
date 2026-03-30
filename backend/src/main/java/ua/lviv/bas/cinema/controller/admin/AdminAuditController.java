package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;
import ua.lviv.bas.cinema.mapper.audit.AuditLogMapper;
import ua.lviv.bas.cinema.repository.audit.AuditLogRepository;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

	private final AuditLogRepository auditLogRepository;
	private final AuditLogMapper auditLogMapper;

	@GetMapping
	public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
			@RequestParam(required = false) String entityType, @RequestParam(required = false) String action,
			@RequestParam(required = false) String changedBy,
			@PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<AuditLog> auditLogs = auditLogRepository.findByFilters(entityType, action, changedBy, pageable);
		Page<AuditLogResponse> responsePage = auditLogs.map(auditLogMapper::toResponse);

		return ResponseEntity.ok(PageResponse.from(responsePage));
	}

	@GetMapping("/entity/{entityType}/{entityId}")
	public ResponseEntity<List<AuditLogResponse>> getEntityHistory(@PathVariable String entityType,
			@PathVariable Long entityId) {

		List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType,
				entityId);
		List<AuditLogResponse> responses = auditLogs.stream().map(auditLogMapper::toResponse).toList();

		return ResponseEntity.ok(responses);
	}
}