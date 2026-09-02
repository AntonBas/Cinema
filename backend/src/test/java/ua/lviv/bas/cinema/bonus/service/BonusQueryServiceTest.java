package ua.lviv.bas.cinema.bonus.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.bonus.mapper.BonusMapper;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BonusQueryServiceTest {

    @Mock
    private BonusCardRepository bonusCardRepository;
    @Mock
    private BonusRulesRepository bonusRulesRepository;
    @Mock
    private BonusTransactionRepository bonusTransactionRepository;
    @Mock
    private BonusMapper bonusMapper;
    @Mock
    private BonusProperties bonusProperties;
    @InjectMocks
    private BonusQueryService bonusQueryService;

    private final Long USER_ID = 1L;
    private final BonusTransactionType PAYMENT_TYPE = BonusTransactionType.PAYMENT_ACCRUAL;
    private final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;

    @Test
    void getBalanceShouldReturnBalance() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();
        BonusRules spendRule = BonusRules.builder().minPointsPerTransaction(10).maxPointsPerTransaction(50).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(SPEND)).thenReturn(Optional.of(spendRule));

        var result = bonusQueryService.getBalance(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.pointsBalance()).isEqualTo(100);
        assertThat(result.pointValue()).isEqualTo(new BigDecimal("1.00"));
        assertThat(result.balanceValue()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.minUsablePoints()).isEqualTo(10);
        assertThat(result.maxUsablePoints()).isEqualTo(50);
    }

    @Test
    void getBalanceShouldThrowExceptionWhenCardNotFound() {
        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bonusQueryService.getBalance(USER_ID)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validateRedemptionWhenSufficientPointsShouldPass() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        bonusQueryService.validateRedemption(USER_ID, 50);
    }

    @Test
    void validateRedemptionWhenInsufficientPointsShouldThrowException() {
        BonusCard card = BonusCard.builder().pointsBalance(30).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> bonusQueryService.validateRedemption(USER_ID, 50))
                .isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    void calculateAccrualPointsShouldCalculateCorrectly() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("1.5")).minPointsPerTransaction(10)
                .maxPointsPerTransaction(500).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusQueryService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(150);
    }

    @Test
    void calculateAccrualPointsShouldApplyMinLimit() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("0.1")).minPointsPerTransaction(50).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusQueryService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(50);
    }

    @Test
    void calculateAccrualPointsShouldApplyMaxLimit() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("10.0")).maxPointsPerTransaction(500).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusQueryService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(500);
    }

    @Test
    void calculateAccrualPointsWhenNoRuleShouldReturnZero() {
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.empty());

        Integer result = bonusQueryService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isZero();
    }

    @Test
    void calculateAccrualPointsWhenAmountNullShouldReturnZero() {
        Integer result = bonusQueryService.calculateAccrualPoints(null);

        assertThat(result).isZero();
    }

    @Test
    void calculateAccrualPointsWhenAmountZeroShouldReturnZero() {
        Integer result = bonusQueryService.calculateAccrualPoints(BigDecimal.ZERO);

        assertThat(result).isZero();
    }

    @Test
    void validatePointsForBookingShouldSucceed() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

        bonusQueryService.validatePointsForBooking(USER_ID, 30, new BigDecimal("100"));
    }

    @Test
    void validatePointsForBookingWhenDiscountExceedsShouldThrowException() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

        assertThatThrownBy(() -> bonusQueryService.validatePointsForBooking(USER_ID, 60, new BigDecimal("100")))
                .isInstanceOf(BonusValidationException.class);
    }
}
