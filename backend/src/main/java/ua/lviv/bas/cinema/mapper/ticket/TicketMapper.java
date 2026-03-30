package ua.lviv.bas.cinema.mapper.ticket;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketInfoProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

	@Mapping(target = "ticketCode", source = "uniqueCode")
	@Mapping(target = "price", source = "finalPrice")
	@Mapping(target = "ticketType", source = "ticketType.displayName")
	@Mapping(target = "movieTitle", source = "booking.session.movie.title")
	@Mapping(target = "sessionTime", source = "booking.session.startTime")
	@Mapping(target = "hallName", source = "booking.session.hall.name")
	@Mapping(target = "row", source = "seatReservation.seat.row")
	@Mapping(target = "seatNumber", source = "seatReservation.seat.number")
	@Mapping(target = "qrCodeUrl", ignore = true)
	TicketResponse toTicketResponse(Ticket ticket);

	@Mapping(target = "ticketCode", source = "uniqueCode")
	@Mapping(target = "price", source = "finalPrice")
	@Mapping(target = "ticketType", source = "ticketTypeName")
	@Mapping(target = "movieTitle", source = "movieTitle")
	@Mapping(target = "sessionTime", source = "sessionStartTime")
	@Mapping(target = "hallName", source = "hallName")
	@Mapping(target = "row", source = "row")
	@Mapping(target = "seatNumber", source = "seatNumber")
	@Mapping(target = "qrCodeUrl", ignore = true)
	TicketResponse toTicketResponse(TicketInfoProjection projection);
}