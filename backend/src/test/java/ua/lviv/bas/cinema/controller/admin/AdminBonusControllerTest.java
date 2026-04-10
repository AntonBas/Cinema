package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.service.bonus.AdminBonusService;

@ExtendWith(MockitoExtension.class)
public class AdminBonusControllerTest {

	@Mock
	private AdminBonusService bonusAdminService;

	@InjectMocks
	private AdminBonusController adminBonusController;

	@Test
	void getRulesShouldReturnRulesList() {
		BonusRulesResponse rule1 = new BonusRulesResponse(1L, BonusTransactionType.WELCOME_BONUS, 100, null, null, null,
				true);
		BonusRulesResponse rule2 = new BonusRulesResponse(2L, BonusTransactionType.BIRTHDAY_BONUS, 200, null, null,
				null, true);

		List<BonusRulesResponse> rules = List.of(rule1, rule2);

		when(bonusAdminService.getRules()).thenReturn(rules);

		List<BonusRulesResponse> result = adminBonusController.getRules();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		assertThat(result).contains(rule1, rule2);
	}

	@Test
	void getRulesWhenNoRulesExistShouldReturnEmptyList() {
		List<BonusRulesResponse> emptyList = Collections.emptyList();
		when(bonusAdminService.getRules()).thenReturn(emptyList);

		List<BonusRulesResponse> result = adminBonusController.getRules();

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	void updateRuleShouldUpdateAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesRequest request = new BonusRulesRequest(200, null, null, null, true);

		BonusRulesResponse response = new BonusRulesResponse(1L, BonusTransactionType.WELCOME_BONUS, 200, null, null,
				null, true);

		when(bonusAdminService.updateRule(eq(type), any(BonusRulesRequest.class))).thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateRule(type, request);

		assertThat(result).isEqualTo(response);
		assertThat(result.points()).isEqualTo(200);
		assertThat(result.active()).isTrue();
	}

	@Test
	void resetRuleShouldResetAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesResponse response = new BonusRulesResponse(1L, BonusTransactionType.WELCOME_BONUS, 150, null, null,
				null, true);

		when(bonusAdminService.resetRuleToDefaults(type)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.resetRule(type);

		assertThat(result).isEqualTo(response);
		assertThat(result.bonusType()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
		assertThat(result.points()).isEqualTo(150);
	}
}