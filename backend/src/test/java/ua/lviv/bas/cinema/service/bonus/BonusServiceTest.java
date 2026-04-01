package ua.lviv.bas.cinema.service.bonus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.mapper.bonus.BonusMapper;
import ua.lviv.bas.cinema.repository.bonus.BonusCardRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusTransactionRepository;
import ua.lviv.bas.cinema.service.shared.AuditService;

@ExtendWith(MockitoExtension.class)
public class BonusServiceTest {

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
	@Mock
	private AuditService auditService;
	@InjectMocks
	private BonusService bonusService;
	@Captor
	private ArgumentCaptor<BonusCard> cardCaptor;
	@Captor
	private ArgumentCaptor<BonusTransaction> transactionCaptor;

	private final Long USER_ID = 1L;
	private final Long BOOKING_ID = 1L;
	private final Long PAYMENT_ID = 1L;
	private final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
	private final BonusTransactionType BIRTHDAY = BonusTransactionType.BIRTHDAY_BONUS;
	private final BonusTransactionType PAYMENT_TYPE = BonusTransactionType.PAYMENT_ACCRUAL;
	private final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;

	@Test
	void getCard_WhenCardExists_ReturnsResponse() {
		BonusCard card = new BonusCard();
		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

		bonusService.getCard(USER_ID);

		verify(bonusMapper).toBonusCardResponse(card);
	}

