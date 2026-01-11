package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

	@Mapping(target = "data", ignore = true)
	@Mapping(target = "signature", ignore = true)
	@Mapping(target = "paymentUrl", ignore = true)
	PaymentLiqPayDataResponse toPaymentLiqPayDataResponse(Payment payment);
}