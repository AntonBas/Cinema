package ua.lviv.bas.cinema.promotion.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.promotion.domain.Promotion;
import ua.lviv.bas.cinema.promotion.domain.PromotionStatus;
import ua.lviv.bas.cinema.promotion.dto.request.PromotionRequest;
import ua.lviv.bas.cinema.promotion.dto.response.PromotionListResponse;
import ua.lviv.bas.cinema.promotion.dto.response.PromotionResponse;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionListProjection;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionResponseProjection;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PromotionMapper {

	default PromotionStatus resolveStatus(LocalDate startDate, LocalDate endDate) {
		return PromotionStatus.of(startDate, endDate);
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	Promotion toEntity(PromotionRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userRedemptions", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updateEntity(PromotionRequest request, @MappingTarget Promotion promotion);

	@Mapping(target = "status", expression = "java(resolveStatus(promotion.getStartDate(), promotion.getEndDate()))")
	PromotionResponse toPromotionResponse(Promotion promotion);

	@Mapping(target = "status", expression = "java(resolveStatus(projection.getStartDate(), projection.getEndDate()))")
	PromotionResponse toPromotionResponse(PromotionResponseProjection projection);

	@Mapping(target = "status", expression = "java(resolveStatus(projection.getStartDate(), projection.getEndDate()))")
	PromotionListResponse toPromotionListResponse(PromotionListProjection projection);
}