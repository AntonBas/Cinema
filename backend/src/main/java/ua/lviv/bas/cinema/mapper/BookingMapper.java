package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

	@Mapping(target = "bookingNumber", ignore = true)
	@Mapping(target = "sessionTime", source = "session.startTime")
	@Mapping(target = "movieTitle", source = "session.movie.title")
	@Mapping(target = "hallName", source = "session.hall.name")
	@Mapping(target = "paymentStatus", source = "payment.status")
	@Mapping(target = "liqpayOrderId", source = "payment.liqpayOrderId")
	@Mapping(target = "bookedSeats", source = "bookedSeats")
	BookingResponse toBookingResponse(Booking booking);

	@Mapping(target = "seatId", source = "seat.id")
	@Mapping(target = "row", source = "seat.row")
	@Mapping(target = "seatNumber", source = "seat.number")
	@Mapping(target = "ticketTypeName", source = "ticketType.displayName")
	BookingResponse.BookedSeatInfo toBookedSeatInfo(BookedSeat bookedSeat);

	List<BookingResponse.BookedSeatInfo> toBookedSeatInfoList(List<BookedSeat> bookedSeats);

	List<BookingResponse> toBookingResponseList(List<Booking> bookings);
}