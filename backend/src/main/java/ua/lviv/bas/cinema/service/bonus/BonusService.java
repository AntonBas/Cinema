package ua.lviv.bas.cinema.service.bonus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.mapper.bonus.BonusMapper;
import ua.lviv.bas.cinema.repository.bonus.BonusCardRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusTransactionRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusCardRepository bonusCardRepository;
    private final BonusRulesRepository bonusRulesRepository;
    private final BonusTransactionRepository bonusTransactionRepository;
    private final BonusMapper bonusMapper;
    private final BonusProperties bonusProperties;
    private final AuditService auditService;

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

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void awardWelcomeBonus(User user) {
        var card = getOrCreateCard(user);
        if (card.isWelcomeBonusReceived()) {
            return;
        }
        var rule = getActiveRule(BonusTransactionType.WELCOME_BONUS);
        addPointsToCard(card, rule.getPoints());
        createTransaction(card, rule.getPoints(), BonusTransactionType.WELCOME_BONUS, "USER_" + user.getId());
        card.setWelcomeBonusReceived(true);
        bonusCardRepository.save(card);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void awardBirthdayBonus(User user) {
        if (!canReceiveBirthdayBonus(user)) {
            return;
        }
        var today = LocalDate.now();
        var card = getOrCreateCard(user);
        if (alreadyReceivedBirthdayBonus(card, today)) {
            return;
        }
        var rule = getActiveRule(BonusTransactionType.BIRTHDAY_BONUS);
        addPointsToCard(card, rule.getPoints());
        createTransaction(card, rule.getPoints(), BonusTransactionType.BIRTHDAY_BONUS, "USER_" + user.getId());
        card.setLastBirthdayBonusDate(today);
        bonusCardRepository.save(card);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void addPromotionPoints(User user, Integer points, String promotionTitle) {
        validatePositivePoints(points);
        var card = getOrCreateCard(user);
        addPointsToCard(card, points);
        createTransaction(card, points, BonusTransactionType.PROMOTION_BONUS, "PROMOTION_" + promotionTitle);
        auditPointsAdded(card, user, points, promotionTitle);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void spendPoints(Long userId, Integer points, Booking booking) {
        validateRedemption(userId, points);
        var card = getCardByUserId(userId);
        int oldBalance = card.getPointsBalance();
        subtractPointsFromCard(card, points);
        bonusCardRepository.save(card);
        auditPointsSpent(card, booking, oldBalance);
        createTransaction(card, -points, BonusTransactionType.BOOKING_SPEND, "BOOKING_" + booking.getId(),
                booking);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void accruePointsForPayment(Long userId, Integer points, Booking booking, Payment payment) {
        if (points == null || points <= 0) {
            return;
        }
        var card = getCardByUserId(userId);
        addPointsToCard(card, points);
        bonusCardRepository.save(card);
        auditPointsAccrued(card, payment, points);
        createTransaction(card, points, BonusTransactionType.PAYMENT_ACCRUAL, "PAYMENT_" + payment.getId(),
                booking);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void refundPoints(Booking booking) {
        if (booking.getBonusPointsUsed() == null || booking.getBonusPointsUsed() <= 0) {
            return;
        }
        var card = getCardByUserId(booking.getUser().getId());
        var points = booking.getBonusPointsUsed();
        int oldBalance = card.getPointsBalance();
        addPointsToCard(card, points);
        bonusCardRepository.save(card);
        auditPointsRefunded(card, booking, oldBalance);
        createTransaction(card, points, BonusTransactionType.REFUND_RETURN, "REFUND_BOOKING_" + booking.getId(),
                booking);
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
    }

    public BonusCard getOrCreateCard(User user) {
        return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> createBonusCard(user));
    }

    public void createTransaction(BonusCard card, Integer points, BonusTransactionType type,
                                  String referenceId) {
        createTransaction(card, points, type, referenceId, null);
    }

    private BonusCard createBonusCard(User user) {
        var card = BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build();
        return bonusCardRepository.save(card);
    }

    private BonusCard getCardByUserId(Long userId) {
        return bonusCardRepository.findByUserId(userId).orElseThrow(() -> new BonusCardNotFoundException(userId));
    }

    private BonusRules getActiveRule(BonusTransactionType type) {
        return bonusRulesRepository.findByBonusTypeAndActiveTrue(type)
                .orElseThrow(() -> new BonusRuleNotFoundException(type));
    }

    private void addPointsToCard(BonusCard card, Integer points) {
        card.setPointsBalance(card.getPointsBalance() + points);
    }

    private void subtractPointsFromCard(BonusCard card, Integer points) {
        card.setPointsBalance(card.getPointsBalance() - points);
    }

    private void createTransaction(BonusCard card, Integer points, BonusTransactionType type,
                                   String referenceId, Booking booking) {
        if (points > 0) {
            validatePositivePoints(points);
        }
        var transaction = BonusTransaction.builder().bonusCard(card).booking(booking).type(type).pointsChange(points)
                .referenceId(referenceId).build();
        bonusTransactionRepository.save(transaction);
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

    private boolean canReceiveBirthdayBonus(User user) {
        return user.getVerificationStatus() == VerificationStatus.VERIFIED && user.getDateOfBirth() != null
                && isBirthdayToday(user.getDateOfBirth(), LocalDate.now());
    }

    private boolean isBirthdayToday(LocalDate birthDate, LocalDate today) {
        return birthDate.getMonth() == today.getMonth() && birthDate.getDayOfMonth() == today.getDayOfMonth();
    }

    private boolean alreadyReceivedBirthdayBonus(BonusCard card, LocalDate today) {
        return card.getLastBirthdayBonusDate() != null && card.getLastBirthdayBonusDate().getYear() == today.getYear();
    }

    private void validatePositivePoints(Integer points) {
        if (points == null || points <= 0) {
            throw BonusValidationException.invalidPoints(points);
        }
    }

    private void auditPointsAdded(BonusCard card, User user, Integer points, String promotionTitle) {
        auditBonusChange(card.getId(), user.getEmail(), AuditAction.POINTS_ADDED, null,
                Map.of("points", points, "promotion", promotionTitle, "newBalance", card.getPointsBalance()));
    }

    private void auditPointsSpent(BonusCard card, Booking booking, int oldBalance) {
        auditBonusChange(card.getId(), "Booking " + booking.getId(), AuditAction.POINTS_SPENT,
                Map.of("points", oldBalance), Map.of("points", card.getPointsBalance()));
    }

    private void auditPointsAccrued(BonusCard card, Payment payment, Integer points) {
        auditBonusChange(card.getId(), "Payment " + payment.getId(), AuditAction.POINTS_ACCRUED, null,
                Map.of("points", points, "newBalance", card.getPointsBalance()));
    }

    private void auditPointsRefunded(BonusCard card, Booking booking, int oldBalance) {
        auditBonusChange(card.getId(), "Booking " + booking.getId(), AuditAction.POINTS_REFUNDED,
                Map.of("points", oldBalance), Map.of("points", card.getPointsBalance()));
    }

    private void auditBonusChange(Long cardId, String target, AuditAction action, Map<String, Object> oldValues,
                                  Map<String, Object> newValues) {
        auditService.logChange("Bonus", cardId, target, action, oldValues, newValues);
    }
}