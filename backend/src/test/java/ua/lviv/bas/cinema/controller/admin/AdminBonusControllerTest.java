package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.service.admin.AdminBonusService;

@ExtendWith(MockitoExtension.class)
public class AdminBonusControllerTest {

	@Mock
	private AdminBonusService bonusAdminService;

	@InjectMocks
	private AdminBonusController adminBonusController;

	@Test
	void getAllBonusRules_ShouldReturnRulesList() {
		BonusRulesResponse rule1 = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(100).build();

		BonusRulesResponse rule2 = BonusRulesResponse.builder().id(2L).bonusType("BIRTHDAY_BONUS").points(200).build();

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
		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(100)
				.build();

		when(bonusAdminService.getRule(type)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.getBonusRule(type);

		assertThat(result).isEqualTo(response);
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.getPoints()).isEqualTo(100);
	}

	@Test
	void updateBonusRule_ShouldUpdateAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesRequest request = new BonusRulesRequest();
		request.setPoints(200);
		request.setActive(true);

		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(200)
				.active(true).build();

		when(bonusAdminService.updateRule(eq(type), any(BonusRulesRequest.class))).thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateBonusRule(type, request);

		assertThat(result).isEqualTo(response);
		assertThat(result.getPoints()).isEqualTo(200);
		assertThat(result.getActive()).isTrue();
	}

	@Test
	void resetBonusRule_ShouldResetAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(150)
				.build();

		when(bonusAdminService.resetRuleToDefaults(type)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.resetBonusRule(type);

		assertThat(result).isEqualTo(response);
		assertThat(result.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.getPoints()).isEqualTo(150);
	}

	@Test
	void getUserTransactions_ShouldReturnPageResponse() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);
		Page<BonusTransactionResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> result = adminBonusController.getUserTransactions(userId, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void getAllTransactions_ShouldReturnPageResponse() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<BonusTransactionResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

		when(bonusAdminService.getAllTransactions(any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> result = adminBonusController.getAllTransactions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void getTransactionsByType_ShouldReturnPageResponse() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		Pageable pageable = PageRequest.of(0, 10);
		Page<BonusTransactionResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

		when(bonusAdminService.getTransactionsByType(eq(type), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> result = adminBonusController.getTransactionsByType(type, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void getUserTransactions_ShouldReturnTransactionsWithData() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.pointsChange("+100").createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("PAYMENT_ACCRUAL")
				.pointsChange("+50").createdAt(LocalDateTime.now()).build();

		List<BonusTransactionResponse> transactions = List.of(transaction1, transaction2);
		Page<BonusTransactionResponse> page = new PageImpl<>(transactions, pageable, 2);

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> result = adminBonusController.getUserTransactions(userId, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).getType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.getContent().get(1).getType()).isEqualTo("PAYMENT_ACCRUAL");
	}

	@Test
	void getTransactionsByType_ShouldReturnTransactionsWithData() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		Pageable pageable = PageRequest.of(0, 10);

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.pointsChange("+100").createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("WELCOME_BONUS")
				.pointsChange("+150").createdAt(LocalDateTime.now()).build();

		List<BonusTransactionResponse> transactions = List.of(transaction1, transaction2);
		Page<BonusTransactionResponse> page = new PageImpl<>(transactions, pageable, 2);

		when(bonusAdminService.getTransactionsByType(eq(type), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> result = adminBonusController.getTransactionsByType(type, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).getType()).isEqualTo("WELCOME_BONUS");
		assertThat(result.getContent().get(1).getType()).isEqualTo("WELCOME_BONUS");
	}
}