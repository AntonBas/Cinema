package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse.TicketTypeInfo;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {

	@Mapping(target = "id", source = "ticketType.id")
	@Mapping(target = "name", source = "ticketType.displayName")
	@Mapping(target = "multiplier", source = "ticketType.priceMultiplier")
	TicketTypeInfo toTicketTypeInfo(TicketType ticketType);

	List<TicketTypeInfo> toTicketTypeInfoList(List<TicketType> ticketTypes);
}