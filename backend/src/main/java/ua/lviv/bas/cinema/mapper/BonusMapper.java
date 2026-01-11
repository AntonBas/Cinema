package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BonusMapper {

	@Mapping(target = "userId", source = "user.id")
	BonusCardResponse toBonusCardResponse(BonusCard bonusCard);

	@Mapping(target = "newBalance", expression = "java(transaction.getBonusCard() != null ? transaction.getBonusCard().getPointsBalance() : null)")
	BonusTransactionResponse toBonusTransactionResponse(BonusTransaction transaction);

	BonusRulesResponse toBonusRulesResponse(BonusRules rules);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateBonusRulesFromRequest(BonusRulesRequest request, @MappingTarget BonusRules rules);
}