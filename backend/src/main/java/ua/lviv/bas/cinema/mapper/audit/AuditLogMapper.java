package ua.lviv.bas.cinema.mapper.audit;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse.AuditLogDetailResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

	@Mapping(target = "details", source = "details")
	AuditLogResponse toResponse(AuditLog auditLog);

	AuditLogDetailResponse toDetailResponse(AuditLogDetail detail);
}