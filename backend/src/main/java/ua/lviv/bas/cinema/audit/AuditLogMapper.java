package ua.lviv.bas.cinema.audit;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.audit.dto.AuditLogResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}