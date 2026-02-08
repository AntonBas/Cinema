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
import ua.lviv.bas.cinema.domain.projection.BonusTransactionProjection;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BonusMapper {

	@Mapping(target = "userId", source = "user.id")
	BonusCardResponse toBonusCardResponse(BonusCard bonusCard);

	BonusRulesResponse toBonusRulesResponse(BonusRules rules);

	@Mapping(target = "typeDisplay", ignore = true)
	@Mapping(target = "pointsChange", ignore = true)
	@Mapping(target = "newBalance", ignore = true)
	@Mapping(target = "bookingDetails", ignore = true)
	BonusTransactionResponse toBonusTransactionResponse(BonusTransaction transaction);

	@Mapping(target = "bookingDetails", ignore = true)
	BonusTransactionResponse toBonusTransactionResponse(BonusTransactionProjection projection);

	@Mapping(target = "movieTitle", source = "movieTitle")
	@Mapping(target = "bookingReference", source = "bookingReference")
	@Mapping(target = "cinemaHall", source = "cinemaHall")
	@Mapping(target = "sessionDateTime", source = "sessionDateTime")
	BonusTransactionResponse.BookingDetails toBookingDetails(BonusTransactionProjection projection);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateBonusRulesFromRequest(BonusRulesRequest request, @MappingTarget BonusRules rules);
}