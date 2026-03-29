package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.mapper.booking.PaymentMapper;

public class PaymentMapperTest {

	private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

	@Test
	void toPaymentLiqPayDataResponse_ShouldMapLiqpayOrderId() {
		Payment payment = Payment.builder().id(1L).liqpayOrderId("ORDER_ABC123").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response = mapper.toPaymentLiqPayDataResponse(payment);

		assertThat(response).isNotNull();
		assertThat(response.liqpayOrderId()).isEqualTo("ORDER_ABC123");
		assertThat(response.data()).isNull();
		assertThat(response.signature()).isNull();
		assertThat(response.paymentUrl()).isNull();
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandleNullPayment() {
		PaymentLiqPayDataResponse response = mapper.toPaymentLiqPayDataResponse(null);
		assertThat(response).isNull();
	}
}