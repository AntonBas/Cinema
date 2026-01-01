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
import ua.lviv.bas.cinema.domain.Payment;
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

@ExtendWith(MockitoExtension.class)
class BonusUserServiceTest {

	@Mock
	private BonusCardRepository bonusCardRepository;

	@Mock
	private BonusRulesRepository bonusRulesRepository;

	@Mock
	private BonusTransactionRepository bonusTransactionRepository;

	@Mock
	private BonusMapper bonusMapper;

	@InjectMocks
	private BonusUserService bonusUserService;

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

		BonusRules writeOffRule = new BonusRules();
		writeOffRule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		writeOffRule.setMinPointsPerTransaction(50);
		writeOffRule.setMaxPointsPerTransaction(300);
		writeOffRule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(writeOffRule));

		BonusBalanceResponse result = bonusUserService.getBalance(userId);

		assertThat(result.getPointsBalance()).isEqualTo(250);
		assertThat(result.getPointValue()).isEqualByComparingTo("1.00");
		assertThat(result.getBalanceValue()).isEqualByComparingTo("250.00");
		assertThat(result.getMinUsablePoints()).isEqualTo(50);
		assertThat(result.getMaxUsablePoints()).isEqualTo(300);
		assertThat(result.getMinRedemptionValue()).isEqualByComparingTo("50.00");
		assertThat(result.getMaxRedemptionValue()).isEqualByComparingTo("300.00");
	}

	@Test
	void getBalance_ShouldHandleNullMinMaxValues() {
		Long userId = 1L;
		User user = new User();
		user.setId(userId);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setUser(user);
		card.setPointsBalance(250);

		BonusRules writeOffRule = new BonusRules();
		writeOffRule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		writeOffRule.setMinPointsPerTransaction(null);
		writeOffRule.setMaxPointsPerTransaction(null);
		writeOffRule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(writeOffRule));

		BonusBalanceResponse result = bonusUserService.getBalance(userId);

		assertThat(result.getMinUsablePoints()).isEqualTo(0);
		assertThat(result.getMaxUsablePoints()).isEqualTo(0);
		assertThat(result.getMinRedemptionValue()).isEqualByComparingTo("0.00");
		assertThat(result.getMaxRedemptionValue()).isEqualByComparingTo("0.00");
	}

	@Test
	void getBalance_ShouldThrowWhenRuleNotFound() {
		Long userId = 1L;
		BonusCard card = new BonusCard();
		card.setId(1L);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.getBalance(userId)).isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusCard card = new BonusCard();
		card.setId(1L);

		BonusTransaction transaction = BonusTransaction.builder().id(1L).bonusCard(card)
				.type(BonusTransactionType.PURCHASE_BONUS).pointsChange(25).build();

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

		verify(bonusCardRepository, times(2)).save(card);
		assertThat(card.isWelcomeBonusReceived()).isTrue();
		assertThat(card.getPointsBalance()).isEqualTo(100);
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
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

		verify(bonusCardRepository, times(2)).save(card);
		assertThat(card.getPointsBalance()).isEqualTo(200);
		assertThat(card.getLastBirthdayBonusDate()).isEqualTo(LocalDate.now());
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
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

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		rule.setMinPointsPerTransaction(50);
		rule.setMaxPointsPerTransaction(300);
		rule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(rule));

		bonusUserService.validatePointsRedemption(userId, pointsToUse);

		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusRulesRepository).findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenInsufficientPoints() {
		Long userId = 1L;
		Integer pointsToUse = 300;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(250);

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		rule.setMinPointsPerTransaction(50);
		rule.setMaxPointsPerTransaction(500);
		rule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(rule));

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(InsufficientPointsException.class);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenPointsOutsideRange() {
		Long userId = 1L;
		Integer pointsToUse = 400;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(500);

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		rule.setMinPointsPerTransaction(50);
		rule.setMaxPointsPerTransaction(300);
		rule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(rule));

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Points must be between");
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
	void validatePointsRedemption_ShouldThrowWhenRuleNotFound() {
		Long userId = 1L;
		Integer pointsToUse = 100;

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(200);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void calculateEarnedPoints_ShouldCalculateCorrectly() {
		BigDecimal purchaseAmount = new BigDecimal("1000");
		BigDecimal moneyRatio = new BigDecimal("0.1");

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_BONUS);
		rule.setMoneyRatio(moneyRatio);
		rule.setActive(true);

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isEqualTo(100);
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenPurchaseAmountIsNull() {
		Integer result = bonusUserService.calculateEarnedPoints(null);

		assertThat(result).isZero();
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenPurchaseAmountIsZero() {
		Integer result = bonusUserService.calculateEarnedPoints(BigDecimal.ZERO);

		assertThat(result).isZero();
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenPurchaseAmountIsNegative() {
		Integer result = bonusUserService.calculateEarnedPoints(new BigDecimal("-100"));

		assertThat(result).isZero();
		verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenNoRatio() {
		BigDecimal purchaseAmount = new BigDecimal("1000");

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_BONUS);
		rule.setMoneyRatio(null);
		rule.setActive(true);

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isZero();
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenRatioIsZero() {
		BigDecimal purchaseAmount = new BigDecimal("1000");

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_BONUS);
		rule.setMoneyRatio(BigDecimal.ZERO);
		rule.setActive(true);

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isZero();
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenRuleNotActive() {
		BigDecimal purchaseAmount = new BigDecimal("1000");

		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.PURCHASE_BONUS);
		rule.setMoneyRatio(new BigDecimal("0.1"));
		rule.setActive(false);

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isZero();
	}

	@Test
	void calculateEarnedPoints_ShouldThrowWhenRuleNotFound() {
		BigDecimal purchaseAmount = new BigDecimal("1000");

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusUserService.calculateEarnedPoints(purchaseAmount))
				.isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void redeemPointsForPurchase_ShouldProcessRedemptionAndAwardPoints() {
		Long userId = 1L;
		Integer pointsToUse = 150;
		BigDecimal purchaseAmount = new BigDecimal("2000");

		Payment payment = new Payment();
		payment.setId(1L);

		BonusCard card = new BonusCard();
		card.setId(1L);
		card.setPointsBalance(250);

		BonusRules writeOffRule = new BonusRules();
		writeOffRule.setBonusType(BonusTransactionType.PURCHASE_WRITE_OFF);
		writeOffRule.setMinPointsPerTransaction(50);
		writeOffRule.setMaxPointsPerTransaction(300);
		writeOffRule.setActive(true);

		BonusRules purchaseRule = new BonusRules();
		purchaseRule.setBonusType(BonusTransactionType.PURCHASE_BONUS);
		purchaseRule.setMoneyRatio(new BigDecimal("0.05"));
		purchaseRule.setActive(true);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(writeOffRule));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(purchaseRule));

		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		BonusTransaction result = bonusUserService.redeemPointsForPurchase(userId, pointsToUse, payment,
				purchaseAmount);

		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(BonusTransactionType.PURCHASE_WRITE_OFF);
		assertThat(result.getPointsChange()).isEqualTo(-150);

		assertThat(card.getPointsBalance()).isEqualTo(200);
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
		BonusTransactionType type = BonusTransactionType.PURCHASE_BONUS;
		String referenceId = "PAYMENT_123";

		when(bonusTransactionRepository.save(any(BonusTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BonusTransaction result = bonusUserService.createBonusTransaction(card, pointsChange, type, referenceId, null,
				null);

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

		BonusTransaction result = bonusUserService.createBonusTransaction(card, -50,
				BonusTransactionType.PURCHASE_WRITE_OFF, "PAYMENT_1", null, null);

		assertThat(result).isNotNull();
		assertThat(result.getPointsChange()).isEqualTo(-50);
		assertThat(card.getPointsBalance()).isEqualTo(50);
	}
}