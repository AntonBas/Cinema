package ua.lviv.bas.cinema.mapper.promotion;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PromotionMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	Promotion toPromotion(PromotionCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updatePromotionFromRequest(@MappingTarget Promotion promotion, PromotionUpdateRequest request);

	PromotionResponse toPromotionResponse(Promotion promotion);

	PromotionResponse toPromotionResponse(PromotionResponseProjection projection);

	PromotionAdminResponse toPromotionAdminResponse(PromotionAdminProjection projection);
}