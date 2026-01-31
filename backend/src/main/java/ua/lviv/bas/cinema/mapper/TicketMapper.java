package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

	@Mapping(target = "ticketCode", source = "uniqueCode")
	@Mapping(target = "qrCodeUrl", ignore = true)
	@Mapping(target = "purchaseTime", source = "purchaseTime")
	@Mapping(target = "price", source = "finalPrice")
	@Mapping(target = "ticketType", source = "ticketType.displayName")
	@Mapping(target = "movieTitle", source = "booking.session.movie.title")
	@Mapping(target = "sessionTime", source = "booking.session.startTime")
	@Mapping(target = "hallName", source = "booking.session.hall.name")
	@Mapping(target = "row", source = "bookedSeat.seat.row")
	@Mapping(target = "seatNumber", source = "bookedSeat.seat.number")
	TicketResponse toTicketResponse(Ticket ticket);
}