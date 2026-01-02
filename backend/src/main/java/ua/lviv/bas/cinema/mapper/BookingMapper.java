package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.dto.booking.response.BookingSummaryResponse;

@Mapper(componentModel = "spring")
public interface BookingMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "bookingNumber", ignore = true)
	@Mapping(target = "status", source = "status")
	@Mapping(target = "totalPrice", ignore = true)
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "expiresAt", source = "expiresAt")
	@Mapping(target = "session", source = "session")
	@Mapping(target = "bookedSeats", source = "bookedSeats")
	@Mapping(target = "payment", source = "payment")
	BookingResponse toBookingResponse(Booking booking);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "startTime", source = "startTime")
	@Mapping(target = "hallName", source = "hall.name")
	BookingResponse.SessionInfo toSessionInfo(ua.lviv.bas.cinema.domain.Session session);

	@Mapping(target = "seatId", source = "seat.id")
	@Mapping(target = "row", source = "seat.row")
	@Mapping(target = "seatNumber", source = "seat.number")
	@Mapping(target = "ticketType", source = "ticketType.displayName")
	@Mapping(target = "price", ignore = true)
	BookingResponse.BookedSeatInfo toBookedSeatInfo(BookedSeat bookedSeat);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "bookingNumber", ignore = true)
	@Mapping(target = "status", source = "status")
	@Mapping(target = "movieTitle", source = "session.movie.title")
	@Mapping(target = "sessionTime", source = "session.startTime")
	@Mapping(target = "hallName", source = "session.hall.name")
	@Mapping(target = "seatsCount", expression = "java(booking.getBookedSeats().size())")
	@Mapping(target = "totalPrice", ignore = true)
	@Mapping(target = "canCancel", ignore = true)
	BookingSummaryResponse toBookingSummaryResponse(Booking booking);

	List<BookingResponse> toBookingResponseList(List<Booking> bookings);

	List<BookingSummaryResponse> toBookingSummaryResponseList(List<Booking> bookings);
}