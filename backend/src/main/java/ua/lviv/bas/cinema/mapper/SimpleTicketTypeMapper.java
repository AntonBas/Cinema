package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse.TicketTypeInfo;

@Mapper(componentModel = "spring")
public interface SimpleTicketTypeMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "name", source = "displayName")
	@Mapping(target = "multiplier", source = "priceMultiplier")
	@Mapping(target = "description", source = "description")
	TicketTypeInfo toTicketTypeInfo(TicketType ticketType);

	List<TicketTypeInfo> toTicketTypeInfoList(List<TicketType> ticketTypes);
}