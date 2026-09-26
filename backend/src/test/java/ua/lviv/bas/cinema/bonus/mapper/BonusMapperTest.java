package ua.lviv.bas.cinema.bonus.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import ua.lviv.bas.cinema.bonus.domain.BonusRuleField;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.dto.request.BonusRulesRequest;
import ua.lviv.bas.cinema.bonus.repository.projection.BonusTransactionProjection;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class BonusMapperTest {

    private final BonusMapper mapper = Mappers.getMapper(BonusMapper.class);

    @Test
    void toResponseFromBonusRules() {
        var rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100).active(true)
                .build();

        var response = mapper.toResponse(rules);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.bonusType()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
        assertThat(response.points()).isEqualTo(100);
        assertThat(response.active()).isTrue();
        assertThat(response.requiredFields()).containsExactly(BonusRuleField.POINTS);
        assertThat(response.optionalFields()).isEmpty();
    }

    @Test
    void toResponseShouldExposeFieldsOfPaymentAccrualRule() {
        var rules = BonusRules.builder().id(4L).bonusType(BonusTransactionType.PAYMENT_ACCRUAL)
                .moneyRatio(new BigDecimal("0.05")).active(true).build();

        var response = mapper.toResponse(rules);

        assertThat(response.requiredFields()).containsExactly(BonusRuleField.MONEY_RATIO);
        assertThat(response.optionalFields()).containsExactly(BonusRuleField.MIN_POINTS, BonusRuleField.MAX_POINTS);
    }

    @Test
    void toResponseShouldExposeNoFieldsForNonConfigurableRule() {
        var rules = BonusRules.builder().id(5L).bonusType(BonusTransactionType.REFUND_RETURN).active(true).build();

        var response = mapper.toResponse(rules);

        assertThat(response.requiredFields()).isEmpty();
        assertThat(response.optionalFields()).isEmpty();
    }

    @Test
    void toResponseFromNullBonusRules() {
        var response = mapper.toResponse((BonusRules) null);
        assertThat(response).isNull();
    }

    @Test
    void toResponseFromProjection() {
        var projection = Mockito.mock(BonusTransactionProjection.class);
        Mockito.when(projection.getId()).thenReturn(1L);
        Mockito.when(projection.getType()).thenReturn("PAYMENT_ACCRUAL");
        Mockito.when(projection.getPointsChangeRaw()).thenReturn(50);
        Mockito.when(projection.getPointsChange()).thenReturn("+50");
        Mockito.when(projection.getNewBalance()).thenReturn(150);
        Mockito.when(projection.getCreatedAt()).thenReturn(Instant.now());

        var response = mapper.toResponse(projection);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(BonusTransactionType.PAYMENT_ACCRUAL);
        assertThat(response.pointsChange()).isEqualTo("+50");
        assertThat(response.newBalance()).isEqualTo(150);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void toResponseFromNullProjection() {
        var response = mapper.toResponse((BonusTransactionProjection) null);
        assertThat(response).isNull();
    }

    @Test
    void updateEntity() {
        var existing = BonusRules.builder().points(0).active(true).build();
        var request = new BonusRulesRequest(150, null, null, null, false);

        mapper.updateEntity(request, existing);

        assertThat(existing.getPoints()).isEqualTo(150);
        assertThat(existing.getActive()).isFalse();
    }

    @Test
    void updateEntityShouldClearFieldsMissingFromRequest() {
        var existing = BonusRules.builder().points(100).moneyRatio(new BigDecimal("0.05")).minPointsPerTransaction(10)
                .maxPointsPerTransaction(500).active(true).build();

        var request = new BonusRulesRequest(null, new BigDecimal("0.10"), 20, null, true);

        mapper.updateEntity(request, existing);

        assertThat(existing.getPoints()).isNull();
        assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
        assertThat(existing.getMinPointsPerTransaction()).isEqualTo(20);
        assertThat(existing.getMaxPointsPerTransaction()).isNull();
        assertThat(existing.getActive()).isTrue();
    }

    @Test
    void updateEntityWithAllFields() {
        var existing = BonusRules.builder().build();
        var request = new BonusRulesRequest(200, new BigDecimal("0.10"), 50, 1000, false);

        mapper.updateEntity(request, existing);

        assertThat(existing.getPoints()).isEqualTo(200);
        assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
        assertThat(existing.getMinPointsPerTransaction()).isEqualTo(50);
        assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(1000);
        assertThat(existing.getActive()).isFalse();
    }

    @Test
    void updateEntityWithNullRequest() {
        var existing = BonusRules.builder().points(100).active(true).build();

        mapper.updateEntity(null, existing);

        assertThat(existing.getPoints()).isEqualTo(100);
        assertThat(existing.getActive()).isTrue();
    }
}