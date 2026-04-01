package ua.lviv.bas.cinema.mapper.audit;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AuditLogMapper {

	AuditLogResponse toResponse(AuditLog auditLog);
}