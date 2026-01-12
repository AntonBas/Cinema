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
public class AdminBonusControllerTest {

	@Mock
	private BonusAdminService bonusAdminService;

	@InjectMocks
	private AdminBonusController adminBonusController;

	@Test
	void getAllBonusRules_ShouldReturnAllRules() {
		BonusRulesResponse rule1 = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(150)
				.active(true).updatedAt(LocalDateTime.now()).build();

		BonusRulesResponse rule2 = BonusRulesResponse.builder().id(2L).bonusType("BIRTHDAY_BONUS").points(200)
				.active(true).updatedAt(LocalDateTime.now()).build();

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
		BonusRulesResponse rule = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(150)
				.active(true).updatedAt(LocalDateTime.now()).build();

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

		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(200)
				.active(true).updatedAt(LocalDateTime.now()).build();

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

		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("PURCHASE_BONUS")
				.moneyRatio(new BigDecimal("0.1")).active(true).updatedAt(LocalDateTime.now()).build();

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.PAYMENT_ACCRUAL), any(BonusRulesRequest.class)))
				.thenReturn(response);

		BonusRulesResponse result = adminBonusController.updateBonusRule(BonusTransactionType.PAYMENT_ACCRUAL, request);

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
		BonusRulesResponse response = BonusRulesResponse.builder().id(1L).bonusType("WELCOME_BONUS").points(150)
				.active(true).updatedAt(LocalDateTime.now()).build();

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

		BonusTransactionResponse transaction = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).referenceId("PAYMENT_123").createdAt(LocalDateTime.now()).newBalance(125).build();

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

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.pointsChange(150).referenceId("USER_1").createdAt(LocalDateTime.now()).newBalance(150).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("PURCHASE_BONUS")
				.pointsChange(25).referenceId("PAYMENT_123").createdAt(LocalDateTime.now()).newBalance(175).build();

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

		BonusTransactionResponse transaction = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.pointsChange(150).referenceId("USER_1").createdAt(LocalDateTime.now()).newBalance(150).build();

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

	@Test
	void getUserTransactions_ShouldHandleEmptyResult() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

		Page<BonusTransactionResponse> result = adminBonusController.getUserTransactions(userId, pageable);

		assertNotNull(result);
		assertTrue(result.getContent().isEmpty());
		assertEquals(0, result.getTotalElements());
	}

	@Test
	void getAllTransactions_ShouldHandleEmptyResult() {
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(bonusAdminService.getAllTransactions(any(Pageable.class))).thenReturn(emptyPage);

		Page<BonusTransactionResponse> result = adminBonusController.getAllTransactions(pageable);

		assertNotNull(result);
		assertTrue(result.getContent().isEmpty());
		assertEquals(0, result.getTotalElements());
	}

	@Test
	void getTransactionsByType_ShouldHandleEmptyResult() {
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(bonusAdminService.getTransactionsByType(eq(BonusTransactionType.WELCOME_BONUS), any(Pageable.class)))
				.thenReturn(emptyPage);

		Page<BonusTransactionResponse> result = adminBonusController
				.getTransactionsByType(BonusTransactionType.WELCOME_BONUS, pageable);

		assertNotNull(result);
		assertTrue(result.getContent().isEmpty());
		assertEquals(0, result.getTotalElements());
	}
}