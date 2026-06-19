package ua.lviv.bas.cinema.mapper.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.dto.refund.response.RefundItemResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RefundItemMapper {

	@Mapping(target = "ticketCode", source = "ticket.uniqueCode")
	@Mapping(target = "ticketId", source = "ticket.id")
	RefundItemResponse toResponse(RefundItem refundItem);
}