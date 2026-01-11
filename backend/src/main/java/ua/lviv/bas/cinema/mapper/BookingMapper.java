package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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
}