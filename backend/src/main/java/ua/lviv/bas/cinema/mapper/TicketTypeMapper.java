package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	TicketType toEntity(TicketTypeCreateRequest dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "code", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "priceMultiplier", source = "priceModifier") // Головне - цей мапінг
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntity(@MappingTarget TicketType entity, TicketTypeUpdateRequest dto);

	TicketTypeResponse toResponseDto(TicketType entity);

	TicketTypeSimpleResponse toSimpleDto(TicketType entity);

	List<TicketTypeResponse> toResponseDtoList(List<TicketType> entities);

	List<TicketTypeSimpleResponse> toSimpleDtoList(List<TicketType> entities);
}