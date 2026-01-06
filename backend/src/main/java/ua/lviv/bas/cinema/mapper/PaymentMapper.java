package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

	@Mapping(target = "data", ignore = true)
	@Mapping(target = "signature", ignore = true)
	@Mapping(target = "paymentUrl", ignore = true)
	@Mapping(target = "liqpayOrderId", source = "liqpayOrderId")
	PaymentLiqPayDataResponse toPaymentLiqPayDataResponse(Payment payment);
}