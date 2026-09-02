package ua.lviv.bas.cinema.bonus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.dto.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.bonus.dto.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.bonus.mapper.BonusMapper;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusQueryService {

    private final BonusCardRepository bonusCardRepository;
    private final BonusRulesRepository bonusRulesRepository;
    private final BonusTransactionRepository bonusTransactionRepository;
    private final BonusMapper bonusMapper;
    private final BonusProperties bonusProperties;

    @Cacheable(value = "bonus", key = "'balance:' + #userId")
    @Transactional(readOnly = true)
    public BonusBalanceResponse getBalance(Long userId) {
        var card = getCardByUserId(userId);
        return buildBalanceResponse(card);
    }

    @Cacheable(value = "bonus", key = "'transactions:' + #userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<BonusTransactionResponse> getTransactions(Long userId, Pageable pageable) {
        var page = bonusTransactionRepository.findProjectionsByUserId(userId, pageable);
        return page.map(bonusMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Integer calculateAccrualPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        var ruleOpt = bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PAYMENT_ACCRUAL);
        if (ruleOpt.isEmpty() || ruleOpt.get().getMoneyRatio() == null) {
            return 0;
        }
        var rule = ruleOpt.get();
        int points = amount.multiply(rule.getMoneyRatio()).intValue();
        points = applyMinMaxLimits(points, rule.getMinPointsPerTransaction(), rule.getMaxPointsPerTransaction());
        return points;
    }

    @Transactional(readOnly = true)
    public void validatePointsForBooking(Long userId, Integer points, BigDecimal totalPrice) {
        validateRedemption(userId, points);
        var discount = bonusProperties.getPointValue().multiply(BigDecimal.valueOf(points));
        var maxDiscount = totalPrice.multiply(bonusProperties.getMaxDiscountPercentage());
        if (discount.compareTo(maxDiscount) > 0) {
            throw BonusValidationException.discountExceedsMax(discount, maxDiscount);
        }
    }

    @Transactional(readOnly = true)
    public void validateRedemption(Long userId, Integer points) {
        var card = getCardByUserId(userId);
        validatePositivePoints(points);
        if (card.getPointsBalance() < points) {
            throw new InsufficientPointsException(card.getPointsBalance(), points);
        }

        var spendRule = bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.BOOKING_SPEND);
        if (spendRule.isPresent()) {
            var rule = spendRule.get();
            if (rule.getMinPointsPerTransaction() != null && points < rule.getMinPointsPerTransaction()) {
                throw BonusValidationException.minPointsRequired(rule.getMinPointsPerTransaction());
            }
            if (rule.getMaxPointsPerTransaction() != null && points > rule.getMaxPointsPerTransaction()) {
                throw BonusValidationException.maxPointsExceeded(rule.getMaxPointsPerTransaction());
            }
        }
    }

    private BonusCard getCardByUserId(Long userId) {
        return bonusCardRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Bonus card", userId));
    }

    private BonusBalanceResponse buildBalanceResponse(BonusCard card) {
        var pointValue = bonusProperties.getPointValue();
        var balanceValue = pointValue.multiply(BigDecimal.valueOf(card.getPointsBalance()));
        var spendRuleOpt = bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.BOOKING_SPEND);

        Integer minPoints = spendRuleOpt.map(BonusRules::getMinPointsPerTransaction).orElse(null);
        Integer maxPoints = spendRuleOpt.map(BonusRules::getMaxPointsPerTransaction).orElse(null);
        BigDecimal minValue = calculateValue(pointValue, minPoints);
        BigDecimal maxValue = calculateValue(pointValue, maxPoints);

        return new BonusBalanceResponse(card.getPointsBalance(), pointValue, balanceValue, minPoints, maxPoints,
                minValue, maxValue);
    }

    private BigDecimal calculateValue(BigDecimal pointValue, Integer points) {
        return points != null && points > 0 ? pointValue.multiply(BigDecimal.valueOf(points)) : null;
    }

    private int applyMinMaxLimits(int points, Integer min, Integer max) {
        if (min != null && points < min)
            return min;
        if (max != null && points > max)
            return max;
        return points;
    }

    private void validatePositivePoints(Integer points) {
        if (points == null || points <= 0) {
            throw BonusValidationException.invalidPoints(points);
        }
    }
}
