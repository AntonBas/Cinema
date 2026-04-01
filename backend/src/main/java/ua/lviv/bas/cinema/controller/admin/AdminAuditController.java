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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;
import ua.lviv.bas.cinema.mapper.audit.AuditLogMapper;
import ua.lviv.bas.cinema.service.integration.audit.AuditQueryService;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Admin", description = "API for administrative audit log management")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

	private final AuditQueryService auditQueryService;
	private final AuditLogMapper auditLogMapper;

	@Operation(summary = "Get audit logs", description = "Returns a paginated list of audit logs with optional filters")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@GetMapping
	public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
			@Parameter(description = "Filter by entity type (e.g., User, BonusRules)") @RequestParam(required = false) String entityType,
			@Parameter(description = "Filter by audit action (CREATED, UPDATED, DELETED, etc.)") @RequestParam(required = false) AuditAction action,
			@Parameter(description = "Filter by user who performed the action") @RequestParam(required = false) String changedBy,
			@PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<AuditLog> auditLogs = auditQueryService.findByFilters(entityType, action, changedBy, pageable);
		Page<AuditLogResponse> responsePage = auditLogs.map(auditLogMapper::toResponse);

		return ResponseEntity.ok(PageResponse.from(responsePage));
	}

	@Operation(summary = "Get entity history", description = "Returns audit history for a specific entity")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Entity history retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No audit logs found for this entity"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@GetMapping("/entity/{entityType}/{entityId}")
	public ResponseEntity<List<AuditLogResponse>> getEntityHistory(
			@Parameter(description = "Entity type (e.g., User, BonusRules)", required = true) @PathVariable String entityType,
			@Parameter(description = "Entity ID", required = true) @PathVariable Long entityId) {

		List<AuditLog> auditLogs = auditQueryService.findByEntityTypeAndEntityId(entityType, entityId);

		if (auditLogs.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<AuditLogResponse> responses = auditLogs.stream().map(auditLogMapper::toResponse).toList();

		return ResponseEntity.ok(responses);
	}
}