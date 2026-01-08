package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;

@ExtendWith(MockitoExtension.class)
public class PaymentMapperTest {

	private PaymentMapper paymentMapper = new PaymentMapperImpl();

	@Test
	void toPaymentLiqPayDataResponse_ShouldMapLiqpayOrderId() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.PENDING).liqpayOrderId("ORDER_ABC123").liqpayPaymentId("lp_123456789")
				.paymentTime(LocalDateTime.of(2024, 1, 15, 14, 35)).createdAt(LocalDateTime.of(2024, 1, 15, 14, 30))
				.updatedAt(LocalDateTime.of(2024, 1, 15, 14, 35)).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertEquals("ORDER_ABC123", response.getLiqpayOrderId());

		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandleNullLiqpayOrderId() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.PENDING).liqpayOrderId(null).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertNull(response.getLiqpayOrderId());
		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandleEmptyLiqpayOrderId() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.PENDING).liqpayOrderId("").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertEquals("", response.getLiqpayOrderId());
		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandleNullPayment() {
		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(null);
		assertNull(response);
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandlePaymentWithoutBooking() {
		Payment payment = Payment.builder().id(1L).booking(null).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.PENDING).liqpayOrderId("ORDER_DEF456").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertEquals("ORDER_DEF456", response.getLiqpayOrderId());
		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandleDifferentPaymentStatuses() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment1 = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.PENDING).liqpayOrderId("ORDER_PENDING").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		Payment payment2 = Payment.builder().id(2L).booking(booking).amount(new BigDecimal("600.00"))
				.status(PaymentStatus.SUCCESS).liqpayOrderId("ORDER_SUCCESS").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		Payment payment3 = Payment.builder().id(3L).booking(booking).amount(new BigDecimal("700.00"))
				.status(PaymentStatus.FAILED).liqpayOrderId("ORDER_FAILED").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response1 = paymentMapper.toPaymentLiqPayDataResponse(payment1);
		PaymentLiqPayDataResponse response2 = paymentMapper.toPaymentLiqPayDataResponse(payment2);
		PaymentLiqPayDataResponse response3 = paymentMapper.toPaymentLiqPayDataResponse(payment3);

		assertNotNull(response1);
		assertEquals("ORDER_PENDING", response1.getLiqpayOrderId());

		assertNotNull(response2);
		assertEquals("ORDER_SUCCESS", response2.getLiqpayOrderId());

		assertNotNull(response3);
		assertEquals("ORDER_FAILED", response3.getLiqpayOrderId());

		assertNull(response1.getData());
		assertNull(response1.getSignature());
		assertNull(response1.getPaymentUrl());

		assertNull(response2.getData());
		assertNull(response2.getSignature());
		assertNull(response2.getPaymentUrl());

		assertNull(response3.getData());
		assertNull(response3.getSignature());
		assertNull(response3.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldHandlePaymentWithErrorData() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment = Payment.builder().id(1L).booking(booking).amount(new BigDecimal("500.00"))
				.status(PaymentStatus.FAILED).liqpayOrderId("ORDER_ERROR").liqpayErrorCode("error_validation")
				.liqpayErrorDescription("Invalid signature").liqpaySenderCardMask("****4832")
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertEquals("ORDER_ERROR", response.getLiqpayOrderId());
		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}

	@Test
	void toPaymentLiqPayDataResponse_ShouldIgnoreOtherPaymentFields() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Booking booking = Booking.builder().id(123L).user(user).build();

		Payment payment = Payment.builder().id(999L).booking(booking).amount(new BigDecimal("999.99"))
				.status(PaymentStatus.PROCESSING).liqpayOrderId("TEST_ORDER").liqpayPaymentId("lp_999999999")
				.liqpayTransactionId("txn_999999").paymentTime(LocalDateTime.of(2024, 1, 15, 15, 45))
				.liqpayErrorCode("test_error").liqpayErrorDescription("Test error description")
				.liqpaySenderCardMask("****9999").createdAt(LocalDateTime.of(2024, 1, 15, 15, 30))
				.updatedAt(LocalDateTime.of(2024, 1, 15, 15, 45)).build();

		PaymentLiqPayDataResponse response = paymentMapper.toPaymentLiqPayDataResponse(payment);

		assertNotNull(response);
		assertEquals("TEST_ORDER", response.getLiqpayOrderId());

		assertNull(response.getData());
		assertNull(response.getSignature());
		assertNull(response.getPaymentUrl());
	}
}