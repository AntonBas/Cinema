package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;

@Mapper(componentModel = "spring")
public interface BookingMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "bookingNumber", ignore = true)
	@Mapping(target = "status", source = "status")
	@Mapping(target = "sessionTime", source = "session.startTime")
	@Mapping(target = "movieTitle", source = "session.movie.title")
	@Mapping(target = "hallName", source = "session.hall.name")
	@Mapping(target = "totalPrice", source = "totalPrice")
	@Mapping(target = "bonusPointsUsed", source = "bonusPointsUsed")
	@Mapping(target = "bonusDiscountAmount", source = "bonusDiscountAmount")
	@Mapping(target = "finalPrice", source = "finalPrice")
	@Mapping(target = "paymentStatus", source = "payment.status")
	@Mapping(target = "liqpayOrderId", source = "payment.liqpayOrderId")
	@Mapping(target = "expiresAt", source = "expiresAt")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "bookedSeats", source = "bookedSeats")
	BookingResponse toBookingResponse(Booking booking);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "seatId", source = "seat.id")
	@Mapping(target = "row", source = "seat.row")
	@Mapping(target = "seatNumber", source = "seat.number")
	@Mapping(target = "ticketTypeName", source = "ticketType.displayName")
	@Mapping(target = "seatPrice", source = "seatPrice")
	BookingResponse.BookedSeatInfo toBookedSeatInfo(BookedSeat bookedSeat);

	List<BookingResponse.BookedSeatInfo> toBookedSeatInfoList(List<BookedSeat> bookedSeats);

	List<BookingResponse> toBookingResponseList(List<Booking> bookings);
}