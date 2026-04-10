package ua.lviv.bas.cinema.mapper.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;

@Mapper(componentModel = "spring", uses = RefundItemMapper.class, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RefundMapper {

	@Mapping(target = "refundNumber", ignore = true)
	@Mapping(target = "paymentMethod", ignore = true)
	@Mapping(target = "message", ignore = true)
	@Mapping(target = "estimatedRefundTime", ignore = true)
	@Mapping(target = "paymentId", source = "payment.id")
	@Mapping(target = "processedBy", ignore = true)
	@Mapping(target = "processedAt", ignore = true)
	@Mapping(target = "createdAt", source = "createdDate")
	RefundResponse toResponse(Refund refund);
}