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

    @Mapping(target = "id", source = "refund.id")
    @Mapping(target = "status", source = "refund.status")
    @Mapping(target = "totalAmount", source = "refund.totalAmount")
    @Mapping(target = "totalBonusPointsToDeduct", source = "refund.totalBonusPointsToDeduct")
    @Mapping(target = "reason", source = "refund.reason")
    @Mapping(target = "items", source = "refund.items")
    @Mapping(target = "paymentId", source = "refund.payment.id")
    @Mapping(target = "createdAt", source = "refund.createdDate")
    @Mapping(target = "paymentMethod", constant = "CARD")
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    RefundResponse toResponse(Refund refund, String refundNumber, String message, String estimatedRefundTime);

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
