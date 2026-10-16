package ua.lviv.bas.cinema.refund.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.refund.item.RefundItem;
import ua.lviv.bas.cinema.refund.item.dto.RefundItemResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RefundItemMapper {

    @Mapping(target = "ticketCode", source = "ticket.uniqueCode")
    @Mapping(target = "ticketId", source = "ticket.id")
    RefundItemResponse toResponse(RefundItem refundItem);
}