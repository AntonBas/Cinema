package ua.lviv.bas.cinema.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingDetailsResponse;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingListResponse;
import ua.lviv.bas.cinema.booking.dto.response.BookingResponse;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.ticket.domain.Ticket;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, imports = {
        NumberGeneratorService.class })
public interface BookingMapper {

    @Mapping(target = "bookingNumber", expression = "java(NumberGeneratorService.generateBookingNumberStatic(booking))")
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "sessionPublicId", source = "session.publicId")
    @Mapping(target = "sessionTime", source = "session.startTime")
    @Mapping(target = "movieTitle", source = "session.movie.title")
    @Mapping(target = "hallName", source = "session.hall.name")
    @Mapping(target = "liqpayOrderId", source = "payment.liqpayOrderId")
    @Mapping(target = "seatReservations", source = "seatReservations")
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "seatId", source = "seat.id")
    @Mapping(target = "row", source = "seat.row")
    @Mapping(target = "seatNumber", source = "seat.number")
    @Mapping(target = "ticketTypeName", source = "ticketType.displayName")
    BookingResponse.SeatReservationInfo toSeatReservationInfo(SeatReservation seatReservation);

    @Mapping(target = "bookingNumber", expression = "java(NumberGeneratorService.generateBookingNumberStatic(booking))")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "movieTitle", source = "session.movie.title")
    @Mapping(target = "hallName", source = "session.hall.name")
    @Mapping(target = "sessionTime", source = "session.startTime")
    @Mapping(target = "paymentStatus", source = "payment.status")
    AdminBookingListResponse toAdminListResponse(Booking booking);

    @Mapping(target = "bookingNumber", expression = "java(NumberGeneratorService.generateBookingNumberStatic(booking))")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userFirstName", source = "user.firstName")
    @Mapping(target = "userLastName", source = "user.lastName")
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "movieTitle", source = "session.movie.title")
    @Mapping(target = "hallName", source = "session.hall.name")
    @Mapping(target = "sessionTime", source = "session.startTime")
    AdminBookingDetailsResponse toAdminDetailsResponse(Booking booking);

    @Mapping(target = "ticketCode", source = "uniqueCode")
    @Mapping(target = "ticketType", source = "ticketType.displayName")
    @Mapping(target = "row", source = "seatReservation.seat.row")
    @Mapping(target = "seatNumber", source = "seatReservation.seat.number")
    @Mapping(target = "price", source = "finalPrice")
    AdminBookingDetailsResponse.TicketInfo toAdminTicketInfo(Ticket ticket);

    @Mapping(target = "cardMask", source = "liqpaySenderCardMask")
    @Mapping(target = "errorCode", source = "liqpayErrorCode")
    @Mapping(target = "errorDescription", source = "liqpayErrorDescription")
    AdminBookingDetailsResponse.PaymentInfo toAdminPaymentInfo(Payment payment);
}
