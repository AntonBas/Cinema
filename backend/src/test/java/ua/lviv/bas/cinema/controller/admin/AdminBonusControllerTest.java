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
	void getAllBonusRules_ShouldReturnRulesList() {
		BonusRulesResponse rule1 = new BonusRulesResponse(1L, "WELCOME_BONUS", 100, null, null, null, true);
		BonusRulesResponse rule2 = new BonusRulesResponse(2L, "BIRTHDAY_BONUS", 200, null, null, null, true);

		List<BonusRulesResponse> rules = List.of(rule1, rule2);

		when(bonusAdminService.getAllRules()).thenReturn(rules);

		List<BonusRulesResponse> result = adminBonusController.getAllBonusRules();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		assertThat(result).contains(rule1, rule2);
	}

	@Test
	void getAllBonusRules_WhenNoRulesExist_ShouldReturnEmptyList() {
		List<BonusRulesResponse> emptyList = Collections.emptyList();
		when(bonusAdminService.getAllRules()).thenReturn(emptyList);

		List<BonusRulesResponse> result = adminBonusController.getAllBonusRules();

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	@Test
	void getBonusRule_ShouldReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", 100, null, null, null, true);

		when(bonusAdminService.getRule(type)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.getBonusRule(type);

		assertThat(result).isEqualTo(response);
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.bonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.points()).isEqualTo(100);
	}

	@Test
	void updateBonusRule_ShouldUpdateAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesRequest request = new BonusRulesRequest(200, null, null, null, true);

		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", 200, null, null, null, true);

		when(bonusAdminService.updateRule(eq(type), any(BonusRulesRequest.class))).thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateBonusRule(type, request);

		assertThat(result).isEqualTo(response);
		assertThat(result.points()).isEqualTo(200);
		assertThat(result.active()).isTrue();
	}

	@Test
	void resetBonusRule_ShouldResetAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesResponse response = new BonusRulesResponse(1L, "WELCOME_BONUS", 150, null, null, null, true);

		when(bonusAdminService.resetRuleToDefaults(type)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.resetBonusRule(type);

		assertThat(result).isEqualTo(response);
		assertThat(result.bonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.points()).isEqualTo(150);
	}
}