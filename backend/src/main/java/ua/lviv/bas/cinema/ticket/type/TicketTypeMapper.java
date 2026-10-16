package ua.lviv.bas.cinema.ticket.type;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.ticket.type.dto.TicketTypeRequest;
import ua.lviv.bas.cinema.ticket.type.dto.TicketTypeResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TicketTypeMapper {

    @Mapping(target = "id", ignore = true)
    TicketType toEntity(TicketTypeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(TicketTypeRequest request, @MappingTarget TicketType ticketType);

    TicketTypeResponse toResponse(TicketType ticketType);

    TicketTypeResponse toResponse(TicketTypeProjection projection);
}