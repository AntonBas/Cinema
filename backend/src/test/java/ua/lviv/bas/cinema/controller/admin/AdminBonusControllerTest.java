package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.service.admin.BonusAdminService;

@ExtendWith(MockitoExtension.class)
class AdminBonusControllerTest {

	@Mock
	private BonusAdminService bonusAdminService;

	@InjectMocks
	private AdminBonusController adminBonusController;

	@Test
	void getAllBonusRules_ShouldReturnAllRules() {
		BonusRulesResponse rule1 = new BonusRulesResponse();
		rule1.setId(1L);
		rule1.setBonusType("WELCOME_BONUS");
		rule1.setPoints(150);
		rule1.setActive(true);
		rule1.setUpdatedAt(LocalDateTime.now());

		BonusRulesResponse rule2 = new BonusRulesResponse();
		rule2.setId(2L);
		rule2.setBonusType("BIRTHDAY_BONUS");
		rule2.setPoints(200);
		rule2.setActive(true);
		rule2.setUpdatedAt(LocalDateTime.now());

		when(bonusAdminService.getAllBonusRules()).thenReturn(List.of(rule1, rule2));

		List<BonusRulesResponse> response = adminBonusController.getAllBonusRules();

		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(1L, response.get(0).getId());
		assertEquals("WELCOME_BONUS", response.get(0).getBonusType());
		assertEquals(150, response.get(0).getPoints());
		assertEquals(2L, response.get(1).getId());
		assertEquals("BIRTHDAY_BONUS", response.get(1).getBonusType());
	}

	@Test
	void getAllBonusRules_ShouldReturnEmptyList() {
		when(bonusAdminService.getAllBonusRules()).thenReturn(List.of());

		List<BonusRulesResponse> response = adminBonusController.getAllBonusRules();

		assertNotNull(response);
		assertTrue(response.isEmpty());
	}

	@Test
	void getBonusRule_ShouldReturnRule() {
		BonusRulesResponse rule = new BonusRulesResponse();
		rule.setId(1L);
		rule.setBonusType("WELCOME_BONUS");
		rule.setPoints(150);
		rule.setActive(true);
		rule.setUpdatedAt(LocalDateTime.now());

		when(bonusAdminService.getBonusRule(BonusTransactionType.WELCOME_BONUS)).thenReturn(rule);

		BonusRulesResponse response = adminBonusController.getBonusRule(BonusTransactionType.WELCOME_BONUS);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("WELCOME_BONUS", response.getBonusType());
		assertEquals(150, response.getPoints());
		assertTrue(response.getActive());
	}

	@Test
	void getBonusRule_ShouldThrowWhenNotFound() {
		when(bonusAdminService.getBonusRule(BonusTransactionType.WELCOME_BONUS))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		assertThrows(BonusRuleNotFoundException.class,
				() -> adminBonusController.getBonusRule(BonusTransactionType.WELCOME_BONUS));
	}

	@Test
	void updateBonusRule_ShouldUpdateRule() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(200).active(true).build();

