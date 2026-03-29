package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;

@Mapper(componentModel = "spring", uses = RefundItemMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundMapper {

	@Mapping(target = "refundNumber", ignore = true)
	@Mapping(target = "paymentMethod", ignore = true)
	@Mapping(target = "message", ignore = true)
	@Mapping(target = "estimatedRefundTime", ignore = true)
	@Mapping(target = "paymentId", source = "payment.id")
	RefundResponse toRefundResponse(Refund refund);
}