package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;

@Mapper(componentModel = "spring")
public interface SeatAvailabilityMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "row", source = "row")
	@Mapping(target = "seatNumber", source = "number")
	@Mapping(target = "seatType", source = "seatType")
	@Mapping(target = "available", ignore = true)
	@Mapping(target = "temporarilyReserved", ignore = true)
	@Mapping(target = "ticketPrices", ignore = true)
	SeatAvailabilityResponse.SeatInfo toSeatAvailabilityInfo(Seat seat);
}