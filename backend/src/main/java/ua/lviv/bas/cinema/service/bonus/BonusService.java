package ua.lviv.bas.cinema.service.bonus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.mapper.bonus.BonusMapper;
import ua.lviv.bas.cinema.repository.bonus.BonusCardRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusTransactionRepository;
import ua.lviv.bas.cinema.repository.bonus.projection.BonusTransactionProjection;
import ua.lviv.bas.cinema.service.shared.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bonus")
public class BonusService {

	private final BonusCardRepository bonusCardRepository;
	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;
	private final BonusProperties bonusProperties;
	private final AuditService auditService;

	@Cacheable(key = "'card:' + #userId")
	@Transactional(readOnly = true)
	public BonusCardResponse getCard(Long userId) {
		BonusCard card = getCardByUserId(userId);
		return bonusMapper.toBonusCardResponse(card);
	}

	@Cacheable(key = "'balance:' + #userId")
	@Transactional(readOnly = true)
	public BonusBalanceResponse getBalance(Long userId) {
		BonusCard card = getCardByUserId(userId);
		return buildBalance(card);
	}

	@Cacheable(key = "'transactions:' + #userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getTransactions(Long userId, Pageable pageable) {
		Page<BonusTransactionProjection> page = bonusTransactionRepository.findProjectionsByUserId(userId, pageable);
		return page.map(bonusMapper::toBonusTransactionResponse);
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #user.id"), @CacheEvict(key = "'balance:' + #user.id"),
			@CacheEvict(key = "'transactions:' + #user.id + '-' + 0 + '-' + 20"), @CacheEvict(allEntries = true) })
	@Transactional
	public void awardWelcomeBonus(User user) {
		BonusCard card = getOrCreateCard(user);
		if (card.isWelcomeBonusReceived()) {
			return;
		}
		BonusRules rule = getActiveRule(BonusTransactionType.WELCOME_BONUS);
		card.setPointsBalance(card.getPointsBalance() + rule.getPoints());
		createTransaction(card, rule.getPoints(), BonusTransactionType.WELCOME_BONUS, "USER_" + user.getId());
		card.setWelcomeBonusReceived(true);
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #user.id"), @CacheEvict(key = "'balance:' + #user.id"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public void awardBirthdayBonus(User user) {
		if (user.getVerificationStatus() != VerificationStatus.VERIFIED || user.getDateOfBirth() == null) {
			return;
		}
		LocalDate today = LocalDate.now();
		if (!isBirthdayToday(user.getDateOfBirth(), today)) {
			return;
		}
		BonusCard card = getOrCreateCard(user);
		if (alreadyReceivedBirthdayBonus(card, today)) {
			return;
		}
		BonusRules rule = getActiveRule(BonusTransactionType.BIRTHDAY_BONUS);
		card.setPointsBalance(card.getPointsBalance() + rule.getPoints());
		createTransaction(card, rule.getPoints(), BonusTransactionType.BIRTHDAY_BONUS, "USER_" + user.getId());
		card.setLastBirthdayBonusDate(today);
		bonusCardRepository.save(card);
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #user.id"), @CacheEvict(key = "'balance:' + #user.id"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public Integer addPoints(User user, Integer points, String promotionTitle) {
		validatePositivePoints(points);
		BonusCard card = getOrCreateCard(user);
		card.setPointsBalance(card.getPointsBalance() + points);
		createTransaction(card, points, BonusTransactionType.PROMOTION_BONUS, "PROMOTION_" + promotionTitle);

		Map<String, Object> newValues = new HashMap<>();
		newValues.put("points", points);
		newValues.put("promotion", promotionTitle);
		newValues.put("newBalance", card.getPointsBalance());

		auditService.logChange("Bonus", card.getId(), "User " + user.getEmail(), AuditAction.POINTS_ADDED, null,
				newValues);

		return card.getPointsBalance();
	}

	@Transactional(readOnly = true)
	public void validateRedemption(Long userId, Integer points) {
		BonusCard card = getCardByUserId(userId);
		validatePositivePoints(points);
		if (card.getPointsBalance() < points) {
			throw new InsufficientPointsException(card.getPointsBalance(), points);
		}
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #userId"), @CacheEvict(key = "'balance:' + #userId"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public BonusTransaction spendPoints(Long userId, Integer points, Booking booking) {
		validateRedemption(userId, points);
		BonusCard card = getCardByUserId(userId);
		card.setPointsBalance(card.getPointsBalance() - points);

		Map<String, Object> oldValues = new HashMap<>();
		Map<String, Object> newValues = new HashMap<>();
		oldValues.put("points", card.getPointsBalance() + points);
		newValues.put("points", card.getPointsBalance());

		auditService.logChange("Bonus", card.getId(), "Booking " + booking.getId(), AuditAction.POINTS_SPENT, oldValues,
				newValues);

		return createTransaction(card, -points, BonusTransactionType.BOOKING_SPEND, "BOOKING_" + booking.getId(),
				booking, null, null);
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #userId"), @CacheEvict(key = "'balance:' + #userId"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public BonusTransaction accruePoints(Long userId, Integer points, Booking booking, Payment payment) {
		if (points == null || points <= 0) {
			return null;
		}
		BonusCard card = getCardByUserId(userId);
		card.setPointsBalance(card.getPointsBalance() + points);

		Map<String, Object> newValues = new HashMap<>();
		newValues.put("points", points);
		newValues.put("newBalance", card.getPointsBalance());

		auditService.logChange("Bonus", card.getId(), "Payment " + payment.getId(), AuditAction.POINTS_ACCRUED, null,
				newValues);

		return createTransaction(card, points, BonusTransactionType.PAYMENT_ACCRUAL, "PAYMENT_" + payment.getId(),
				booking, payment, null);
	}

	@Caching(evict = { @CacheEvict(key = "'card:' + #booking.user.id"),
			@CacheEvict(key = "'balance:' + #booking.user.id"), @CacheEvict(allEntries = true) })
	@Transactional
	public BonusTransaction refundPoints(Booking booking) {
		if (booking.getBonusPointsUsed() == null || booking.getBonusPointsUsed() <= 0) {
			return null;
		}
		BonusCard card = getCardByUserId(booking.getUser().getId());
		Integer points = booking.getBonusPointsUsed();
		int oldBalance = card.getPointsBalance();
		card.setPointsBalance(card.getPointsBalance() + points);

		Map<String, Object> oldValues = new HashMap<>();
		Map<String, Object> newValues = new HashMap<>();
		oldValues.put("points", oldBalance);
		newValues.put("points", card.getPointsBalance());

		auditService.logChange("Bonus", card.getId(), "Booking " + booking.getId(), AuditAction.POINTS_REFUNDED,
				oldValues, newValues);

		return createTransaction(card, points, BonusTransactionType.REFUND_RETURN, "REFUND_BOOKING_" + booking.getId(),
				booking, null, null);
	}

	@Transactional(readOnly = true)
	public Integer calculatePoints(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}
		Optional<BonusRules> ruleOpt = bonusRulesRepository
				.findByBonusTypeAndActiveTrue(BonusTransactionType.PAYMENT_ACCRUAL);
		if (ruleOpt.isEmpty() || ruleOpt.get().getMoneyRatio() == null) {
			return 0;
		}
		BonusRules rule = ruleOpt.get();
		int points = amount.multiply(rule.getMoneyRatio()).intValue();
		if (rule.getMinPointsPerTransaction() != null && points < rule.getMinPointsPerTransaction()) {
			points = rule.getMinPointsPerTransaction();
		}
		if (rule.getMaxPointsPerTransaction() != null && points > rule.getMaxPointsPerTransaction()) {
			points = rule.getMaxPointsPerTransaction();
		}
		return points;
	}

	@Transactional(readOnly = true)
	public void validatePointsForBooking(Long userId, Integer points, BigDecimal totalPrice) {
		validateRedemption(userId, points);
		BigDecimal discount = bonusProperties.getPointValue().multiply(BigDecimal.valueOf(points));
		BigDecimal maxDiscount = totalPrice.multiply(bonusProperties.getMaxDiscountPercentage());
		if (discount.compareTo(maxDiscount) > 0) {
			throw BonusValidationException.discountExceedsMax(discount, maxDiscount);
		}
	}

	@Cacheable(key = "'available:' + #userId + '-' + #totalPrice")
	@Transactional(readOnly = true)
	public Integer getAvailablePoints(Long userId, BigDecimal totalPrice) {
		BonusCard card = getCardByUserId(userId);
		int available = card.getPointsBalance();
		BigDecimal maxDiscount = totalPrice.multiply(bonusProperties.getMaxDiscountPercentage());
		int maxByPrice = maxDiscount.divide(bonusProperties.getPointValue(), 0, RoundingMode.DOWN).intValue();
		return Math.min(available, Math.max(maxByPrice, 0));
	}

	private BonusCard getCardByUserId(Long userId) {
		return bonusCardRepository.findByUserId(userId).orElseThrow(() -> new BonusCardNotFoundException(userId));
	}

	public BonusCard getOrCreateCard(User user) {
		return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> bonusCardRepository
				.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build()));
	}

	private BonusRules getActiveRule(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusTypeAndActiveTrue(type)
				.orElseThrow(() -> new BonusRuleNotFoundException(type));
	}

	private BonusBalanceResponse buildBalance(BonusCard card) {
		BigDecimal pointValue = bonusProperties.getPointValue();
		BigDecimal balanceValue = pointValue.multiply(BigDecimal.valueOf(card.getPointsBalance()));

		Optional<BonusRules> spendRuleOpt = bonusRulesRepository
				.findByBonusTypeAndActiveTrue(BonusTransactionType.BOOKING_SPEND);
		Integer minPoints = spendRuleOpt.map(BonusRules::getMinPointsPerTransaction).orElse(null);
		Integer maxPoints = spendRuleOpt.map(BonusRules::getMaxPointsPerTransaction).orElse(null);

		BigDecimal minValue = minPoints != null && minPoints > 0 ? pointValue.multiply(new BigDecimal(minPoints))
				: null;
		BigDecimal maxValue = maxPoints != null && maxPoints > 0 ? pointValue.multiply(new BigDecimal(maxPoints))
				: null;

		return new BonusBalanceResponse(card.getPointsBalance(), pointValue, balanceValue, minPoints, maxPoints,
				minValue, maxValue);
	}

	private BonusTransaction createTransaction(BonusCard card, Integer points, BonusTransactionType type,
			String referenceId) {
		return createTransaction(card, points, type, referenceId, null, null, null);
	}

	public BonusTransaction createTransaction(BonusCard card, Integer points, BonusTransactionType type,
			String referenceId, Booking booking, Payment payment, Refund refund) {
		if (points == null) {
			throw new IllegalArgumentException("Points cannot be null");
		}
		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking).type(type)
				.pointsChange(points).referenceId(referenceId).refund(refund).build();
		bonusCardRepository.save(card);
		return bonusTransactionRepository.save(transaction);
	}

	private void validatePositivePoints(Integer points) {
		if (points == null || points <= 0) {
			throw BonusValidationException.invalidPoints(points);
		}
	}

	private boolean isBirthdayToday(LocalDate birthDate, LocalDate today) {
		return birthDate.getMonth() == today.getMonth() && birthDate.getDayOfMonth() == today.getDayOfMonth();
	}

	private boolean alreadyReceivedBirthdayBonus(BonusCard card, LocalDate today) {
		return card.getLastBirthdayBonusDate() != null && card.getLastBirthdayBonusDate().getYear() == today.getYear();
	}
}