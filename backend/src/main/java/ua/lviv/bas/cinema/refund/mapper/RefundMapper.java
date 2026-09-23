package ua.lviv.bas.cinema.refund.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.dto.response.AdminRefundListResponse;
import ua.lviv.bas.cinema.refund.dto.response.RefundResponse;

@Mapper(componentModel = "spring", uses = RefundItemMapper.class, unmappedTargetPolicy = ReportingPolicy.WARN,
        imports = NumberGeneratorService.class)
public interface RefundMapper {

    @Mapping(target = "refundNumber", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "estimatedRefundTime", ignore = true)
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", source = "createdDate")
    RefundResponse toResponse(Refund refund);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "bookingId", source = "payment.booking.id")
    @Mapping(target = "bookingNumber",
            expression = "java(NumberGeneratorService.generateBookingNumberStatic(refund.getPayment().getBooking()))")
    @Mapping(target = "movieTitle", source = "payment.booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "payment.booking.session.startTime")
    @Mapping(target = "ticketCode", source = "ticket.uniqueCode")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "liqpayOrderId", source = "payment.liqpayOrderId")
    AdminRefundListResponse toAdminListResponse(Refund refund);
}
