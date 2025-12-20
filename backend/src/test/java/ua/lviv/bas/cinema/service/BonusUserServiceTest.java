package ua.lviv.bas.cinema.service;

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
import ua.lviv.bas.cinema.dto.shared.PageResponse;
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
		BonusCard card = BonusCard.builder().id(1L).pointsBalance(250).welcomeBonusReceived(true).build();
		BonusCardResponse expectedResponse = BonusCardResponse.builder().id(1L).pointsBalance(250)
				.welcomeBonusReceived(true).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusMapper.toBonusCardResponse(card)).thenReturn(expectedResponse);

		BonusCardResponse result = bonusUserService.getBonusCard(userId);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusMapper).toBonusCardResponse(card);
	}

	@Test
	void getBalance_ShouldReturnBalanceResponse() {
		Long userId = 1L;
		BonusCard card = BonusCard.builder().id(1L).pointsBalance(250).build();

		BonusRules writeOffRule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_WRITE_OFF)
				.pointValue(BigDecimal.ONE).minPointsPerTransaction(50).maxPointsPerTransaction(300).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(writeOffRule));

		BonusBalanceResponse result = bonusUserService.getBalance(userId);

		assertThat(result.getPointsBalance()).isEqualTo(250);
		assertThat(result.getPointValue()).isEqualTo(BigDecimal.ONE);
		assertThat(result.getBalanceValue()).isEqualTo(BigDecimal.valueOf(250));
		assertThat(result.getMinUsablePoints()).isEqualTo(50);
		assertThat(result.getMaxUsablePoints()).isEqualTo(300);
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);
		BonusCard card = BonusCard.builder().id(1L).build();
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.PURCHASE_BONUS)
				.pointsChange(25).build();

		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);
		BonusTransactionResponse response = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusTransactionRepository.findByBonusCardOrderByCreatedAtDesc(card, pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(transaction)).thenReturn(response);

		PageResponse<BonusTransactionResponse> result = bonusUserService.getUserTransactions(userId, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isSameAs(response);
		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusTransactionRepository).findByBonusCardOrderByCreatedAtDesc(card, pageable);
	}

	@Test
	void awardWelcomeBonus_ShouldAwardWhenNotReceived() {
		User user = User.builder().id(1L).build();
		BonusCard card = BonusCard.builder().id(1L).user(user).pointsBalance(0).welcomeBonusReceived(false).build();

		BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.WELCOME_BONUS).points(100).build();

		BonusTransaction mockTransaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.WELCOME_BONUS)
				.pointsChange(100).build();

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.WELCOME_BONUS))
				.thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenReturn(mockTransaction);

		bonusUserService.awardWelcomeBonus(user);

		verify(bonusCardRepository, times(2)).save(card);
		assertThat(card.getWelcomeBonusReceived()).isTrue();
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}

	@Test
	void awardWelcomeBonus_ShouldNotAwardWhenAlreadyReceived() {
		User user = User.builder().id(1L).build();
		BonusCard card = BonusCard.builder().id(1L).user(user).welcomeBonusReceived(true).build();

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		bonusUserService.awardWelcomeBonus(user);

		verify(bonusRulesRepository, never()).findByBonusTypeAndIsActiveTrue(any());
		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void awardBirthdayBonus_ShouldNotAwardWhenNotVerified() {
		User user = User.builder().id(1L).dateOfBirth(LocalDate.of(1990, 5, 15))
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		bonusUserService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
		verify(bonusRulesRepository, never()).findByBonusTypeAndIsActiveTrue(any());
	}

	@Test
	void validatePointsRedemption_ShouldPassWhenValid() {
		Long userId = 1L;
		Integer pointsToUse = 150;

		BonusCard card = BonusCard.builder().id(1L).pointsBalance(250).build();

		BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_WRITE_OFF)
				.minPointsPerTransaction(50).maxPointsPerTransaction(300).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(rule));

		bonusUserService.validatePointsRedemption(userId, pointsToUse);

		verify(bonusCardRepository).findByUserId(userId);
		verify(bonusRulesRepository).findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF);
	}

	@Test
	void validatePointsRedemption_ShouldThrowWhenInsufficientPoints() {
		Long userId = 1L;
		Integer pointsToUse = 300;

		BonusCard card = BonusCard.builder().id(1L).pointsBalance(250).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF)).thenReturn(
				Optional.of(BonusRules.builder().minPointsPerTransaction(50).maxPointsPerTransaction(500).build()));

		assertThatThrownBy(() -> bonusUserService.validatePointsRedemption(userId, pointsToUse))
				.isInstanceOf(InsufficientPointsException.class);
	}

	@Test
	void calculateEarnedPoints_ShouldCalculateCorrectly() {
		BigDecimal purchaseAmount = new BigDecimal("1000");
		BigDecimal moneyRatio = new BigDecimal("0.1");

		BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS).moneyRatio(moneyRatio)
				.build();

		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isEqualTo(100);
	}

	@Test
	void calculateEarnedPoints_ShouldReturnZeroWhenNoRatio() {
		BigDecimal purchaseAmount = new BigDecimal("1000");

		BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS).moneyRatio(null).build();

		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(rule));

		Integer result = bonusUserService.calculateEarnedPoints(purchaseAmount);

		assertThat(result).isZero();
	}

	@Test
	void redeemPointsForPurchase_ShouldCreateTransactions() {
		Long userId = 1L;
		Integer pointsToUse = 100;
		BigDecimal purchaseAmount = new BigDecimal("500");

		Payment payment = Payment.builder().id(1L).build();
		BonusCard card = BonusCard.builder().id(1L).pointsBalance(250).build();

		BonusRules writeOffRule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_WRITE_OFF)
				.minPointsPerTransaction(50).maxPointsPerTransaction(300).build();

		BonusRules purchaseRule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS)
				.moneyRatio(new BigDecimal("0.1")).build();

		BonusTransaction writeOffTransaction = BonusTransaction.builder().id(1L)
				.type(BonusTransactionType.PURCHASE_WRITE_OFF).pointsChange(-100).build();

		BonusTransaction purchaseTransaction = BonusTransaction.builder().id(2L)
				.type(BonusTransactionType.PURCHASE_BONUS).pointsChange(50).build();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_WRITE_OFF))
				.thenReturn(Optional.of(writeOffRule));
		when(bonusRulesRepository.findByBonusTypeAndIsActiveTrue(BonusTransactionType.PURCHASE_BONUS))
				.thenReturn(Optional.of(purchaseRule));
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenReturn(writeOffTransaction,
				purchaseTransaction);

		BonusTransaction result = bonusUserService.redeemPointsForPurchase(userId, pointsToUse, payment,
				purchaseAmount);

		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(BonusTransactionType.PURCHASE_WRITE_OFF);
		assertThat(result.getPointsChange()).isEqualTo(-100);
		assertThat(card.getPointsBalance()).isEqualTo(200);

		verify(bonusTransactionRepository, times(2)).save(any(BonusTransaction.class));
		verify(bonusCardRepository, times(2)).save(card);
	}

	@Test
	void createBonusTransaction_ShouldCreateAndUpdateBalance() {
		BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

		Integer pointsChange = 25;
		BonusTransactionType type = BonusTransactionType.PURCHASE_BONUS;
		String referenceId = "PAYMENT_123";

		BonusTransaction mockTransaction = BonusTransaction.builder().id(1L).type(type).pointsChange(pointsChange)
				.referenceId(referenceId).build();

		when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenReturn(mockTransaction);
		when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BonusTransaction result = bonusUserService.createBonusTransaction(card, pointsChange, type, referenceId, null,
				null);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getPointsChange()).isEqualTo(pointsChange);
		assertThat(result.getReferenceId()).isEqualTo(referenceId);
		assertThat(card.getPointsBalance()).isEqualTo(125);

		verify(bonusCardRepository).save(card);
		verify(bonusTransactionRepository).save(any(BonusTransaction.class));
	}
}