package ua.lviv.bas.cinema.config.properties;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class RefundRules {

    private static final BigDecimal FULL_REFUND = new BigDecimal("100.00");
    private static final BigDecimal STANDARD_REFUND = new BigDecimal("85.00");
    private static final BigDecimal LAST_MINUTE_REFUND = new BigDecimal("50.00");
    private static final BigDecimal NO_REFUND = BigDecimal.ZERO;

    private static final long FULL_REFUND_MINUTES = 48 * 60L;
    private static final long STANDARD_REFUND_MINUTES = 24 * 60L;
    private static final long MINIMUM_REFUND_MINUTES = 2 * 60L;

    public BigDecimal getRefundPercentage(LocalDateTime sessionTime) {
        long minutesBefore = minutesUntilSession(sessionTime);

        if (minutesBefore >= FULL_REFUND_MINUTES) {
            return FULL_REFUND;
        } else if (minutesBefore >= STANDARD_REFUND_MINUTES) {
            return STANDARD_REFUND;
        } else if (minutesBefore >= MINIMUM_REFUND_MINUTES) {
            return LAST_MINUTE_REFUND;
        } else {
            return NO_REFUND;
        }
    }

    public boolean isRefundable(LocalDateTime sessionTime) {
        return getRefundPercentage(sessionTime).compareTo(BigDecimal.ZERO) > 0;
    }

    public String getPolicyName(LocalDateTime sessionTime) {
        long minutesBefore = minutesUntilSession(sessionTime);

        if (minutesBefore >= FULL_REFUND_MINUTES) {
            return "Full Refund";
        } else if (minutesBefore >= STANDARD_REFUND_MINUTES) {
            return "Standard Refund";
        } else if (minutesBefore >= MINIMUM_REFUND_MINUTES) {
            return "Last Minute Refund";
        } else {
            return "No Refund";
        }
    }

    public String getPolicyDescription(LocalDateTime sessionTime) {
        long minutesBefore = minutesUntilSession(sessionTime);

        if (minutesBefore >= FULL_REFUND_MINUTES) {
            return "100% refund — 48+ hours before the session";
        } else if (minutesBefore >= STANDARD_REFUND_MINUTES) {
            return "85% refund — 24-48 hours before the session";
        } else if (minutesBefore >= MINIMUM_REFUND_MINUTES) {
            return "50% refund — 2-24 hours before the session";
        } else {
            return "Refund not available — less than 2 hours before the session";
        }
    }

    private long minutesUntilSession(LocalDateTime sessionTime) {
        return Duration.between(LocalDateTime.now(), sessionTime).toMinutes();
    }
}