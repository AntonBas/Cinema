package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
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

	@InjectMocks
	private BonusService bonusUserService;

	@Captor
	private ArgumentCaptor<BonusTransaction> transactionCaptor;

	@Captor
	private ArgumentCaptor<BonusCard> cardCaptor;

	@Test
	void getBonusCard_ShouldReturnCardResponse() {
		Long userId = 1L;
		User user = new User();
		user.setId(userId);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(250);
		card.setWelcomeBonusReceived(true);

		BonusCardResponse expectedResponse = BonusCardResponse.builder().id(1L).userId(userId).pointsBalance(250)
				.welcomeBonusReceived(true).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusMapper.toBonusCardResponse(card)).thenReturn(expectedResponse);

		BonusCardResponse result = bonusUserService.getBonusCard(userId);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusMapper).toBonusCardResponse(card);
	}

	@Test
	void getBonusCard_ShouldThrowWhenNotFound() {
		Long userId = 1L;

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.getBonusCard(userId)).isInstanceOf(BonusCardNotFoundException.class);

		verify(bonusCardRepository).findByUserId(userId);
	}

	@Test
	void getBalance_ShouldReturnBalanceResponse() {
		Long userId = 1L;
		User user = new User();
		user.setId(userId);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(250);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		BonusBalanceResponse result = bonusUserService.getBalance(userId);

		assertThat(result.getPointsBalance()).isEqualTo(250);
		assertThat(result.getPointValue()).isEqualByComparingTo("1.00");
		assertThat(result.getBalanceValue()).isEqualByComparingTo("250.00");
	}

	@Test
	void getBalance_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.getBalance(userId)).isInstanceOf(BonusCardNotFoundException.class);
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusCard card = new BonusCard();
		card.setId(1L);

		BonusTransaction transaction = BonusTransaction.builder().id(1L).bonusCard(card)
				.type(BonusTransactionType.PAYMENT_ACCRUAL).pointsChange(25).build();

		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);
		BonusTransactionResponse response = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusTransactionRepository.findByBonusCardOrderByCreatedAtDesc(card, pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(transaction)).thenReturn(response);

		Page<BonusTransactionResponse> result = bonusUserService.getUserTransactions(userId, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isSameAs(response);
		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusTransactionRepository).findByBonusCardOrderByCreatedAtDesc(card, pageable);
	}

	@Test
	void awardWelcomeBonus_ShouldAwardWhenNotReceived() {
		User user = new User();
		user.setId(1L);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(0);
		card.setWelcomeBonusReceived(false);

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.WELCOME_BONUS);
		rule.setPoints(100);
		rule.setActive(true);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.WELCOME_BONUS))
				.thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		bonusUserService.awardWelcomeBonus(user);

		verify(bonusCardRepository, times(2)).save(cardCaptor.capture());
		List<BonusCard> savedCards = cardCaptor.getAllValues();
		assertThat(savedCards.get(1).isWelcomeBonusReceived()).isTrue();
		assertThat(savedCards.get(1).getPointsBalance()).isEqualTo(100);
		verify(bonusTransactionRepository).save(transactionCaptor.capture());
		assertThat(transactionCaptor.getValue().getPointsChange()).isEqualTo(100);
		assertThat(transactionCaptor.getValue().getType()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
	}

	@Test
	void awardWelcomeBonus_ShouldNotAwardWhenAlreadyReceived() {
		User user = new User();
		user.setId(1L);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setWelcomeBonusReceived(true);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		bonusUserService.awardWelcomeBonus(user);

		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void awardBirthdayBonus_ShouldAwardWhenConditionsMet() {
		User user = new User();
		user.setId(1L);
		user.setDateOfBirth(LocalDate.now());
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(0);
		card.setLastBirthdayBonusDate(null);

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.BIRTHDAY_BONUS);
		rule.setPoints(200);
		rule.setActive(true);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.BIRTHDAY_BONUS))
				.thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusCardRepository, times(2)).save(cardCaptor.capture());
		List<BonusCard> savedCards = cardCaptor.getAllValues();
		assertThat(savedCards.get(1).getPointsBalance()).isEqualTo(200);
		assertThat(savedCards.get(1).getLastBirthdayBonusDate()).isEqualTo(LocalDate.now());
		verify(bonusTransactionRepository).save(transactionCaptor.capture());
		assertThat(transactionCaptor.getValue().getPointsChange()).isEqualTo(200);
		assertThat(transactionCaptor.getValue().getType()).isEqualTo(BonusTransactionType.BIRTHDAY_BONUS);
	}

	@Test
	void awardBirthdayBonus_ShouldNotAwardWhenNotVerified() {
		User user = new User();
		user.setId(1L);
		user.setDateOfBirth(LocalDate.now());
		user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void awardBirthdayBonus_ShouldNotAwardWhenNoBirthDate() {
		User user = new User();
		user.setId(1L);
		user.setDateOfBirth(null);
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void awardBirthdayBonus_ShouldNotAwardWhenNotBirthday() {
		User user = new User();
		user.setId(1L);
		user.setDateOfBirth(LocalDate.now().minusDays(1));
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void awardBirthdayBonus_ShouldNotAwardWhenAlreadyReceivedThisYear() {
		User user = new User();
		user.setId(1L);
		user.setDateOfBirth(LocalDate.now());
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setLastBirthdayBonusDate(LocalDate.now());

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void validatePointsRedemption_ShouldPassWhenValid() {
		Long userId = 1L;
		Integer pointsToUse = 150;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(250);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		bonusUserService.validatePointsRedemption(userId, pointsToUse);

		verify(bonusCardRepository).findByUserId(userId);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenInsufficientPoints() {
		Long userId = 1L;
		Integer pointsToUse = 300;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(250);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(InsufficientPointsException.class);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenPointsOutsideRange() {
		Long userId = 1L;
		Integer pointsToUse = 0;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(500);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(BonusValidationException.class);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;
		Integer pointsToUse = 100;

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(BonusCardNotFoundException.class);
	}

	@Test
	void addPoints_ShouldAddPointsToUser() {
		User user = new User();
		user.setId(1L);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(100);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Integer result = bonusUserService.addPoints(user, 50);

		assertThat(result).isEqualTo(150);
		assertThat(card.getPointsBalance()).isEqualTo(150);

		verify(bonusCardRepository).save(card);
		verify(bonusTransactionRepository).save(transactionCaptor.capture());
		assertThat(transactionCaptor.getValue().getType()).isEqualTo(BonusTransactionType.PROMOTION_BONUS);
		assertThat(transactionCaptor.getValue().getPointsChange()).isEqualTo(50);
	}

	@Test
	void addPoints_ShouldCreateNewCardIfNotExists() {
		User user = new User();
		user.setId(1L);

		BonusCard newCard = BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build();

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
		when(bonusCardRepository.save(any(BonusCard.class))).thenReturn(newCard);
		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Integer result = bonusUserService.addPoints(user, 100);

		assertThat(result).isEqualTo(100);
		assertThat(newCard.getPointsBalance()).isEqualTo(100);

		verify(bonusCardRepository, times(2)).save(any(BonusCard.class));
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void addPoints_ShouldThrowWhenPointsIsNull() {
		User user = new User();
		user.setId(1L);

		assertThatThrownBy(() -> bonusUserService.addPoints(user, null)).isInstanceOf(BonusValidationException.class);
	}

	@Test
	void addPoints_ShouldThrowWhenPointsIsZero() {
		User user = new User();
		user.setId(1L);

		assertThatThrownBy(() -> bonusUserService.addPoints(user, 0)).isInstanceOf(BonusValidationException.class);
	}

	@Test
	void addPoints_ShouldThrowWhenPointsIsNegative() {
		User user = new User();
		user.setId(1L);

		assertThatThrownBy(() -> bonusUserService.addPoints(user, -50)).isInstanceOf(BonusValidationException.class);
	}

	@Test
	void findOrCreateBonusCard_ShouldReturnExistingCard() {
		User user = new User();
		user.setId(1L);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		BonusCard result = bonusUserService.findOrCreateBonusCard(user);

		assertThat(result).isSameAs(card);
		verify(bonusCardRepository).findByUserId(user.getId());
		verify(bonusCardRepository, never()).save(any());
	}

	@Test
	void findOrCreateBonusCard_ShouldCreateNewCard() {
		User user = new User();
		user.setId(1L);

		BonusCard newCard = BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(false).build();

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
		when(bonusCardRepository.save(any(BonusCard.class))).thenReturn(newCard);

		BonusCard result = bonusUserService.findOrCreateBonusCard(user);

		assertThat(result).isNotNull();
		assertThat(result.getUser()).isEqualTo(user);
		assertThat(result.getPointsBalance()).isEqualTo(0);
		assertThat(result.isWelcomeBonusReceived()).isFalse();
		verify(bonusCardRepository).save(any(BonusCard.class));
	}

	@Test
	void createBonusTransaction_ShouldCreateAndUpdateBalance() {
		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(100);

		Integer pointsChange = 25;
		BonusTransactionType type = BonusTransactionType.PAYMENT_ACCRUAL;
		String referenceId = "PAYMENT_123";

		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BonusTransaction result = bonusUserService.createBonusTransaction(card, pointsChange, type, referenceId, null,
				null, null);

		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getPointsChange()).isEqualTo(pointsChange);
		assertThat(result.getReferenceId()).isEqualTo(referenceId);
		assertThat(card.getPointsBalance()).isEqualTo(125);

		verify(bonusCardRepository).save(card);
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void createBonusTransaction_ShouldCreateWithNegativePoints() {
		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(100);

		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BonusTransaction result = bonusUserService.createBonusTransaction(card, -50, BonusTransactionType.BOOKING_SPEND,
				"PAYMENT_1", null, null, null);

		assertThat(result).isNotNull();
		assertThat(result.getPointsChange()).isEqualTo(-50);
		assertThat(card.getPointsBalance()).isEqualTo(50);
	}

	@Test
	void calculateAccruedPointsForAmount_ShouldCalculateCorrectly() {
		BigDecimal purchaseAmount = new BigDecimal("1000");
		Integer result = bonusUserService.calculateAccruedPointsForAmount(purchaseAmount);
		assertThat(result).isEqualTo(50);
	}

	@Test
	void calculateAccruedPointsForAmount_ShouldReturnZeroWhenAmountIsNull() {
		Integer result = bonusUserService.calculateAccruedPointsForAmount(null);
		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculateAccruedPointsForAmount_ShouldReturnZeroWhenAmountIsZero() {
		Integer result = bonusUserService.calculateAccruedPointsForAmount(BigDecimal.ZERO);
		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculateAccruedPointsForAmount_ShouldReturnZeroWhenAmountIsNegative() {
		Integer result = bonusUserService.calculateAccruedPointsForAmount(new BigDecimal("-100"));
		assertThat(result).isEqualTo(0);
	}

	@Test
	void validateBonusPointsForBooking_ShouldPassWhenValid() {
		Long userId = 1L;
		Integer pointsToUse = 50;
		BigDecimal bookingTotalPrice = new BigDecimal("200");

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(100);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		bonusUserService.validateBonusPointsForBooking(userId, pointsToUse, bookingTotalPrice);
	}

	@Test
	void validateBonusPointsForBooking_ShouldThrowWhenDiscountExceedsMax() {
		Long userId = 1L;
		Integer pointsToUse = 150;
		BigDecimal bookingTotalPrice = new BigDecimal("200");

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(200);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		assertThatThrownBy(() -> bonusUserService.validateBonusPointsForBooking(userId, pointsToUse, bookingTotalPrice))
				.isInstanceOf(BonusValidationException.class);
	}

	@Test
	void getAvailablePointsForRedemption_ShouldReturnCorrectValue() {
		Long userId = 1L;
		BigDecimal bookingTotalPrice = new BigDecimal("200");

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(150);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		Integer result = bonusUserService.getAvailablePointsForRedemption(userId, bookingTotalPrice);

		assertThat(result).isEqualTo(100);
	}

	@Test
	void getAvailablePointsForRedemption_ShouldReturnZeroWhenNoBalance() {
		Long userId = 1L;
		BigDecimal bookingTotalPrice = new BigDecimal("200");

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(0);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		Integer result = bonusUserService.getAvailablePointsForRedemption(userId, bookingTotalPrice);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void findActiveRule_ShouldReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRules rule = new BonusRules();
		rule.setBonusType(type);
		rule.setActive(true);

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(type)).thenReturn(Optional.of(rule));

		BonusRules result = bonusUserService.findActiveRule(type);

		assertThat(result).isSameAs(rule);
	}

	@Test
	void findActiveRule_ShouldThrowWhenNotFound() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(type)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.findActiveRule(type)).isInstanceOf(BonusRuleNotFoundException.class);
	}
}