package ua.lviv.bas.cinema.bonus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardConcurrentModificationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
public class BonusLedgerService {

    private static final int MAX_OPTIMISTIC_LOCK_ATTEMPTS = 3;

    private final BonusCardRepository bonusCardRepository;
    private final BonusRulesRepository bonusRulesRepository;
    private final BonusTransactionRepository bonusTransactionRepository;
    private final BonusQueryService bonusQueryService;
    private final AuditService auditService;
    private final TransactionTemplate transactionTemplate;

    public BonusLedgerService(BonusCardRepository bonusCardRepository, BonusRulesRepository bonusRulesRepository,
            BonusTransactionRepository bonusTransactionRepository, BonusQueryService bonusQueryService,
            AuditService auditService, PlatformTransactionManager transactionManager) {
        this.bonusCardRepository = bonusCardRepository;
        this.bonusRulesRepository = bonusRulesRepository;
        this.bonusTransactionRepository = bonusTransactionRepository;
        this.bonusQueryService = bonusQueryService;
        this.auditService = auditService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void awardWelcomeBonus(User user) {
        executeWithOptimisticLockRetry(() -> {
            var card = getOrCreateCard(user);
            if (card.isWelcomeBonusReceived()) {
                return;
            }
            var rule = getActiveRule(BonusTransactionType.WELCOME_BONUS);
            addPointsToCard(card, rule.getPoints());
            createTransaction(card, rule.getPoints(), BonusTransactionType.WELCOME_BONUS, "USER_" + user.getId());
            card.setWelcomeBonusReceived(true);
            bonusCardRepository.save(card);
        });
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void awardBirthdayBonus(User user) {
        if (!canReceiveBirthdayBonus(user)) {
            return;
        }
        var today = LocalDate.now();
        executeWithOptimisticLockRetry(() -> {
            var card = getOrCreateCard(user);
            if (alreadyReceivedBirthdayBonus(card, today)) {
                return;
            }
            var rule = getActiveRule(BonusTransactionType.BIRTHDAY_BONUS);
            addPointsToCard(card, rule.getPoints());
            createTransaction(card, rule.getPoints(), BonusTransactionType.BIRTHDAY_BONUS, "USER_" + user.getId());
            card.setLastBirthdayBonusDate(today);
            bonusCardRepository.save(card);
        });
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void addPromotionPoints(User user, Integer points, String promotionTitle) {
        validatePositivePoints(points);
        var card = executeWithOptimisticLockRetry(() -> {
            var c = getOrCreateCard(user);
            addPointsToCard(c, points);
            createTransaction(c, points, BonusTransactionType.PROMOTION_BONUS, "PROMOTION_" + promotionTitle);
            return c;
        });
        auditPointsAdded(card, user, points, promotionTitle);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void spendPoints(Long userId, Integer points, Booking booking) {
        var result = executeWithOptimisticLockRetry(() -> {
            bonusQueryService.validateRedemption(userId, points);
            var card = getCardByUserId(userId);
            int oldBalance = card.getPointsBalance();
            subtractPointsFromCard(card, points);
            bonusCardRepository.save(card);
            createTransaction(card, -points, BonusTransactionType.BOOKING_SPEND, "BOOKING_" + booking.getId(),
                    booking);
            return new CardBalanceChange(card, oldBalance);
        });
        auditPointsSpent(result.card(), booking, result.oldBalance());
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void accruePointsForPayment(Long userId, Integer points, Booking booking, Payment payment) {
        if (points == null || points <= 0) {
            return;
        }
        var card = executeWithOptimisticLockRetry(() -> {
            var c = getCardByUserId(userId);
            addPointsToCard(c, points);
            bonusCardRepository.save(c);
            createTransaction(c, points, BonusTransactionType.PAYMENT_ACCRUAL, "PAYMENT_" + payment.getId(),
                    booking);
            return c;
        });
        auditPointsAccrued(card, payment, points);
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void refundPoints(Booking booking) {
        if (booking.getBonusPointsUsed() == null || booking.getBonusPointsUsed() <= 0) {
            return;
        }
        var points = booking.getBonusPointsUsed();
        var result = executeWithOptimisticLockRetry(() -> {
            var card = getCardByUserId(booking.getUser().getId());
            int oldBalance = card.getPointsBalance();
            addPointsToCard(card, points);
            bonusCardRepository.save(card);
            createTransaction(card, points, BonusTransactionType.REFUND_RETURN, "REFUND_BOOKING_" + booking.getId(),
                    booking);
            return new CardBalanceChange(card, oldBalance);
        });
        auditPointsRefunded(result.card(), booking, result.oldBalance());
    }

    @CacheEvict(value = "bonus", allEntries = true)
    public void refundPointsForTicket(Long userId, Integer points, String referenceId) {
        if (points == null || points <= 0) {
            return;
        }
        CardBalanceChange result;
        try {
            result = executeWithOptimisticLockRetry(() -> {
                if (bonusTransactionRepository.existsByReferenceId(referenceId)) {
                    log.debug("Bonus refund for reference {} already applied, skipping", referenceId);
                    return null;
                }
                var card = getCardByUserId(userId);
                int oldBalance = card.getPointsBalance();
                addPointsToCard(card, points);
                bonusCardRepository.save(card);
                createTransaction(card, points, BonusTransactionType.REFUND_RETURN, referenceId);
                return new CardBalanceChange(card, oldBalance);
            });
        } catch (DataIntegrityViolationException e) {
            log.debug("Bonus refund for reference {} already applied concurrently, skipping", referenceId);
            return;
        }

        if (result == null) {
            return;
        }
        auditBonusChange(result.card().getId(), "Ticket " + referenceId, AuditAction.POINTS_REFUNDED,
                Map.of("points", result.oldBalance()), Map.of("points", result.card().getPointsBalance()));
    }

    public BonusCard getOrCreateCard(User user) {
        return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> createBonusCard(user));
    }

    private void executeWithOptimisticLockRetry(Runnable action) {
        executeWithOptimisticLockRetry(() -> {
            action.run();
            return null;
        });
    }

    private <T> T executeWithOptimisticLockRetry(Supplier<T> action) {
        ObjectOptimisticLockingFailureException lastError = null;
        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_LOCK_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> action.get());
            } catch (ObjectOptimisticLockingFailureException e) {
                lastError = e;
                log.warn("Attempt {}/{} to update bonus card failed due to concurrent update", attempt,
                        MAX_OPTIMISTIC_LOCK_ATTEMPTS, e);
            }
        }
        throw new BonusCardConcurrentModificationException(lastError);
    }

    private record CardBalanceChange(BonusCard card, int oldBalance) {
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
