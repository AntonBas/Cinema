package ua.lviv.bas.cinema.refund.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.dto.response.AdminRefundListResponse;
import ua.lviv.bas.cinema.refund.dto.response.RefundPolicyRuleResponse;
import ua.lviv.bas.cinema.refund.dto.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.refund.dto.response.RefundResponse;
import ua.lviv.bas.cinema.refund.service.RefundCalculator;
import ua.lviv.bas.cinema.ticket.domain.Ticket;

import java.time.LocalDateTime;
import java.util.List;

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

    @Mapping(target = "refundNumber", expression = "java(NumberGeneratorService.generateRefundNumberStatic(refund))")
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

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "ticketCode", source = "ticket.uniqueCode")
    @Mapping(target = "movieTitle", source = "ticket.booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "ticket.booking.session.startTime")
    @Mapping(target = "hallName", source = "ticket.booking.session.hall.name")
    @Mapping(target = "originalPrice", source = "ticket.originalPrice")
    @Mapping(target = "finalPrice", source = "ticket.finalPrice")
    @Mapping(target = "refundAmount", source = "calculation.refundAmount")
    @Mapping(target = "refundPercentage", source = "calculation.percentage")
    @Mapping(target = "feeAmount", expression = "java(calculation.feeAmount())")
    @Mapping(target = "feePercentage", expression = "java(calculation.feePercentage())")
    @Mapping(target = "bonusPointsUsed", source = "calculation.bonusPointsUsed")
    @Mapping(target = "bonusPointsToRefund", source = "calculation.bonusPointsToRefund")
    @Mapping(target = "earnedPointsToRevoke", source = "calculation.earnedPointsToRevoke")
    @Mapping(target = "isRefundable", constant = "true")
    @Mapping(target = "nonRefundableReason", ignore = true)
    @Mapping(target = "purchaseTime", expression = "java(ticket.getPurchaseTime().toString())")
    @Mapping(target = "ticketType", source = "ticket.ticketType.displayName")
    RefundPreviewResponse toPreview(Ticket ticket, RefundCalculator.RefundCalculation calculation, String seatInfo,
                                    String policyName, String policyDescription, LocalDateTime refundDeadline,
                                    String remainingTime);

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "ticketCode", source = "ticket.uniqueCode")
    @Mapping(target = "movieTitle", source = "ticket.booking.session.movie.title")
    @Mapping(target = "sessionTime", source = "ticket.booking.session.startTime")
    @Mapping(target = "isRefundable", constant = "false")
    @Mapping(target = "hallName", ignore = true)
    @Mapping(target = "seatInfo", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(target = "refundAmount", ignore = true)
    @Mapping(target = "refundPercentage", ignore = true)
    @Mapping(target = "feeAmount", ignore = true)
    @Mapping(target = "feePercentage", ignore = true)
    @Mapping(target = "bonusPointsUsed", ignore = true)
    @Mapping(target = "bonusPointsToRefund", ignore = true)
    @Mapping(target = "policyName", ignore = true)
    @Mapping(target = "policyDescription", ignore = true)
    @Mapping(target = "refundDeadline", ignore = true)
    @Mapping(target = "remainingTime", ignore = true)
    @Mapping(target = "purchaseTime", ignore = true)
    @Mapping(target = "ticketType", ignore = true)
    @Mapping(target = "earnedPointsToRevoke", ignore = true)
    RefundPreviewResponse toNonRefundablePreview(Ticket ticket, String nonRefundableReason);

    @Mapping(target = "percentage", expression = "java(tier.percentage().intValue())")
    RefundPolicyRuleResponse toPolicyRule(RefundRules.Tier tier);

    List<RefundPolicyRuleResponse> toPolicyRules(List<RefundRules.Tier> tiers);
}
