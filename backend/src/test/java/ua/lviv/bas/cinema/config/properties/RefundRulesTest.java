package ua.lviv.bas.cinema.config.properties;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefundRulesTest {

    private final RefundRules refundRules = new RefundRules();

    @Test
    void getRefundDeadlineShouldBeTwoHoursBeforeTheSession() {
        var sessionTime = LocalDateTime.now().plusDays(3);

        var deadline = refundRules.getRefundDeadline(sessionTime);

        assertThat(deadline).isEqualTo(sessionTime.minusHours(2));
    }

    @Test
    void getRefundDeadlineShouldMatchTheCutoffEnforcedByGetRefundPercentage() {
        var justBeforeDeadline = LocalDateTime.now().plusHours(2).plusMinutes(5);
        var justAfterDeadline = LocalDateTime.now().plusHours(1).plusMinutes(55);

        assertThat(refundRules.isRefundable(justBeforeDeadline)).isTrue();
        assertThat(refundRules.isRefundable(justAfterDeadline)).isFalse();
    }
}
