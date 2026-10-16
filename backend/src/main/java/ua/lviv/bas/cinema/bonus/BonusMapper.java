package ua.lviv.bas.cinema.bonus;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.bonus.rules.BonusRuleSpec;
import ua.lviv.bas.cinema.bonus.rules.BonusRules;
import ua.lviv.bas.cinema.bonus.rules.dto.BonusRulesRequest;
import ua.lviv.bas.cinema.bonus.rules.dto.BonusRulesResponse;
import ua.lviv.bas.cinema.bonus.dto.BonusTransactionResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, imports = BonusRuleSpec.class)
public interface BonusMapper {

    @Mapping(target = "requiredFields", expression = "java(BonusRuleSpec.requiredFieldsOf(rules.getBonusType()))")
    @Mapping(target = "optionalFields", expression = "java(BonusRuleSpec.optionalFieldsOf(rules.getBonusType()))")
    BonusRulesResponse toResponse(BonusRules rules);

    @Mapping(target = "pointsChange", expression = "java(projection.getPointsChange())")
    BonusTransactionResponse toResponse(BonusTransactionProjection projection);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bonusType", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(BonusRulesRequest request, @MappingTarget BonusRules rules);
}