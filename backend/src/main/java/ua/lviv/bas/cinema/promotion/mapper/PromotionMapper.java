package ua.lviv.bas.cinema.promotion.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.promotion.domain.Promotion;
import ua.lviv.bas.cinema.promotion.dto.request.PromotionRequest;
import ua.lviv.bas.cinema.promotion.dto.response.PromotionListResponse;
import ua.lviv.bas.cinema.promotion.dto.response.PromotionResponse;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionListProjection;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionResponseProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PromotionMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	Promotion toPromotion(PromotionRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updatePromotionFromRequest(PromotionRequest request, @MappingTarget Promotion promotion);

	PromotionResponse toPromotionResponse(Promotion promotion);

	PromotionResponse toPromotionResponse(PromotionResponseProjection projection);

	PromotionListResponse toPromotionListResponse(PromotionListProjection projection);
}