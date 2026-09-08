package ua.lviv.bas.cinema.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.dto.response.BookingResponse;
import ua.lviv.bas.cinema.common.NumberGeneratorService;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, imports = {
		NumberGeneratorService.class })
public interface BookingMapper {

	@Mapping(target = "bookingNumber", expression = "java(NumberGeneratorService.generateBookingNumberStatic(booking))")
	@Mapping(target = "sessionId", source = "session.id")
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
}