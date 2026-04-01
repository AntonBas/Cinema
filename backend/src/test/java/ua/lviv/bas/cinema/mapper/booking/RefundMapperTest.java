package ua.lviv.bas.cinema.mapper.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;

@ExtendWith(MockitoExtension.class)
public class RefundMapperTest {

	@Mock
	private RefundItemMapper refundItemMapper;

	@InjectMocks
	private RefundMapperImpl refundMapper;

	@Test
	void toRefundResponse_ShouldMapAllFieldsCorrectly() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Payment payment = Payment.builder().id(100L).amount(new BigDecimal("500.00")).build();

		Refund refund = Refund.builder().id(50L).user(user).payment(payment).totalAmount(new BigDecimal("400.00"))
				.totalBonusPointsToDeduct(20).reason("Changed my mind").status(RefundStatus.APPROVED).build();

		refund.setCreatedDate(LocalDateTime.of(2024, 1, 15, 14, 30));
		refund.setLastModifiedDate(LocalDateTime.of(2024, 1, 15, 15, 0));

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(50L);
		assertThat(response.paymentId()).isEqualTo(100L);
		assertThat(response.totalAmount()).isEqualTo(new BigDecimal("400.00"));
		assertThat(response.totalBonusPointsToDeduct()).isEqualTo(20);
		assertThat(response.reason()).isEqualTo("Changed my mind");
		assertThat(response.status()).isEqualTo("APPROVED");
		assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));
		assertThat(response.processedAt()).isNull();

		assertThat(response.refundNumber()).isNull();
		assertThat(response.paymentMethod()).isNull();
		assertThat(response.message()).isNull();
		assertThat(response.estimatedRefundTime()).isNull();
	}

	@Test
	void toRefundResponse_ShouldHandleNullPayment() {
		User user = User.builder().id(1L).email("user@example.com").build();

		Refund refund = Refund.builder().id(51L).user(user).payment(null).totalAmount(new BigDecimal("200.00"))
				.totalBonusPointsToDeduct(0).status(RefundStatus.PENDING).build();

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(51L);
		assertThat(response.paymentId()).isNull();
		assertThat(response.totalAmount()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.totalBonusPointsToDeduct()).isZero();
		assertThat(response.status()).isEqualTo("PENDING");
	}

	@Test
	void toRefundResponse_ShouldHandleNullReason() {
		User user = User.builder().id(1L).email("user@example.com").build();

		Payment payment = Payment.builder().id(103L).build();

		Refund refund = Refund.builder().id(54L).user(user).payment(payment).totalAmount(new BigDecimal("300.00"))
				.totalBonusPointsToDeduct(15).reason(null).status(RefundStatus.PENDING).build();

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response).isNotNull();
		assertThat(response.reason()).isNull();
		assertThat(response.totalBonusPointsToDeduct()).isEqualTo(15);
		assertThat(response.status()).isEqualTo("PENDING");
	}

	@Test
	void toRefundResponse_ShouldHandleZeroTotalAmount() {
		User user = User.builder().id(1L).email("user@example.com").build();

		Payment payment = Payment.builder().id(104L).build();

		Refund refund = Refund.builder().id(55L).user(user).payment(payment).totalAmount(BigDecimal.ZERO)
				.totalBonusPointsToDeduct(0).status(RefundStatus.PROCESSED).build();

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response.totalAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(response.totalBonusPointsToDeduct()).isZero();
		assertThat(response.status()).isEqualTo("PROCESSED");
	}

	@Test
	void toRefundResponse_ShouldHandleNullDates() {
		User user = User.builder().id(1L).email("user@example.com").build();

		Payment payment = Payment.builder().id(105L).build();

		Refund refund = Refund.builder().id(56L).user(user).payment(payment).totalAmount(new BigDecimal("250.00"))
				.totalBonusPointsToDeduct(5).status(RefundStatus.PENDING).build();

		refund.setCreatedDate(null);
		refund.setLastModifiedDate(null);

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response.createdAt()).isNull();
		assertThat(response.processedAt()).isNull();
		assertThat(response.totalBonusPointsToDeduct()).isEqualTo(5);
		assertThat(response.status()).isEqualTo("PENDING");
	}

	@Test
	void toRefundResponse_ShouldMapAllRefundStatuses() {
		User user = User.builder().id(1L).email("user@example.com").build();

		Payment payment = Payment.builder().id(106L).build();

		Refund pendingRefund = Refund.builder().id(57L).user(user).payment(payment)
				.totalAmount(new BigDecimal("100.00")).totalBonusPointsToDeduct(10).status(RefundStatus.PENDING)
				.build();

		Refund approvedRefund = Refund.builder().id(58L).user(user).payment(payment)
				.totalAmount(new BigDecimal("100.00")).totalBonusPointsToDeduct(10).status(RefundStatus.APPROVED)
				.build();

		Refund rejectedRefund = Refund.builder().id(59L).user(user).payment(payment)
				.totalAmount(new BigDecimal("100.00")).totalBonusPointsToDeduct(10).status(RefundStatus.REJECTED)
				.build();

		Refund processedRefund = Refund.builder().id(60L).user(user).payment(payment)
				.totalAmount(new BigDecimal("100.00")).totalBonusPointsToDeduct(10).status(RefundStatus.PROCESSED)
				.build();

		assertThat(refundMapper.toRefundResponse(pendingRefund).status()).isEqualTo("PENDING");
		assertThat(refundMapper.toRefundResponse(approvedRefund).status()).isEqualTo("APPROVED");
		assertThat(refundMapper.toRefundResponse(rejectedRefund).status()).isEqualTo("REJECTED");
		assertThat(refundMapper.toRefundResponse(processedRefund).status()).isEqualTo("PROCESSED");
	}

	@Test
	void toRefundResponse_ShouldHandleNullInput() {
		RefundResponse response = refundMapper.toRefundResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toRefundResponse_ShouldMapRefundWithoutBuilder() {
		User user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");

		Payment payment = new Payment();
		payment.setId(107L);

		Refund refund = new Refund();
		refund.setId(61L);
		refund.setUser(user);
		refund.setPayment(payment);
		refund.setTotalAmount(new BigDecimal("350.00"));
		refund.setTotalBonusPointsToDeduct(25);
		refund.setReason("Technical issues");
		refund.setStatus(RefundStatus.APPROVED);
		refund.setCreatedDate(LocalDateTime.of(2024, 1, 17, 9, 0));
		refund.setLastModifiedDate(LocalDateTime.of(2024, 1, 17, 10, 0));

		RefundResponse response = refundMapper.toRefundResponse(refund);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(61L);
		assertThat(response.paymentId()).isEqualTo(107L);
		assertThat(response.totalAmount()).isEqualTo(new BigDecimal("350.00"));
		assertThat(response.totalBonusPointsToDeduct()).isEqualTo(25);
		assertThat(response.reason()).isEqualTo("Technical issues");
		assertThat(response.status()).isEqualTo("APPROVED");
		assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 17, 9, 0));
		assertThat(response.processedAt()).isNull();
	}
}