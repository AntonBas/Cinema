package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;

@ExtendWith(MockitoExtension.class)
public class AdminBonusServiceTest {

	@Mock
	private BonusRulesRepository bonusRulesRepository;
	@Mock
	private BonusMapper bonusMapper;
	@Mock
	private BonusProperties bonusProperties;
	@InjectMocks
	private AdminBonusService service;

	private final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
	private final BonusTransactionType BIRTHDAY = BonusTransactionType.BIRTHDAY_BONUS;
	private final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;
	private final BonusTransactionType ACCRUAL = BonusTransactionType.PAYMENT_ACCRUAL;
	private final BonusTransactionType REFUND = BonusTransactionType.REFUND_RETURN;
	private final BonusTransactionType CANCEL = BonusTransactionType.BOOKING_CANCEL;
	private final BonusTransactionType PROMOTION = BonusTransactionType.PROMOTION_BONUS;

	@Test
	void getAllRules_ReturnsOnlyRuleTypes() {
		BonusRules ruleWelcome = new BonusRules();
		ruleWelcome.setBonusType(WELCOME);
		BonusRules ruleBirthday = new BonusRules();
		ruleBirthday.setBonusType(BIRTHDAY);
		BonusRules ruleSpend = new BonusRules();
		ruleSpend.setBonusType(SPEND);
		BonusRules ruleAccrual = new BonusRules();
		ruleAccrual.setBonusType(ACCRUAL);
		BonusRules ruleRefund = new BonusRules();
		ruleRefund.setBonusType(REFUND);
		BonusRules ruleCancel = new BonusRules();
		ruleCancel.setBonusType(CANCEL);
		BonusRules rulePromotion = new BonusRules();
		rulePromotion.setBonusType(PROMOTION);

		when(bonusRulesRepository.findAll()).thenReturn(
				List.of(ruleWelcome, ruleBirthday, ruleSpend, ruleAccrual, ruleRefund, ruleCancel, rulePromotion));
		when(bonusMapper.toBonusRulesResponse(any())).thenAnswer(inv -> {
			BonusRules rule = inv.getArgument(0);
			return new BonusRulesResponse(1L, rule.getBonusType().name(), null, null, null, null, null, null);
		});

		List<BonusRulesResponse> result = service.getAllRules();

		assertThat(result).hasSize(4);
		assertThat(result).extracting(BonusRulesResponse::bonusType).containsExactlyInAnyOrder("WELCOME_BONUS",
				"BIRTHDAY_BONUS", "BOOKING_SPEND", "PAYMENT_ACCRUAL");
	}

	@Test
	void getAllRules_WhenEmpty_ReturnsEmptyList() {
		when(bonusRulesRepository.findAll()).thenReturn(List.of());

		List<BonusRulesResponse> result = service.getAllRules();

		assertThat(result).isEmpty();
		verify(bonusRulesRepository).findAll();
	}

	@Test
	void getRule_ReturnsRule() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", null, null, null, null, null, null);

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.getRule(WELCOME);

		assertThat(result).isEqualTo(response);
		verify(bonusRulesRepository).findByBonusType(WELCOME);
	}

	@Test
	void getRule_WhenNotFound_ThrowsException() {
		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getRule(WELCOME)).isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void getRule_WhenTypeNotSupported_ThrowsException() {
		assertThatThrownBy(() -> service.getRule(REFUND)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No bonus rule configuration for type: REFUND_RETURN");
	}

	@Test
	void updateRule_Success() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(WELCOME);
		BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, true);
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", 100, null, null, null, null, null);

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.updateRule(WELCOME, request);

		assertThat(result).isEqualTo(response);
		verify(bonusMapper).updateBonusRulesFromRequest(request, rule);
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void updateRule_WhenTypeNotSupported_ThrowsException() {
		BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, true);

		assertThatThrownBy(() -> service.updateRule(REFUND, request)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No bonus rule configuration for type: REFUND_RETURN");
	}

	@Test
	void updateRule_ForBookingSpend_ValidatesPoints() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(SPEND);

		BonusRulesRequest request = new BonusRulesRequest(null, null, 100, 50, null);

		when(bonusRulesRepository.findByBonusType(SPEND)).thenReturn(Optional.of(rule));

		doAnswer(invocation -> {
			BonusRulesRequest req = invocation.getArgument(0);
			BonusRules r = invocation.getArgument(1);
			r.setMinPointsPerTransaction(req.minPointsPerTransaction());
			r.setMaxPointsPerTransaction(req.maxPointsPerTransaction());
			return null;
		}).when(bonusMapper).updateBonusRulesFromRequest(any(BonusRulesRequest.class), any(BonusRules.class));

		assertThatThrownBy(() -> service.updateRule(SPEND, request)).isInstanceOf(InvalidMinMaxPointsException.class);

		verify(bonusRulesRepository, never()).save(any());
	}

	@Test
	void resetRuleToDefaults_WhenDefaultsExist_UpdatesRule() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", null, null, null, null, null, null);

		BonusProperties.RuleDefaults defaults = new BonusProperties.RuleDefaults();
		defaults.setPoints(200);
		defaults.setMoneyRatio(new BigDecimal("0.10"));
		defaults.setMinPoints(50);
		defaults.setMaxPoints(500);

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusProperties.getDefaults()).thenReturn(Map.of(WELCOME, defaults));
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.resetRuleToDefaults(WELCOME);

		assertThat(result).isEqualTo(response);
		assertThat(rule.getPoints()).isEqualTo(200);
		assertThat(rule.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
		assertThat(rule.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(rule.getMaxPointsPerTransaction()).isEqualTo(500);
		assertThat(rule.getActive()).isTrue();
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void resetRuleToDefaults_WhenDefaultsNull_DoesNotUpdate() {
		BonusRules rule = new BonusRules();
		rule.setPoints(100);
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", 100, null, null, null, null, null);

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusProperties.getDefaults()).thenReturn(Map.of());
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.resetRuleToDefaults(WELCOME);

		assertThat(result).isEqualTo(response);
		assertThat(rule.getPoints()).isEqualTo(100);
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void resetRuleToDefaults_WhenTypeNotSupported_ThrowsException() {
		assertThatThrownBy(() -> service.resetRuleToDefaults(REFUND)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No bonus rule configuration for type: REFUND_RETURN");
	}
}