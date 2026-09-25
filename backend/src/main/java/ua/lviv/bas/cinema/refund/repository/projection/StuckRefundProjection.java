package ua.lviv.bas.cinema.refund.repository.projection;

import java.math.BigDecimal;
import java.time.Instant;

public interface StuckRefundProjection {

    Long getRefundId();

    Long getTicketId();

    String getLiqpayOrderId();

    Long getPaymentId();

    BigDecimal getRefundAmount();

    BigDecimal getPaymentAmount();

    Instant getCreatedDate();
}
