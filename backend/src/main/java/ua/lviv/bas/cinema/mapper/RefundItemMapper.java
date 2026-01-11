package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.RefundItem;
import ua.lviv.bas.cinema.dto.refund.response.RefundItemResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundItemMapper {

	@Mapping(target = "ticketCode", source = "ticket.uniqueCode")
	@Mapping(target = "ticketId", source = "ticket.id")
	RefundItemResponse toRefundItemResponse(RefundItem refundItem);
}