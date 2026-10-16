package ua.lviv.bas.cinema.promotion;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.promotion.Promotion;
import ua.lviv.bas.cinema.promotion.PromotionStatus;
import ua.lviv.bas.cinema.promotion.dto.PromotionRequest;
import ua.lviv.bas.cinema.promotion.dto.PromotionListResponse;
import ua.lviv.bas.cinema.promotion.dto.PromotionResponse;
import ua.lviv.bas.cinema.promotion.PromotionListProjection;
import ua.lviv.bas.cinema.promotion.PromotionResponseProjection;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PromotionMapper {

    default PromotionStatus resolveStatus(boolean active, LocalDate startDate, LocalDate endDate) {
        return PromotionStatus.of(active, startDate, endDate);
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

    @Mapping(target = "status", expression = "java(resolveStatus(promotion.isActive(), promotion.getStartDate(), promotion.getEndDate()))")
    PromotionResponse toPromotionResponse(Promotion promotion);

    @Mapping(target = "status", expression = "java(resolveStatus(projection.getActive(), projection.getStartDate(), projection.getEndDate()))")
    PromotionResponse toPromotionResponse(PromotionResponseProjection projection);

    @Mapping(target = "status", expression = "java(resolveStatus(projection.getActive(), projection.getStartDate(), projection.getEndDate()))")
    PromotionListResponse toPromotionListResponse(PromotionListProjection projection);
}