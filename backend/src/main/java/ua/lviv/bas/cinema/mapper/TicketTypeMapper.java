package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeUserProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketTypeMapper {

	@Mapping(target = "id", ignore = true)
	TicketType toTicketType(TicketTypeCreateRequest dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	void updateTicketTypeFromRequest(@MappingTarget TicketType entity, TicketTypeUpdateRequest dto);

	TicketTypeResponse toTicketTypeResponse(TicketType entity);

	TicketTypeResponse toTicketTypeResponse(TicketTypeAdminProjection projection);

	TicketTypeUserResponse toTicketTypeUserResponse(TicketType entity);

	TicketTypeUserResponse toTicketTypeUserResponse(TicketTypeUserProjection projection);
}