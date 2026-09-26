package ua.lviv.bas.cinema.ticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.dto.response.TicketCashierResponse;
import ua.lviv.bas.cinema.ticket.dto.response.TicketResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TicketMapper {

    @Mapping(target = "id", source = "ticket.id")
    @Mapping(target = "status", source = "ticket.status")
    @Mapping(target = "purchaseTime", source = "ticket.purchaseTime")
    @Mapping(target = "ticketCode", source = "ticket.uniqueCode")
    @Mapping(target = "price", source = "ticket.finalPrice")
    @Mapping(target = "ticketType", source = "ticket.ticketType.displayName")
    @Mapping(target = "movieTitle", source = "ticket.booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "ticket.booking.session.startTime")
    @Mapping(target = "hallName", source = "ticket.booking.session.hall.name")
    @Mapping(target = "row", source = "ticket.seatReservation.seat.row")
    @Mapping(target = "seatNumber", source = "ticket.seatReservation.seat.number")
    TicketResponse toTicketResponse(Ticket ticket, String qrCodeUrl, boolean refundable);

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