package ua.lviv.bas.cinema.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusCardRepository;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusUserService {

	private final BonusCardRepository bonusCardRepository;
	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;

	public boolean hasEnoughPoints(BonusCard card, Integer points) {
		return card != null && card.getPointsBalance() != null && points != null && card.getPointsBalance() >= points;
	}

	public BigDecimal calculateMoneyValue(BonusRules rule, Integer points) {
		if (points == null || rule == null || rule.getPointValue() == null) {
			return BigDecimal.ZERO;
		}
		return rule.getPointValue().multiply(BigDecimal.valueOf(points));
	}

	public boolean isValidForWriteOff(BonusRules rule, Integer pointsToUse) {
		if (rule == null || !rule.getIsActive() || rule.getPointValue() == null
				|| rule.getBonusType() != BonusTransactionType.PURCHASE_WRITE_OFF) {
			return false;
		}

		return pointsToUse != null && pointsToUse >= rule.getMinPointsPerTransaction()
				&& pointsToUse <= rule.getMaxPointsPerTransaction();
	}

	public boolean isValidForPurchaseBonus(BonusRules rule) {
		return rule != null && rule.getIsActive() && rule.getMoneyRatio() != null
				&& rule.getBonusType() == BonusTransactionType.PURCHASE_BONUS;
	}

	public BonusCardResponse getBonusCard(Long userId) {
		log.debug("Getting bonus card for user: {}", userId);
		BonusCard card = findBonusCardByUserId(userId);
		return bonusMapper.toBonusCardResponse(card);
	}

	public BonusBalanceResponse getBalance(Long userId) {
		log.debug("Getting balance for user: {}", userId);
		BonusCard card = findBonusCardByUserId(userId);
		BonusRules writeOffRule = findActiveRule(BonusTransactionType.PURCHASE_WRITE_OFF);
		return buildBalanceResponse(card, writeOffRule);
	}

	public PageResponse<BonusTransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		log.debug("Getting transactions for user: {}, page: {}, size: {}", userId, pageNumber, pageSize);
		BonusCard card = findBonusCardByUserId(userId);
		Page<BonusTransaction> page = bonusTransactionRepository.findByBonusCardOrderByCreatedAtDesc(card, pageable);
		return PageResponse.of(page, bonusMapper::toBonusTransactionResponse);
	}

	@Transactional
	public void awardWelcomeBonus(User user) {
		log.debug("Processing welcome bonus for user: {}", user.getId());
		BonusCard card = findOrCreateBonusCard(user);

		if (card.getWelcomeBonusReceived()) {
			log.debug("User {} already received welcome bonus", user.getId());
			return;
		}
		BonusRules rule = findActiveRule(BonusTransactionType.WELCOME_BONUS);
		createBonusTransaction(card, rule.getPoints(), BonusTransactionType.WELCOME_BONUS, "USER_" + user.getId(), null,
				null);

		card.setWelcomeBonusReceived(true);
		bonusCardRepository.save(card);
		log.info("Awarded welcome bonus ({} points) to user {}", rule.getPoints(), user.getId());
	}

	@Transactional
	public void awardBirthdayBonus(User user) {
		log.debug("Checking birthday bonus for user: {}", user.getId());
		if (user.getDateOfBirth() == null) {
			log.debug("User {} has no birth date set", user.getId());
			return;
		}

		if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
			log.debug("User {} is not verified. Status: {}", user.getId(), user.getVerificationStatus());
			return;
		}

		LocalDate today = LocalDate.now();
		if (!isBirthdayToday(user.getDateOfBirth(), today)) {
			log.debug("Today is not birthday for user {}", user.getId());
			return;
		}

		BonusCard card = findOrCreateBonusCard(user);
		if (alreadyReceivedThisYear(card, today)) {
			log.debug("User {} already received birthday bonus this year", user.getId());
			return;
		}

		BonusRules rule = findActiveRule(BonusTransactionType.BIRTHDAY_BONUS);
		createBonusTransaction(card, rule.getPoints(), BonusTransactionType.BIRTHDAY_BONUS, "USER_" + user.getId(),
				null, null);

		card.setLastBirthdayBonusDate(today);
		bonusCardRepository.save(card);
		log.info("Awarded birthday bonus ({} points) to user {}", rule.getPoints(), user.getId());
	}

	public void validatePointsRedemption(Long userId, Integer pointsToUse) {
		log.debug("Validating {} points redemption for user: {}", pointsToUse, userId);
		BonusCard card = findBonusCardByUserId(userId);
		BonusRules writeOffRule = findActiveRule(BonusTransactionType.PURCHASE_WRITE_OFF);

		if (!hasEnoughPoints(card, pointsToUse)) {
			log.warn("User {} has insufficient points: available {}, required {}", userId, card.getPointsBalance(),
					pointsToUse);
			throw new InsufficientPointsException(card.getPointsBalance(), pointsToUse);
		}

		if (!isValidForWriteOff(writeOffRule, pointsToUse)) {
			log.warn("User {} attempted invalid redemption: {} points, range {}-{}", userId, pointsToUse,
					writeOffRule.getMinPointsPerTransaction(), writeOffRule.getMaxPointsPerTransaction());
			throw new InsufficientPointsException(card.getPointsBalance(), pointsToUse);
		}

		log.debug("Points redemption validation passed for user: {}", userId);
	}

	public Integer calculateEarnedPoints(BigDecimal purchaseAmount) {
		log.debug("Calculating earned points for purchase amount: {}", purchaseAmount);
		BonusRules purchaseRule = findActiveRule(BonusTransactionType.PURCHASE_BONUS);

		if (!isValidForPurchaseBonus(purchaseRule)) {
			log.warn("Purchase bonus rule is not valid for awarding points");
			return 0;
		}

		Integer points = purchaseAmount.multiply(purchaseRule.getMoneyRatio()).intValue();
		log.debug("Calculated {} points for {} UAH purchase", points, purchaseAmount);
		return points;
	}

	@Transactional
	public BonusTransaction redeemPointsForPurchase(Long userId, Integer pointsToUse, Payment payment,
			BigDecimal purchaseAmount) {
		log.info("Processing points redemption for user: {}, points: {}, payment: {}", userId, pointsToUse,
				payment.getId());

		validatePointsRedemption(userId, pointsToUse);
		BonusCard card = findBonusCardByUserId(userId);

		BonusTransaction writeOff = createBonusTransaction(card, -pointsToUse, BonusTransactionType.PURCHASE_WRITE_OFF,
				"PAYMENT_" + payment.getId(), payment, null);

		Integer earnedPoints = calculateEarnedPoints(purchaseAmount);
		if (earnedPoints > 0) {
			createBonusTransaction(card, earnedPoints, BonusTransactionType.PURCHASE_BONUS,
					"PAYMENT_" + payment.getId(), payment, null);
			log.debug("Awarded {} points for purchase", earnedPoints);
		}
		log.info("Successfully processed {} points redemption for user {}", pointsToUse, userId);
		return writeOff;
	}

	private BonusCard findBonusCardByUserId(Long userId) {
		return bonusCardRepository.findByUserId(userId).orElseThrow(() -> {
			log.error("Bonus card not found for user: {}", userId);
			return new BonusCardNotFoundException(userId);
		});
	}

	private BonusCard findOrCreateBonusCard(User user) {
		return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> {
			log.debug("Creating new bonus card for user: {}", user.getId());
			return bonusCardRepository
					.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build());
		});
	}

	private BonusRules findActiveRule(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusTypeAndIsActiveTrue(type).orElseThrow(() -> {
			log.error("Active bonus rule not found for type: {}", type);
			return new BonusRuleNotFoundException(type);
		});
	}

	@Transactional
	public BonusTransaction createBonusTransaction(BonusCard card, Integer pointsChange, BonusTransactionType type,
			String referenceId, Payment payment, Refund refund) {
		log.debug("Creating {} transaction: {} points for card {}", type, pointsChange, card.getId());

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).type(type).pointsChange(pointsChange)
				.referenceId(referenceId).payment(payment).refund(refund).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + pointsChange);
		bonusCardRepository.save(card);

		BonusTransaction saved = bonusTransactionRepository.save(transaction);
		log.debug("Created transaction {} for card {}", saved.getId(), card.getId());
		return saved;
	}

	private BonusBalanceResponse buildBalanceResponse(BonusCard card, BonusRules writeOffRule) {
		BigDecimal pointValue = writeOffRule.getPointValue();
		BigDecimal balanceValue = calculateMoneyValue(writeOffRule, card.getPointsBalance());

		return BonusBalanceResponse.builder().pointsBalance(card.getPointsBalance()).pointValue(pointValue)
				.balanceValue(balanceValue).minUsablePoints(writeOffRule.getMinPointsPerTransaction())
				.maxUsablePoints(writeOffRule.getMaxPointsPerTransaction())
				.minRedemptionValue(calculateMoneyValue(writeOffRule, writeOffRule.getMinPointsPerTransaction()))
				.maxRedemptionValue(calculateMoneyValue(writeOffRule, writeOffRule.getMaxPointsPerTransaction()))
				.build();
	}

	private boolean isBirthdayToday(LocalDate birthDate, LocalDate today) {
		return birthDate.getMonth() == today.getMonth() && birthDate.getDayOfMonth() == today.getDayOfMonth();
	}

	private boolean alreadyReceivedThisYear(BonusCard card, LocalDate today) {
		return card.getLastBirthdayBonusDate() != null && card.getLastBirthdayBonusDate().getYear() == today.getYear();
	}
}