package ua.lviv.bas.cinema.config.properties;

import ua.lviv.bas.cinema.common.CinemaTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefundRulesTest {

    private final RefundRules refundRules = new RefundRules();

    @Test
    void getRefundDeadlineShouldBeTwoHoursBeforeTheSession() {
        var sessionTime = CinemaTime.now().plusDays(3);

        var deadline = refundRules.getRefundDeadline(sessionTime);

        assertThat(deadline).isEqualTo(sessionTime.minusHours(2));
    }

    @Test
    void getRefundDeadlineShouldMatchTheCutoffEnforcedByGetRefundPercentage() {
        var justBeforeDeadline = CinemaTime.now().plusHours(2).plusMinutes(5);
        var justAfterDeadline = CinemaTime.now().plusHours(1).plusMinutes(55);

        assertThat(refundRules.isRefundable(justBeforeDeadline)).isTrue();
        assertThat(refundRules.isRefundable(justAfterDeadline)).isFalse();
    }
}
