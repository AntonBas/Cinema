package ua.lviv.bas.cinema.mapper.bonus;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.repository.bonus.projection.BonusTransactionProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BonusMapper {

	@Mapping(target = "userId", source = "user.id")
	BonusCardResponse toBonusCardResponse(BonusCard bonusCard);

	BonusRulesResponse toBonusRulesResponse(BonusRules rules);

	BonusTransactionResponse toBonusTransactionResponse(BonusTransactionProjection projection);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateBonusRulesFromRequest(BonusRulesRequest request, @MappingTarget BonusRules rules);
}