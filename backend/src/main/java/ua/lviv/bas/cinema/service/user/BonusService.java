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
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.Booking;
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
public class BonusService {

	private static final BigDecimal POINT_VALUE = new BigDecimal("1.00");
	private static final BigDecimal ACCRUAL_RATIO = new BigDecimal("0.05");
	private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("0.5");

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
		return buildBalanceResponse(card);
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
				null, null);

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
				null, null, null);

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

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card)
				.type(BonusTransactionType.PROMOTION_BONUS).pointsChange(points)
				.referenceId("PROMOTION_" + System.currentTimeMillis()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + points);
		bonusCardRepository.save(card);
		bonusTransactionRepository.save(transaction);

		log.info("Added {} points to user {}. New balance: {}", points, user.getId(), card.getPointsBalance());

		return card.getPointsBalance();
	}

	@Transactional(readOnly = true)
	public void validatePointsRedemption(Long userId, Integer pointsToUse) {
		log.debug("Validating {} points redemption for user: {}", pointsToUse, userId);
		BonusCard card = findBonusCardByUserId(userId);

		if (card.getPointsBalance() < pointsToUse) {
			log.warn("User {} has insufficient points: available {}, required {}", userId, card.getPointsBalance(),
					pointsToUse);
			throw new InsufficientPointsException(card.getPointsBalance(), pointsToUse);
		}

		if (pointsToUse <= 0) {
			log.warn("User {} attempted invalid redemption: {} points", userId, pointsToUse);
			throw new IllegalArgumentException("Points must be positive");
		}

		log.debug("Points redemption validation passed for user: {}", userId);
	}

	@Transactional
	public BonusTransaction spendBonusPointsForBooking(Long userId, Integer pointsToUse, Booking booking,
			String reference) {
		log.info("Spending {} points for booking {} (user: {})", pointsToUse, booking.getId(), userId);

		validatePointsRedemption(userId, pointsToUse);
		BonusCard card = findBonusCardByUserId(userId);

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.BOOKING_SPEND).pointsChange(-pointsToUse)
				.referenceId(reference != null ? reference : "BOOKING_" + booking.getId())
				.createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() - pointsToUse);
		bonusCardRepository.save(card);

		BonusTransaction saved = bonusTransactionRepository.save(transaction);
		log.info("Successfully spent {} points for booking {}", pointsToUse, booking.getId());

		return saved;
	}

	@Transactional
	public BonusTransaction accrueBonusPointsForPayment(Long userId, Integer pointsToAccrue, Booking booking,
			Payment payment) {
		log.info("Accruing {} points for payment {} (user: {})", pointsToAccrue, payment.getId(), userId);

		if (pointsToAccrue == null || pointsToAccrue <= 0) {
			log.debug("No points to accrue for payment {}", payment.getId());
			return null;
		}

		BonusCard card = findBonusCardByUserId(userId);

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.PAYMENT_ACCRUAL).pointsChange(pointsToAccrue)
				.referenceId("PAYMENT_" + payment.getId()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + pointsToAccrue);
		bonusCardRepository.save(card);

		BonusTransaction saved = bonusTransactionRepository.save(transaction);
		log.info("Successfully accrued {} points for payment {}", pointsToAccrue, payment.getId());

		return saved;
	}

	@Transactional
	public BonusTransaction refundBonusPointsForCancellation(Booking booking) {
		if (booking.getBonusPointsUsed() == null || booking.getBonusPointsUsed() <= 0) {
			log.debug("No bonus points to refund for booking {}", booking.getId());
			return null;
		}

		log.info("Refunding {} bonus points for cancelled booking {}", booking.getBonusPointsUsed(), booking.getId());

		BonusCard card = findBonusCardByUserId(booking.getUser().getId());
		Integer pointsToRefund = booking.getBonusPointsUsed();

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking)
				.type(BonusTransactionType.REFUND_RETURN).pointsChange(pointsToRefund)
				.referenceId("REFUND_BOOKING_" + booking.getId()).createdAt(LocalDateTime.now()).build();

		card.setPointsBalance(card.getPointsBalance() + pointsToRefund);
		bonusCardRepository.save(card);

		BonusTransaction saved = bonusTransactionRepository.save(transaction);
		log.info("Successfully refunded {} points for booking {}", pointsToRefund, booking.getId());

		return saved;
	}

	@Transactional(readOnly = true)
	public Integer calculateAccruedPointsForAmount(BigDecimal finalAmount) {
		log.debug("Calculating accrued points for amount: {}", finalAmount);

		if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return 0;
		}

		Integer points = finalAmount.multiply(ACCRUAL_RATIO).intValue();
		log.debug("Calculated {} points for {} UAH purchase", points, finalAmount);
		return points;
	}

	@Transactional(readOnly = true)
	public void validateBonusPointsForBooking(Long userId, Integer pointsToUse, BigDecimal bookingTotalPrice) {
		validatePointsRedemption(userId, pointsToUse);

		BigDecimal discountAmount = POINT_VALUE.multiply(BigDecimal.valueOf(pointsToUse));
		BigDecimal maxAllowedDiscount = bookingTotalPrice.multiply(MAX_DISCOUNT_PERCENTAGE);

		if (discountAmount.compareTo(maxAllowedDiscount) > 0) {
			throw new IllegalArgumentException(String.format("Bonus discount %.2f exceeds maximum allowed %.2f",
					discountAmount, maxAllowedDiscount));
		}
	}

	@Transactional(readOnly = true)
	public Integer getAvailablePointsForRedemption(Long userId, BigDecimal bookingTotalPrice) {
		BonusCard card = findBonusCardByUserId(userId);

		int availablePoints = card.getPointsBalance();

		BigDecimal maxDiscountAmount = bookingTotalPrice.multiply(MAX_DISCOUNT_PERCENTAGE);
		int maxPointsByPrice = maxDiscountAmount.divide(POINT_VALUE, 0, RoundingMode.DOWN).intValue();

		availablePoints = Math.min(availablePoints, maxPointsByPrice);

		return Math.max(availablePoints, 0);
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
			String referenceId, Booking booking, Payment payment, Refund refund) {
		log.debug("Creating {} transaction: {} points for card {}", type, pointsChange, card.getId());

		BonusTransaction transaction = BonusTransaction.builder().bonusCard(card).booking(booking).type(type)
				.pointsChange(pointsChange).referenceId(referenceId).refund(refund).createdAt(LocalDateTime.now())
				.build();

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

	private BonusBalanceResponse buildBalanceResponse(BonusCard card) {
		BigDecimal balanceValue = POINT_VALUE.multiply(BigDecimal.valueOf(card.getPointsBalance()));

		return BonusBalanceResponse.builder().pointsBalance(card.getPointsBalance()).pointValue(POINT_VALUE)
				.balanceValue(balanceValue).build();
	}
}