package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;

@Mapper(componentModel = "spring", uses = RefundItemMapper.class)
public interface RefundMapper {

	@Mapping(target = "refundNumber", ignore = true)
	@Mapping(target = "paymentMethod", ignore = true)
	@Mapping(target = "message", ignore = true)
	@Mapping(target = "estimatedRefundTime", ignore = true)
	@Mapping(target = "paymentId", source = "payment.id")
	RefundResponse toResponse(Refund refund);
}