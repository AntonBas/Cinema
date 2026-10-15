package ua.lviv.bas.cinema.config.properties;

import ua.lviv.bas.cinema.common.CinemaTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RefundRules {

    private static final long MINIMUM_REFUND_MINUTES = 2 * 60L;

    private static final List<Tier> TIERS = List.of(
            new Tier("Full Refund", "100% refund — 48+ hours before the session",
                    "More than 48 hours before session start", new BigDecimal("100.00"), 48 * 60L),
            new Tier("Standard Refund", "85% refund — 24-48 hours before the session",
                    "24 to 48 hours before session start", new BigDecimal("85.00"), 24 * 60L),
            new Tier("Last Minute Refund", "50% refund — 2-24 hours before the session",
                    "2 to 24 hours before session start", new BigDecimal("50.00"), MINIMUM_REFUND_MINUTES),
            new Tier("No Refund", "Refund not available — less than 2 hours before the session",
                    "Less than 2 hours before session start", BigDecimal.ZERO, Long.MIN_VALUE));

    public List<Tier> getTiers() {
        return TIERS;
    }

    public BigDecimal getRefundPercentage(LocalDateTime sessionTime) {
        return tierFor(sessionTime).percentage();
    }

    public boolean isRefundable(LocalDateTime sessionTime) {
        return getRefundPercentage(sessionTime).compareTo(BigDecimal.ZERO) > 0;
    }

    public LocalDateTime getRefundDeadline(LocalDateTime sessionTime) {
        return sessionTime.minusMinutes(MINIMUM_REFUND_MINUTES);
    }

    public String getPolicyName(LocalDateTime sessionTime) {
        return tierFor(sessionTime).name();
    }

    public String getPolicyDescription(LocalDateTime sessionTime) {
        return tierFor(sessionTime).description();
    }

    private Tier tierFor(LocalDateTime sessionTime) {
        long minutesBefore = Duration.between(CinemaTime.now(), sessionTime).toMinutes();
        return TIERS.stream().filter(tier -> minutesBefore >= tier.minMinutesBefore()).findFirst()
                .orElseThrow();
    }

    public record Tier(String name, String description, String condition, BigDecimal percentage,
                       long minMinutesBefore) {
    }
}
