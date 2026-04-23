package ua.lviv.bas.cinema.config.properties;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class RefundRules {

    private static final BigDecimal FULL_REFUND = new BigDecimal("100.00");
    private static final BigDecimal STANDARD_REFUND = new BigDecimal("90.00");
    private static final BigDecimal LAST_MINUTE_REFUND = new BigDecimal("50.00");
    private static final BigDecimal NO_REFUND = BigDecimal.ZERO;

    private static final int FULL_REFUND_HOURS = 24;
    private static final int STANDARD_REFUND_HOURS = 2;
    private static final int MINIMUM_REFUND_HOURS = 0;

    public BigDecimal getRefundPercentage(LocalDateTime sessionTime) {
        long hoursBefore = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);

        if (hoursBefore > FULL_REFUND_HOURS) {
            return FULL_REFUND;
        } else if (hoursBefore > STANDARD_REFUND_HOURS) {
            return STANDARD_REFUND;
        } else if (hoursBefore > MINIMUM_REFUND_HOURS) {
            return LAST_MINUTE_REFUND;
        } else {
            return NO_REFUND;
        }
    }

    public boolean isRefundable(LocalDateTime sessionTime) {
        return getRefundPercentage(sessionTime).compareTo(BigDecimal.ZERO) > 0;
    }

    public String getPolicyName(LocalDateTime sessionTime) {
        long hoursBefore = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);

        if (hoursBefore > FULL_REFUND_HOURS) {
            return "Full Refund";
        } else if (hoursBefore > STANDARD_REFUND_HOURS) {
            return "Standard Refund";
        } else if (hoursBefore > MINIMUM_REFUND_HOURS) {
            return "Last Minute Refund";
        } else {
            return "No Refund";
        }
    }

    public String getPolicyDescription(LocalDateTime sessionTime) {
        long hoursBefore = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);

        if (hoursBefore > FULL_REFUND_HOURS) {
            return "100% refund up to 24 hours before the session";
        } else if (hoursBefore > STANDARD_REFUND_HOURS) {
            return "90% refund 2-24 hours before the session";
        } else if (hoursBefore > MINIMUM_REFUND_HOURS) {
            return "50% refund less than 2 hours before the session";
        } else {
            return "Refund not available";
        }
    }
}