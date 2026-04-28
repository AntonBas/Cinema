package ua.lviv.bas.cinema.mapper.ticket;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
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

    @Mapping(target = "ticketType", source = "ticketType.displayName")
    @Mapping(target = "requiresDocument", source = "ticketType.requiresDocument")
    @Mapping(target = "documentType", source = "ticketType.documentType")
    @Mapping(target = "movieTitle", source = "booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "booking.session.startTime")
    @Mapping(target = "hallName", source = "booking.session.hall.name")
    @Mapping(target = "seatRow", source = "seatReservation.seat.row")
    @Mapping(target = "seatNumber", source = "seatReservation.seat.number")
    @Mapping(target = "userEmail", source = "user.email")
    TicketCashierResponse toTicketCashierResponse(Ticket ticket);
}