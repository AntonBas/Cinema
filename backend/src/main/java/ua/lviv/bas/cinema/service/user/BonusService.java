package ua.lviv.bas.cinema.service.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.BonusTransactionProjection;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusCardRepository;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusService {

	private final BonusCardRepository bonusCardRepository;
	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;
	private final BonusProperties bonusProperties;

	@Transactional(readOnly = true)
	public BonusCardResponse getCard(Long userId) {
		BonusCard card = getCardByUserId(userId);
		return bonusMapper.toBonusCardResponse(card);
	}

	@Transactional(readOnly = true)
	public BonusBalanceResponse getBalance(Long userId) {
		BonusCard card = getCardByUserId(userId);
		return buildBalance(card);
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getTransactions(Long userId, Pageable pageable) {
		Page<BonusTransactionProjection> page = bonusTransactionRepository.findProjectionsByUserId(userId, pageable);

		return page.map(projection -> {
			BonusTransactionResponse response = bonusMapper.toBonusTransactionResponse(projection);
			response.setBookingDetails(bonusMapper.toBookingDetails(projection));
			return response;
		});
	}

	@Transactional
	public void awardWelcomeBonus(User user) {
		BonusCard card = getOrCreateCard(user);

		if (card.isWelcomeBonusReceived()) {
			return;
		}

		BonusRules rule = getActiveRule(BonusTransactionType.WELCOME_BONUS);
		createTransaction(card, rule.getPoints(), BonusTransactionType.WELCOME_BONUS, "USER_" + user.getId(), null,
				null, null);

		card.setWelcomeBonusReceived(true);
		bonusCardRepository.save(card);
	}

	@Transactional
	public void awardBirthdayBonus(User user) {
		if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
			return;
		}

		if (user.getDateOfBirth() == null) {
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
		createTransaction(card, rule.getPoints(), BonusTransactionType.BIRTHDAY_BONUS, "USER_" + user.getId(), null,
				null, null);

		card.setLastBirthdayBonusDate(today);
		bonusCardRepository.save(card);
	}

	@Transactional
	public Integer addPoints(User user, Integer points) {
		if (points == null || points <= 0) {
			throw BonusValidationException.invalidPoints(points);
		}

		BonusCard card = getOrCreateCard(user);

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card)
				.type(BonusTransactionType.PROMOTION_BONUS).pointsChange(points)
				.referenceId("PROMOTION_" + System.currentTimeMillis()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + points);
		bonusCardRepository.save(card);
		bonusTransactionRepository.save(transaction);

		return card.getPointsBalance();
	}

	@Transactional(readOnly = true)
	public void validateRedemption(Long userId, Integer points) {
		BonusCard card = getCardByUserId(userId);

		if (card.getPointsBalance() < points) {
			throw new InsufficientPointsException(card.getPointsBalance(), points);
		}

		if (points <= 0) {
			throw BonusValidationException.invalidPoints(points);
		}
	}

	@Transactional
	public BonusTransaction spendPoints(Long userId, Integer points, Booking booking, String reference) {
		validateRedemption(userId, points);
		BonusCard card = getCardByUserId(userId);

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.BOOKING_SPEND).pointsChange(-points)
				.referenceId(reference != null ? reference : "BOOKING_" + booking.getId())
				.createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() - points);
		bonusCardRepository.save(card);

		return bonusTransactionRepository.save(transaction);
	}

	@Transactional
	public BonusTransaction accruePoints(Long userId, Integer points, Booking booking, Payment payment) {
		if (points == null || points <= 0) {
			return null;
		}

		BonusCard card = getCardByUserId(userId);

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.PAYMENT_ACCRUAL).pointsChange(points)
				.referenceId("PAYMENT_" + payment.getId()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + points);
		bonusCardRepository.save(card);

		return bonusTransactionRepository.save(transaction);
	}

	@Transactional
	public BonusTransaction refundPoints(Booking booking) {
		if (booking.getBonusPointsUsed() == null || booking.getBonusPointsUsed() <= 0) {
			return null;
		}

		BonusCard card = getCardByUserId(booking.getUser().getId());
		Integer points = booking.getBonusPointsUsed();

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.REFUND_RETURN).pointsChange(points)
				.referenceId("REFUND_BOOKING_" + booking.getId()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + points);
		bonusCardRepository.save(card);

		return bonusTransactionRepository.save(transaction);
	}

	@Transactional(readOnly = true)
	public Integer calculatePoints(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}

		BonusRules rule = bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PAYMENT_ACCRUAL)
				.orElse(null);

		if (rule == null || rule.getMoneyRatio() == null) {
			return 0;
		}

		Integer points = amount.multiply(rule.getMoneyRatio()).intValue();

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

	@Transactional(readOnly = true)
	public Integer getAvailablePoints(Long userId, BigDecimal totalPrice) {
		BonusCard card = getCardByUserId(userId);
		int available = card.getPointsBalance();

		BigDecimal maxDiscount = totalPrice.multiply(bonusProperties.getMaxDiscountPercentage());
		int maxByPrice = maxDiscount.divide(bonusProperties.getPointValue(), 0, RoundingMode.DOWN).intValue();

		available = Math.min(available, maxByPrice);
		return Math.max(available, 0);
	}

	@Transactional
	public BonusCard getOrCreateCard(User user) {
		return bonusCardRepository.findByUserId(user.getId()).orElseGet(() -> {
			return bonusCardRepository
					.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build());
		});
	}

	@Transactional(readOnly = true)
	public BonusRules getActiveRule(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusTypeAndActiveTrue(type)
				.orElseThrow(() -> new BonusRuleNotFoundException(type));
	}

	@Transactional
	public BonusTransaction createTransaction(BonusCard card, Integer points, BonusTransactionType type,
			String referenceId, Booking booking, Payment payment, Refund refund) {
		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking).type(type)
				.pointsChange(points).referenceId(referenceId).refund(refund).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + points);
		bonusCardRepository.save(card);

		return bonusTransactionRepository.save(transaction);
	}

	private BonusCard getCardByUserId(Long userId) {
		return bonusCardRepository.findByUserId(userId).orElseThrow(() -> new BonusCardNotFoundException(userId));
	}

	private BonusBalanceResponse buildBalance(BonusCard card) {
		BigDecimal pointValue = bonusProperties.getPointValue();
		BigDecimal balanceValue = pointValue.multiply(BigDecimal.valueOf(card.getPointsBalance()));

		BonusRules spendRule = bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.BOOKING_SPEND)
				.orElse(null);

		Integer minPoints = spendRule != null ? spendRule.getMinPointsPerTransaction() : null;
		Integer maxPoints = spendRule != null ? spendRule.getMaxPointsPerTransaction() : null;

		BigDecimal minValue = minPoints != null && minPoints > 0 ? pointValue.multiply(new BigDecimal(minPoints))
				: null;
		BigDecimal maxValue = maxPoints != null && maxPoints > 0 ? pointValue.multiply(new BigDecimal(maxPoints))
				: null;

		return BonusBalanceResponse.builder().pointsBalance(card.getPointsBalance()).pointValue(pointValue)
				.balanceValue(balanceValue).minUsablePoints(minPoints).maxUsablePoints(maxPoints)
				.minRedemptionValue(minValue).maxRedemptionValue(maxValue).build();
	}

	private boolean isBirthdayToday(LocalDate birthDate, LocalDate today) {
		return birthDate.getMonth() == today.getMonth() && birthDate.getDayOfMonth() == today.getDayOfMonth();
	}

	private boolean alreadyReceivedBirthdayBonus(BonusCard card, LocalDate today) {
		return card.getLastBirthdayBonusDate() != null && card.getLastBirthdayBonusDate().getYear() == today.getYear();
	}
}