package ua.lviv.bas.cinema.service.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class UserBonusService {

	private static final BigDecimal POINT_VALUE = new BigDecimal("1.00");

	private final BonusCardRepository bonusCardRepository;
	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;

	@Transactional(readOnly = true)
	public BonusCardResponse getBonusCard(Long userId) {
		log.debug("Getting bonus card for user: {}", userId);
		BonusCard card = findBonusCardByUserId(userId);
		return bonusMapper.toBonusCardResponse(card);
	}

	@Transactional(readOnly = true)
	public BonusBalanceResponse getBalance(Long userId) {
		log.debug("Getting balance for user: {}", userId);
		BonusCard card = findBonusCardByUserId(userId);
		BonusRules writeOffRule = findActiveRule(BonusTransactionType.PURCHASE_WRITE_OFF);
		return buildBalanceResponse(card, writeOffRule);
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
		log.debug("Getting transactions for user: {}, page: {}, size: {}", userId, pageable.getPageNumber(),
				pageable.getPageSize());

		BonusCard card = findBonusCardByUserId(userId);
		Page<BonusTransaction> page = bonusTransactionRepository.findByBonusCardOrderByCreatedAtDesc(card, pageable);

		return page.map(bonusMapper::toBonusTransactionResponse);
	}

	@Transactional
	public void awardWelcomeBonus(User user) {
		log.debug("Processing welcome bonus for user: {}", user.getId());

		BonusCard card = findOrCreateBonusCard(user);

		if (card.isWelcomeBonusReceived()) {
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

		if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
			log.debug("User {} is not verified. Status: {}. Skipping birthday bonus.", user.getId(),
					user.getVerificationStatus());
			return;
		}

		if (user.getDateOfBirth() == null) {
			log.debug("User {} has no birth date set", user.getId());
			return;
		}

		LocalDate today = LocalDate.now();
		if (user.getDateOfBirth().getMonth() != today.getMonth()
				|| user.getDateOfBirth().getDayOfMonth() != today.getDayOfMonth()) {
			log.debug("Today is not birthday for user {}", user.getId());
			return;
		}

		BonusCard card = findOrCreateBonusCard(user);
		if (card.getLastBirthdayBonusDate() != null && card.getLastBirthdayBonusDate().getYear() == today.getYear()) {
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

	@Transactional
	public Integer addPoints(User user, Integer points) {
		log.info("Adding {} points to user {} from promotion", points, user.getId());

		if (points == null || points <= 0) {
			throw new IllegalArgumentException("Points must be a positive number");
		}

		BonusCard card = findOrCreateBonusCard(user);

		createBonusTransaction(card, points, BonusTransactionType.PROMOTION_BONUS,
				"PROMOTION_" + System.currentTimeMillis(), null, null);

		log.info("Added {} points to user {}. New balance: {}", points, user.getId(), card.getPointsBalance());

		return card.getPointsBalance();
	}

	@Transactional(readOnly = true)
	public void validatePointsRedemption(Long userId, Integer pointsToUse) {
		log.debug("Validating {} points redemption for user: {}", pointsToUse, userId);
		BonusCard card = findBonusCardByUserId(userId);
		BonusRules writeOffRule = findActiveRule(BonusTransactionType.PURCHASE_WRITE_OFF);

		if (card.getPointsBalance() < pointsToUse) {
			log.warn("User {} has insufficient points: available {}, required {}", userId, card.getPointsBalance(),
					pointsToUse);
			throw new InsufficientPointsException(card.getPointsBalance(), pointsToUse);
		}

		Integer min = writeOffRule.getMinPointsPerTransaction();
		Integer max = writeOffRule.getMaxPointsPerTransaction();

		if (min == null || max == null || pointsToUse < min || pointsToUse > max) {
			log.warn("User {} attempted invalid redemption: {} points, range {}-{}", userId, pointsToUse, min, max);
			throw new IllegalArgumentException(String.format("Points must be between %d and %d", min, max));
		}

		log.debug("Points redemption validation passed for user: {}", userId);
	}

	@Transactional(readOnly = true)
	public Integer calculateEarnedPoints(BigDecimal purchaseAmount) {
		log.debug("Calculating earned points for purchase amount: {}", purchaseAmount);
		if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}

		BonusRules purchaseRule = findActiveRule(BonusTransactionType.PURCHASE_BONUS);

		if (purchaseRule == null || !purchaseRule.isActive() || purchaseRule.getMoneyRatio() == null
				|| purchaseRule.getMoneyRatio().compareTo(BigDecimal.ZERO) <= 0) {
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

	@Transactional
	public BonusCard findOrCreateBonusCard(User user) {
		return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> {
			log.debug("Creating new bonus card for user: {}", user.getId());
			return bonusCardRepository
					.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build());
		});
	}

	@Transactional(readOnly = true)
	public BonusRules findActiveRule(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusTypeAndActiveTrue(type).orElseThrow(() -> {
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

	@Transactional(readOnly = true)
	private BonusCard findBonusCardByUserId(Long userId) {
		return bonusCardRepository.findByUserId(userId).orElseThrow(() -> {
			log.error("Bonus card not found for user: {}", userId);
			return new BonusCardNotFoundException(userId);
		});
	}

	private BonusBalanceResponse buildBalanceResponse(BonusCard card, BonusRules writeOffRule) {
		BigDecimal balanceValue = POINT_VALUE.multiply(BigDecimal.valueOf(card.getPointsBalance()));

		Integer minUsablePoints = writeOffRule.getMinPointsPerTransaction() != null
				? writeOffRule.getMinPointsPerTransaction()
				: 0;
		Integer maxUsablePoints = writeOffRule.getMaxPointsPerTransaction() != null
				? writeOffRule.getMaxPointsPerTransaction()
				: 0;

		return BonusBalanceResponse.builder().pointsBalance(card.getPointsBalance()).pointValue(POINT_VALUE)
				.balanceValue(balanceValue).minUsablePoints(minUsablePoints).maxUsablePoints(maxUsablePoints)
				.minRedemptionValue(POINT_VALUE.multiply(BigDecimal.valueOf(minUsablePoints)))
				.maxRedemptionValue(POINT_VALUE.multiply(BigDecimal.valueOf(maxUsablePoints))).build();
	}
}