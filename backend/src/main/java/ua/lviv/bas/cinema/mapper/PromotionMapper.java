package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.domain.projection.UserPromotionResponseProjection;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	Promotion toPromotion(PromotionCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updatePromotionFromRequest(@MappingTarget Promotion promotion, PromotionUpdateRequest request);

	PromotionResponse toPromotionResponse(Promotion promotion);

	PromotionResponse toPromotionResponse(PromotionResponseProjection projection);

	List<PromotionResponse> toPromotionResponseList(List<Promotion> promotions);

	List<PromotionResponse> toPromotionResponseListFromProjections(List<PromotionResponseProjection> projections);

	@Mapping(target = "promotionId", source = "promotion.id")
	@Mapping(target = "promotionTitle", source = "promotion.title")
	@Mapping(target = "claimedAt", source = "redeemedAt")
	UserPromotionResponse toUserPromotionResponse(UserPromotion userPromotion);

	@Mapping(target = "claimedAt", source = "claimedAt")
	UserPromotionResponse toUserPromotionResponse(UserPromotionResponseProjection projection);

	List<UserPromotionResponse> toUserPromotionResponseList(List<UserPromotion> userPromotions);

	List<UserPromotionResponse> toUserPromotionResponseListFromProjections(
			List<UserPromotionResponseProjection> projections);
}