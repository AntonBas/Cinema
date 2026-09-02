package ua.lviv.bas.cinema.bonus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusLedgerService {

    private final BonusCardRepository bonusCardRepository;
    private final BonusRulesRepository bonusRulesRepository;
    private final BonusTransactionRepository bonusTransactionRepository;
    private final BonusQueryService bonusQueryService;
    private final AuditService auditService;

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
        bonusQueryService.validateRedemption(userId, points);
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

    @CacheEvict(value = "bonus", allEntries = true)
    @Transactional
    public void refundPointsForTicket(Long userId, Integer points, String referenceId) {
        if (points == null || points <= 0) {
            return;
        }
        if (bonusTransactionRepository.existsByReferenceId(referenceId)) {
            log.debug("Bonus refund for reference {} already applied, skipping", referenceId);
            return;
        }
        var card = getCardByUserId(userId);
        int oldBalance = card.getPointsBalance();
        addPointsToCard(card, points);
        bonusCardRepository.save(card);
        auditBonusChange(card.getId(), "Ticket " + referenceId, AuditAction.POINTS_REFUNDED,
                Map.of("points", oldBalance), Map.of("points", card.getPointsBalance()));
        createTransaction(card, points, BonusTransactionType.REFUND_RETURN, referenceId);
    }

    public BonusCard getOrCreateCard(User user) {
        return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> createBonusCard(user));
    }

    private BonusCard createBonusCard(User user) {
        var card = BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build();
        return bonusCardRepository.save(card);
    }

    private BonusCard getCardByUserId(Long userId) {
        return bonusCardRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Bonus card", userId));
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
                                   String referenceId) {
        createTransaction(card, points, type, referenceId, null);
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
