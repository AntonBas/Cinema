package ua.lviv.bas.cinema.mapper.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PaymentMapper {

	@Mapping(target = "data", ignore = true)
	@Mapping(target = "signature", ignore = true)
	@Mapping(target = "paymentUrl", ignore = true)
	PaymentLiqPayDataResponse toPaymentLiqPayDataResponse(Payment payment);
}