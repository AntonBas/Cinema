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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
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

	@InjectMocks
	private BonusService bonusService;

	@Test
	void getCard_ShouldReturnCardResponse() {
		Long userId = 1L;
		BonusCard card = new BonusCard();
		card.setId(1L);
		BonusCardResponse response = new BonusCardResponse();

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusMapper.toBonusCardResponse(card)).thenReturn(response);

		BonusCardResponse result = bonusService.getCard(userId);

		assertThat(result).isEqualTo(response);
		verify(bonusCardRepository).findByUserId(userId);
	}

	@Test
	void getCard_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusService.getCard(userId)).isInstanceOf(BonusCardNotFoundException.class);
	}

	@Test
	void getBalance_ShouldReturnBalanceResponse() {
		Long userId = 1L;
		BonusCard card = new BonusCard();
		card.setPointsBalance(100);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));

		BonusBalanceResponse result = bonusService.getBalance(userId);

		assertThat(result).isNotNull();
		assertThat(result.getPointsBalance()).isEqualTo(100);
	}

	@Test
	void getTransactions_ShouldReturnPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);
		Page<BonusTransactionProjection> page = Page.empty();

		when(bonusTransactionRepository.findProjectionsByUserId(userId, pageable)).thenReturn(page);

		Page<BonusTransactionResponse> result = bonusService.getTransactions(userId, pageable);

		assertThat(result).isNotNull();
		verify(bonusTransactionRepository).findProjectionsByUserId(userId, pageable);
	}

	@Test
	void awardWelcomeBonus_ShouldAddPointsWhenNotReceived() {
		User user = new User();
		user.setId(1L);
		BonusCard card = new BonusCard();
		card.setWelcomeBonusReceived(false);
		BonusRules rule = new BonusRules();
		rule.setPoints(50);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.WELCOME_BONUS))
				.thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any())).thenReturn(card);
		when(bonusTransactionRepository.save(any())).thenReturn(new BonusTransaction());

		bonusService.awardWelcomeBonus(user);

		verify(bonusCardRepository, times(2)).save(card);
		verify(bonusTransactionRepository).save(any());
		assertThat(card.isWelcomeBonusReceived()).isTrue();
	}

	@Test
	void awardWelcomeBonus_ShouldSkipWhenAlreadyReceived() {
		User user = new User();
		user.setId(1L);
		BonusCard card = new BonusCard();
		card.setWelcomeBonusReceived(true);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		bonusService.awardWelcomeBonus(user);

		verify(bonusCardRepository, never()).save(any());
		verify(bonusTransactionRepository, never()).save(any());
	}

	@Test
	void awardBirthdayBonus_ShouldAddPointsWhenConditionsMet() {
		User user = new User();
		user.setId(1L);
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		user.setDateOfBirth(LocalDate.now());
		BonusCard card = new BonusCard();
		card.setLastBirthdayBonusDate(null);
		BonusRules rule = new BonusRules();
		rule.setPoints(100);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.BIRTHDAY_BONUS))
				.thenReturn(Optional.of(rule));
		when(bonusCardRepository.save(any())).thenReturn(card);
		when(bonusTransactionRepository.save(any())).thenReturn(new BonusTransaction());

		bonusService.awardBirthdayBonus(user);

		verify(bonusCardRepository, times(2)).save(card);
		verify(bonusTransactionRepository).save(any());
	}

	@Test
	void awardBirthdayBonus_ShouldSkipWhenNotVerified() {
		User user = new User();
		user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		bonusService.awardBirthdayBonus(user);

		verify(bonusCardRepository, never()).findByUserId(any());
	}

	@Test
	void addPoints_ShouldAddPoints() {
		User user = new User();
		user.setId(1L);
		BonusCard card = new BonusCard();
		card.setPointsBalance(100);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));
		when(bonusCardRepository.save(any())).thenReturn(card);
		when(bonusTransactionRepository.save(any())).thenReturn(new BonusTransaction());

		Integer result = bonusService.addPoints(user, 50);

		assertThat(result).isEqualTo(150);
		verify(bonusCardRepository).save(card);
		verify(bonusTransactionRepository).save(any());
	}

	@Test
	void addPoints_ShouldThrowWhenPointsInvalid() {
		User user = new User();

		assertThatThrownBy(() -> bonusService.addPoints(user, 0)).isInstanceOf(BonusValidationException.class);
	}

	@Test
	void validateRedemption_ShouldPassWhenSufficientPoints() {
		Long userId = 1L;
		BonusCard card = new BonusCard();
		card.setPointsBalance(100);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		bonusService.validateRedemption(userId, 50);
	}

	@Test
	void validateRedemption_ShouldThrowWhenInsufficientPoints() {
		Long userId = 1L;
		BonusCard card = new BonusCard();
		card.setPointsBalance(30);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));

		assertThatThrownBy(() -> bonusService.validateRedemption(userId, 50))
				.isInstanceOf(InsufficientPointsException.class);
	}

	@Test
	void calculatePoints_ShouldReturnZeroWhenAmountNull() {
		Integer result = bonusService.calculatePoints(null);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculatePoints_ShouldCalculateCorrectly() {
		BigDecimal amount = new BigDecimal("100");
		BonusRules rule = new BonusRules();
		rule.setMoneyRatio(new BigDecimal("1.5"));

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BonusTransactionType.PAYMENT_ACCRUAL))
				.thenReturn(Optional.of(rule));

		Integer result = bonusService.calculatePoints(amount);

		assertThat(result).isEqualTo(150);
	}

	@Test
	void getOrCreateCard_ShouldReturnExistingCard() {
		User user = new User();
		user.setId(1L);
		BonusCard card = new BonusCard();

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.of(card));

		BonusCard result = bonusService.getOrCreateCard(user);

		assertThat(result).isEqualTo(card);
		verify(bonusCardRepository, never()).save(any());
	}

	@Test
	void getOrCreateCard_ShouldCreateNewCard() {
		User user = new User();
		user.setId(1L);

		when(bonusCardRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
		when(bonusCardRepository.save(any())).thenReturn(new BonusCard());

		BonusCard result = bonusService.getOrCreateCard(user);

		assertThat(result).isNotNull();
		verify(bonusCardRepository).save(any());
	}

	@Test
	void getActiveRule_ShouldReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRules rule = new BonusRules();

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(type)).thenReturn(Optional.of(rule));

		BonusRules result = bonusService.getActiveRule(type);

		assertThat(result).isEqualTo(rule);
	}

	@Test
	void getActiveRule_ShouldThrowWhenNotFound() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;

		when(bonusRulesRepository.findByBonusTypeAndActiveTrue(type)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusService.getActiveRule(type)).isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void validatePointsForBooking_ShouldPassWhenValid() {
		Long userId = 1L;
		BigDecimal totalPrice = new BigDecimal("100");
		BonusCard card = new BonusCard();
		card.setPointsBalance(50);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		bonusService.validatePointsForBooking(userId, 30, totalPrice);
	}

	@Test
	void getAvailablePoints_ShouldReturnCorrectValue() {
		Long userId = 1L;
		BigDecimal totalPrice = new BigDecimal("100");
		BonusCard card = new BonusCard();
		card.setPointsBalance(200);

		when(bonusCardRepository.findByUserId(userId)).thenReturn(Optional.of(card));
		when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
		when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

		Integer result = bonusService.getAvailablePoints(userId, totalPrice);

		assertThat(result).isEqualTo(50);
	}
}