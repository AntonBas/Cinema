package ua.lviv.bas.cinema.refund.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.refund.domain.RefundItem;
import ua.lviv.bas.cinema.refund.dto.response.RefundItemResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RefundItemMapper {

	@Mapping(target = "ticketCode", source = "ticket.uniqueCode")
	@Mapping(target = "ticketId", source = "ticket.id")
	RefundItemResponse toResponse(RefundItem refundItem);
}