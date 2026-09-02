package ua.lviv.bas.cinema.bonus.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.dto.request.BonusRulesRequest;
import ua.lviv.bas.cinema.bonus.dto.response.BonusRulesResponse;
import ua.lviv.bas.cinema.bonus.dto.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.bonus.repository.projection.BonusTransactionProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BonusMapper {

	BonusRulesResponse toResponse(BonusRules rules);

	@Mapping(target = "pointsChange", expression = "java(projection.getPointsChange())")
	BonusTransactionResponse toResponse(BonusTransactionProjection projection);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "bonusType", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updateFromRequest(BonusRulesRequest request, @MappingTarget BonusRules rules);
}