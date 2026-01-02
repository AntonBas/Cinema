package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "bookingId", source = "booking.id")
	@Mapping(target = "amount", source = "amount")
	@Mapping(target = "bonusPointsUsed", source = "bonusPointsUsed")
	@Mapping(target = "finalAmount", ignore = true)
	@Mapping(target = "paymentMethod", source = "paymentMethod")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "paymentUrl", ignore = true)
	@Mapping(target = "createdAt", source = "createdAt")
	PaymentResponse toPaymentResponse(Payment payment);

	List<PaymentResponse> toPaymentResponseList(List<Payment> payments);
}