package ua.lviv.bas.cinema.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatReservationMapper {

	@Mapping(target = "sessionId", source = "session.id")
	@Mapping(target = "movieTitle", source = "session.movie.title")
	@Mapping(target = "basePrice", source = "session.basePrice")
	@Mapping(target = "hallName", source = "session.hall.name")
	@Mapping(target = "availableSeats", source = "availableSeatsCount")
	@Mapping(target = "seats", source = "seatInfos")
	SeatReservationResponse toResponse(Session session, List<SeatReservationResponse.SeatInfo> seatInfos,
			int availableSeatsCount);

	@Mapping(target = "id", source = "seat.id")
	@Mapping(target = "row", source = "seat.row")
	@Mapping(target = "seatNumber", source = "seat.number")
	@Mapping(target = "seatType", source = "seat.seatType")
	@Mapping(target = "available", source = "available")
	@Mapping(target = "temporarilyReserved", source = "temporarilyReserved")
	@Mapping(target = "active", source = "seat.active")
	@Mapping(target = "ticketPrices", source = "ticketPrices")
	SeatReservationResponse.SeatInfo toSeatInfo(Seat seat, Boolean available, Boolean temporarilyReserved,
			List<SeatReservationResponse.TicketPriceInfo> ticketPrices);

	@Mapping(target = "ticketTypeId", source = "ticketType.id")
	@Mapping(target = "ticketTypeName", source = "ticketType.displayName")
	@Mapping(target = "finalPrice", source = "price")
	SeatReservationResponse.TicketPriceInfo toTicketPriceInfo(TicketType ticketType, BigDecimal price);
}