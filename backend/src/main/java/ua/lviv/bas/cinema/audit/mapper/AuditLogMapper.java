package ua.lviv.bas.cinema.audit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.audit.domain.AuditLog;
import ua.lviv.bas.cinema.audit.dto.AuditLogResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AuditLogMapper {

	AuditLogResponse toResponse(AuditLog auditLog);
}