		BonusRulesResponse response = new BonusRulesResponse();
		response.setId(1L);
		response.setBonusType("WELCOME_BONUS");
		response.setPoints(200);
		response.setActive(true);
		response.setUpdatedAt(LocalDateTime.now());

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class)))
				.thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateBonusRule(BonusTransactionType.WELCOME_BONUS, request);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("WELCOME_BONUS", result.getBonusType());
		assertEquals(200, result.getPoints());
		assertTrue(result.getActive());
	}

	@Test
	void updateBonusRule_ShouldUpdatePurchaseBonusRule() {
		BonusRulesRequest request = BonusRulesRequest.builder().moneyRatio(new BigDecimal("0.1")).active(true).build();

		BonusRulesResponse response = new BonusRulesResponse();
		response.setId(1L);
		response.setBonusType("PURCHASE_BONUS");
		response.setMoneyRatio(new BigDecimal("0.1"));
		response.setActive(true);
		response.setUpdatedAt(LocalDateTime.now());

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.PURCHASE_BONUS), any(BonusRulesRequest.class)))
				.thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateBonusRule(BonusTransactionType.PURCHASE_BONUS, request);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("PURCHASE_BONUS", result.getBonusType());
		assertEquals(new BigDecimal("0.1"), result.getMoneyRatio());
		assertTrue(result.getActive());
	}

	@Test
	void updateBonusRule_ShouldThrowWhenNotFound() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(200).active(true).build();

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class)))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		assertThrows(BonusRuleNotFoundException.class,
				() -> adminBonusController.updateBonusRule(BonusTransactionType.WELCOME_BONUS, request));
	}

	@Test
	void resetBonusRule_ShouldResetRule() {
		BonusRulesResponse response = new BonusRulesResponse();
		response.setId(1L);
		response.setBonusType("WELCOME_BONUS");
		response.setPoints(150);
		response.setActive(true);
		response.setUpdatedAt(LocalDateTime.now());

		when(bonusAdminService.resetBonusRuleToDefaults(BonusTransactionType.WELCOME_BONUS)).thenReturn(response);

		BonusRulesResponse result = adminBonusController.resetBonusRule(BonusTransactionType.WELCOME_BONUS);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("WELCOME_BONUS", result.getBonusType());
		assertEquals(150, result.getPoints());
		assertTrue(result.getActive());
	}

	@Test
	void resetBonusRule_ShouldThrowWhenNotFound() {
		when(bonusAdminService.resetBonusRuleToDefaults(BonusTransactionType.WELCOME_BONUS))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		assertThrows(BonusRuleNotFoundException.class,
				() -> adminBonusController.resetBonusRule(BonusTransactionType.WELCOME_BONUS));
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction = new BonusTransactionResponse();
		transaction.setId(1L);
		transaction.setType("PURCHASE_BONUS");
		transaction.setPointsChange(25);
		transaction.setReferenceId("PAYMENT_123");
		transaction.setCreatedAt(LocalDateTime.now());
		transaction.setNewBalance(125);

		Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction), pageable, 1);

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

		Page<BonusTransactionResponse> result = adminBonusController.getUserTransactions(userId, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getTotalElements());
		assertEquals(1L, result.getContent().get(0).getId());
		assertEquals("PURCHASE_BONUS", result.getContent().get(0).getType());
		assertEquals(25, result.getContent().get(0).getPointsChange());
	}

	@Test
	void getAllTransactions_ShouldReturnPagedTransactions() {
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction1 = new BonusTransactionResponse();
		transaction1.setId(1L);
		transaction1.setType("WELCOME_BONUS");
		transaction1.setPointsChange(150);
		transaction1.setReferenceId("USER_1");
		transaction1.setCreatedAt(LocalDateTime.now());
		transaction1.setNewBalance(150);

		BonusTransactionResponse transaction2 = new BonusTransactionResponse();
		transaction2.setId(2L);
		transaction2.setType("PURCHASE_BONUS");
		transaction2.setPointsChange(25);
		transaction2.setReferenceId("PAYMENT_123");
		transaction2.setCreatedAt(LocalDateTime.now());
		transaction2.setNewBalance(175);

		Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

		when(bonusAdminService.getAllTransactions(any(Pageable.class))).thenReturn(page);

		Page<BonusTransactionResponse> result = adminBonusController.getAllTransactions(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals(2, result.getTotalElements());
		assertEquals("WELCOME_BONUS", result.getContent().get(0).getType());
		assertEquals("PURCHASE_BONUS", result.getContent().get(1).getType());
	}

	@Test
	void getTransactionsByType_ShouldReturnFilteredTransactions() {
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction = new BonusTransactionResponse();
		transaction.setId(1L);
		transaction.setType("WELCOME_BONUS");
		transaction.setPointsChange(150);
		transaction.setReferenceId("USER_1");
		transaction.setCreatedAt(LocalDateTime.now());
		transaction.setNewBalance(150);

		Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction), pageable, 1);

		when(bonusAdminService.getTransactionsByType(eq(BonusTransactionType.WELCOME_BONUS), any(Pageable.class)))
				.thenReturn(page);

		Page<BonusTransactionResponse> result = adminBonusController
				.getTransactionsByType(BonusTransactionType.WELCOME_BONUS, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getTotalElements());
		assertEquals("WELCOME_BONUS", result.getContent().get(0).getType());
		assertEquals(150, result.getContent().get(0).getPointsChange());
	}
}