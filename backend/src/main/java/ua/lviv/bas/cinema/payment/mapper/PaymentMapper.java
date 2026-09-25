package ua.lviv.bas.cinema.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.dto.response.PaymentResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN,
        imports = NumberGeneratorService.class)
public interface PaymentMapper {

    @Mapping(target = "bookingNumber",
            expression = "java(NumberGeneratorService.generateBookingNumberStatic(payment.getBooking()))")
    @Mapping(target = "movieTitle", source = "booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "booking.session.startTime")
    @Mapping(target = "hallName", source = "booking.session.hall.name")
    @Mapping(target = "finalAmount", source = "amount")
    @Mapping(target = "expiresAt", source = "booking.expiresAt")
    @Mapping(target = "senderCardMask", source = "liqpaySenderCardMask")
    @Mapping(target = "errorDescription", source = "liqpayErrorDescription")
    PaymentResponse toResponse(Payment payment);
}
