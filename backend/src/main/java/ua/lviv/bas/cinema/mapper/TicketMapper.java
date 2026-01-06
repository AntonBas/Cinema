package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

@Mapper(componentModel = "spring")
public interface TicketMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "ticketCode", source = "uniqueCode")
	@Mapping(target = "qrCodeUrl", ignore = true)
	@Mapping(target = "status", source = "status")
	@Mapping(target = "purchaseTime", source = "purchaseTime")
	@Mapping(target = "price", source = "finalPrice")
	@Mapping(target = "ticketType", source = "ticketType.displayName")
	@Mapping(target = "movieTitle", source = "booking.session.movie.title")
	@Mapping(target = "sessionTime", source = "booking.session.startTime")
	@Mapping(target = "hallName", source = "booking.session.hall.name")
	@Mapping(target = "row", ignore = true)
	@Mapping(target = "seatNumber", ignore = true)
	TicketResponse toTicketResponse(Ticket ticket);

	List<TicketResponse> toTicketResponseList(List<Ticket> tickets);
}