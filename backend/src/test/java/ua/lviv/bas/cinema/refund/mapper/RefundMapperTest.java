package ua.lviv.bas.cinema.refund.mapper;

import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.user.domain.User;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class RefundMapperTest {

    private final RefundMapper refundMapper = new RefundMapperImpl();

    @Test
    void toResponse() {
        var refund = createRefund(50L, 100L, "400.00", 20, "Changed my mind", RefundStatus.PROCESSING,
                Instant.parse("2024-01-15T14:30:00Z"), Instant.parse("2024-01-15T15:00:00Z"));

        var response = refundMapper.toResponse(refund, "RF-2024-000050", "Refund is being processed", "3-5 business days");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.paymentId()).isEqualTo(100L);
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("400.00"));
        assertThat(response.totalBonusPointsToDeduct()).isEqualTo(20);
        assertThat(response.reason()).isEqualTo("Changed my mind");
        assertThat(response.status()).isEqualTo("PROCESSING");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2024-01-15T14:30:00Z"));
        assertThat(response.processedAt()).isNull();
        assertThat(response.refundNumber()).isEqualTo("RF-2024-000050");
        assertThat(response.paymentMethod()).isEqualTo("CARD");
        assertThat(response.message()).isEqualTo("Refund is being processed");
        assertThat(response.estimatedRefundTime()).isEqualTo("3-5 business days");
    }

    @Test
    void toResponseWithNullPayment() {
        var refund = createRefund(51L, null, "200.00", 0, null, RefundStatus.PROCESSING, null, null);

        var response = refundMapper.toResponse(refund, "RF-2024-000050", "Refund is being processed", "3-5 business days");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(51L);
        assertThat(response.paymentId()).isNull();
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(response.totalBonusPointsToDeduct()).isZero();
        assertThat(response.status()).isEqualTo("PROCESSING");
    }

    @Test
    void toResponseWithNullReason() {
        var refund = createRefund(54L, 103L, "300.00", 15, null, RefundStatus.PROCESSING, null, null);

        var response = refundMapper.toResponse(refund, "RF-2024-000050", "Refund is being processed", "3-5 business days");

        assertThat(response).isNotNull();
        assertThat(response.reason()).isNull();
        assertThat(response.totalBonusPointsToDeduct()).isEqualTo(15);
        assertThat(response.status()).isEqualTo("PROCESSING");
    }

    @Test
    void toResponseWithZeroTotalAmount() {
        var refund = createRefund(55L, 104L, "0.00", 0, null, RefundStatus.PROCESSED, null, null);

        var response = refundMapper.toResponse(refund, "RF-2024-000050", "Refund is being processed", "3-5 business days");

        assertThat(response.totalBonusPointsToDeduct()).isEqualTo(0);
        assertThat(response.totalBonusPointsToDeduct()).isZero();
        assertThat(response.status()).isEqualTo("PROCESSED");
    }

    @Test
    void toResponseWithNullDates() {
        var refund = createRefund(56L, 105L, "250.00", 5, null, RefundStatus.PROCESSING, null, null);

        var response = refundMapper.toResponse(refund, "RF-2024-000050", "Refund is being processed", "3-5 business days");

        assertThat(response.createdAt()).isNull();
        assertThat(response.processedAt()).isNull();
        assertThat(response.totalBonusPointsToDeduct()).isEqualTo(5);
        assertThat(response.status()).isEqualTo("PROCESSING");
    }

    @Test
    void toResponseWithAllStatuses() {
        assertThat(refundMapper.toResponse(
                createRefund(57L, 106L, "100.00", 10, null, RefundStatus.PROCESSING, null, null), null, null, null).status())
                .isEqualTo("PROCESSING");
        assertThat(refundMapper.toResponse(
                createRefund(58L, 106L, "100.00", 10, null, RefundStatus.PROCESSING, null, null), null, null, null).status())
                .isEqualTo("PROCESSING");
        assertThat(refundMapper.toResponse(
                createRefund(59L, 106L, "100.00", 10, null, RefundStatus.REJECTED, null, null), null, null, null).status())
                .isEqualTo("REJECTED");
        assertThat(refundMapper.toResponse(
                createRefund(60L, 106L, "100.00", 10, null, RefundStatus.PROCESSED, null, null), null, null, null).status())
                .isEqualTo("PROCESSED");
    }

    @Test
    void toResponseWithNull() {
        var response = refundMapper.toResponse(null, null, null, null);
        assertThat(response).isNull();
    }

    private Refund createRefund(Long id, Long paymentId, String amount, Integer bonusPointsToDeduct,
                                String reason, RefundStatus status, Instant createdDate,
                                Instant lastModifiedDate) {
        var user = User.builder().id(1L).email("user@example.com").build();
        var payment = paymentId != null ? Payment.builder().id(paymentId).build() : null;
        var refund = Refund.builder().id(id).user(user).payment(payment)
                .totalAmount(new BigDecimal(amount)).totalBonusPointsToDeduct(bonusPointsToDeduct)
                .reason(reason).status(status).build();
        refund.setCreatedDate(createdDate);
        refund.setLastModifiedDate(lastModifiedDate);
        return refund;
    }
}