	@Test
	void getCard_WhenCardNotFound_ThrowsException() {
		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusService.getCard(USER_ID)).isInstanceOf(BonusCardNotFoundException.class);
	}

	@Test
	void getBalance_ReturnsBalance() {
		BonusCard card = BonusCard.builder().pointsBalance(100).build();
		BonusRules spendRule = BonusRules.builder().minPointsPerTransaction(10).maxPointsPerTransaction(50).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(SPEND)).thenReturn(Optional.of(spendRule));

		var result = bonusService.getBalance(USER_ID);

		assertThat(result).isNotNull();
		assertThat(result.pointsBalance()).isEqualTo(100);
		assertThat(result.pointValue()).isEqualTo(new BigDecimal("1.00"));
		assertThat(result.balanceValue()).isEqualTo(new BigDecimal("100.00"));
		assertThat(result.minUsablePoints()).isEqualTo(10);
		assertThat(result.maxUsablePoints()).isEqualTo(50);
	}

	@Test
	void awardWelcomeBonus_WhenNotReceived_AddsPoints() {
		User user = User.builder().id(USER_ID).build();
		BonusCard card = BonusCard.builder().pointsBalance(0).welcomeBonusReceived(false).build();
		BonusRules rule = BonusRules.builder().points(50).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		bonusService.awardWelcomeBonus(user);

		assertThat(card.getPointsBalance()).isEqualTo(50);
		assertThat(card.isWelcomeBonusReceived()).isTrue();
		verify(bonusCardRepository).save(cardCaptor.capture());
		verify(bonusTransactionRepository).save(transactionCaptor.capture());

		BonusTransaction transaction = transactionCaptor.getValue();
		assertThat(transaction.getPointsChange()).isEqualTo(50);
		assertThat(transaction.getType()).isEqualTo(WELCOME);
	}

	@Test
	void awardWelcomeBonus_WhenAlreadyReceived_DoesNothing() {
		User user = User.builder().id(USER_ID).build();
		BonusCard card = BonusCard.builder().welcomeBonusReceived(true).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

		bonusService.awardWelcomeBonus(user);

		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void awardBirthdayBonus_WhenBirthday_AddsPoints() {
		User user = User.builder().id(USER_ID).verificationStatus(VerificationStatus.VERIFIED)
				.dateOfBirth(LocalDate.now()).build();
		BonusCard card = BonusCard.builder().pointsBalance(0).lastBirthdayBonusDate(null).build();
		BonusRules rule = BonusRules.builder().points(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BIRTHDAY)).thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		bonusService.awardBirthdayBonus(user);

		assertThat(card.getPointsBalance()).isEqualTo(100);
		assertThat(card.getLastBirthdayBonusDate()).isEqualTo(LocalDate.now());
		verify(bonusCardRepository).save(any(BonusCard.class));
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void awardBirthdayBonus_WhenNotVerified_DoesNothing() {
		User user = User.builder().verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		bonusService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
	}

	@Test
	void awardBirthdayBonus_WhenNoBirthDate_DoesNothing() {
		User user = User.builder().verificationStatus(VerificationStatus.VERIFIED).dateOfBirth(null).build();

		bonusService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
	}

	@Test
	void addPoints_AddsPointsAndReturnsBalance() {
		User user = User.builder().id(USER_ID).build();
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		Integer result = bonusService.addPoints(user, 50, "PROMO");

		assertThat(result).isEqualTo(150);
		assertThat(card.getPointsBalance()).isEqualTo(150);
		verify(bonusCardRepository).findByUserId(USER_ID);
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void addPoints_WhenPointsInvalid_ThrowsException() {
		User user = User.builder().id(USER_ID).build();

		assertThatThrownBy(() -> bonusService.addPoints(user, 0, "PROMO")).isInstanceOf(BonusValidationException.class);
	}

	@Test
	void validateRedemption_WhenSufficientPoints_Passes() {
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

		bonusService.validateRedemption(USER_ID, 50);
	}

	@Test
	void validateRedemption_WhenInsufficientPoints_ThrowsException() {
		BonusCard card = BonusCard.builder().pointsBalance(30).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

		assertThatThrownBy(() -> bonusService.validateRedemption(USER_ID, 50))
				.isInstanceOf(InsufficientPointsException.class);
	}

	@Test
	void spendPoints_Success() {
		Booking booking = Booking.builder().id(BOOKING_ID).build();
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		BonusTransaction result = bonusService.spendPoints(USER_ID, 30, booking);

		assertThat(result).isNotNull();
		assertThat(card.getPointsBalance()).isEqualTo(70);
		verify(bonusCardRepository).save(any(BonusCard.class));
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void accruePoints_Success() {
		Booking booking = Booking.builder().id(BOOKING_ID).build();
		Payment payment = Payment.builder().id(PAYMENT_ID).build();
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		BonusTransaction result = bonusService.accruePoints(USER_ID, 50, booking, payment);

		assertThat(result).isNotNull();
		assertThat(card.getPointsBalance()).isEqualTo(150);
		verify(bonusCardRepository).save(any(BonusCard.class));
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void accruePoints_WhenPointsNull_ReturnsNull() {
		Booking booking = Booking.builder().id(BOOKING_ID).build();
		Payment payment = Payment.builder().id(PAYMENT_ID).build();

		BonusTransaction result = bonusService.accruePoints(USER_ID, null, booking, payment);

		assertThat(result).isNull();
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void refundPoints_Success() {
		User user = User.builder().id(USER_ID).build();
		Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(50).user(user).build();
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

		BonusTransaction result = bonusService.refundPoints(booking);

		assertThat(result).isNotNull();
		assertThat(card.getPointsBalance()).isEqualTo(150);
		verify(bonusCardRepository).save(any(BonusCard.class));
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void refundPoints_WhenNoPointsUsed_ReturnsNull() {
		User user = User.builder().id(USER_ID).build();
		Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(null).user(user).build();

		BonusTransaction result = bonusService.refundPoints(booking);

		assertThat(result).isNull();
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void calculatePoints_CalculatesCorrectly() {
		BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("1.5")).minPointsPerTransaction(10)
				.maxPointsPerTransaction(500).build();

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

		Integer result = bonusService.calculatePoints(new BigDecimal("100"));

		assertThat(result).isEqualTo(150);
	}

	@Test
	void calculatePoints_AppliesMinLimit() {
		BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("0.1")).minPointsPerTransaction(50).build();

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

		Integer result = bonusService.calculatePoints(new BigDecimal("100"));

		assertThat(result).isEqualTo(50);
	}

	@Test
	void calculatePoints_WhenNoRule_ReturnsZero() {
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.empty());

		Integer result = bonusService.calculatePoints(new BigDecimal("100"));

		assertThat(result).isZero();
	}

	@Test
	void validatePointsForBooking_Success() {
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		bonusService.validatePointsForBooking(USER_ID, 30, new BigDecimal("100"));
	}

	@Test
	void validatePointsForBooking_WhenDiscountExceeds_ThrowsException() {
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		assertThatThrownBy(() -> bonusService.validatePointsForBooking(USER_ID, 60, new BigDecimal("100")))
				.isInstanceOf(BonusValidationException.class);
	}

	@Test
	void getAvailablePoints_ReturnsCorrectValue() {
		BonusCard card = BonusCard.builder().pointsBalance(100).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		Integer result = bonusService.getAvailablePoints(USER_ID, new BigDecimal("100"));

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getAvailablePoints_WhenBalanceLower_ReturnsBalance() {
		BonusCard card = BonusCard.builder().pointsBalance(30).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		Integer result = bonusService.getAvailablePoints(USER_ID, new BigDecimal("100"));

		assertThat(result).isEqualTo(30);
	}

	@Test
	void getOrCreateCard_WhenExists_ReturnsCard() {
		BonusCard card = new BonusCard();
		User user = User.builder().id(USER_ID).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

		BonusCard result = bonusService.getOrCreateCard(user);

		assertThat(result).isEqualTo(card);
		verify(bonusCardRepository, never()).save(any());
	}

	@Test
	void getOrCreateCard_WhenNotExists_CreatesCard() {
		User user = User.builder().id(USER_ID).build();

		when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));

		bonusService.getOrCreateCard(user);

		verify(bonusCardRepository).save(any(BonusCard.class));
	}

	@Test
	void createTransaction_WithNullPoints_ThrowsException() {
		BonusCard card = new BonusCard();

		assertThatThrownBy(() -> bonusService.createTransaction(card, null, WELCOME, "REF"))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Points cannot be null");
	}
}