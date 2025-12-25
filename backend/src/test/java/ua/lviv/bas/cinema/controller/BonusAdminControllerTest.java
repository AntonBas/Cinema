package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.service.admin.BonusAdminService;

@ExtendWith(MockitoExtension.class)
class BonusAdminControllerTest {

	@Mock
	private BonusAdminService bonusAdminService;

	@InjectMocks
	private BonusAdminController bonusAdminController;

	@Test
	void getAllBonusRules_ShouldReturnRulesList() {
		BonusRulesResponse rule1 = BonusRulesResponse.builder().bonusType("WELCOME_BONUS").points(100)
				.moneyRatio(BigDecimal.valueOf(0.1)).active(true).build();

		BonusRulesResponse rule2 = BonusRulesResponse.builder().bonusType("BIRTHDAY_BONUS").points(200)
				.moneyRatio(BigDecimal.valueOf(0.2)).active(true).build();

		when(bonusAdminService.getAllBonusRules()).thenReturn(List.of(rule1, rule2));

		List<BonusRulesResponse> result = bonusAdminController.getAllBonusRules();

		assertEquals(2, result.size());
		assertEquals("WELCOME_BONUS", result.get(0).getBonusType());
		assertEquals(100, result.get(0).getPoints());
		assertEquals("BIRTHDAY_BONUS", result.get(1).getBonusType());
		assertEquals(200, result.get(1).getPoints());
		verify(bonusAdminService).getAllBonusRules();
	}

	@Test
	void getAllBonusRules_ShouldReturnEmptyList() {
		when(bonusAdminService.getAllBonusRules()).thenReturn(List.of());

		List<BonusRulesResponse> result = bonusAdminController.getAllBonusRules();

		assertEquals(0, result.size());
		verify(bonusAdminService).getAllBonusRules();
	}

	@Test
	void getBonusRule_ShouldReturnRule() {
		BonusRulesResponse expectedResponse = BonusRulesResponse.builder().bonusType("WELCOME_BONUS").points(100)
				.moneyRatio(BigDecimal.valueOf(0.1)).active(true).build();

		when(bonusAdminService.getBonusRule(BonusTransactionType.WELCOME_BONUS)).thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminController.getBonusRule(BonusTransactionType.WELCOME_BONUS);

		assertNotNull(result);
		assertEquals("WELCOME_BONUS", result.getBonusType());
		assertEquals(100, result.getPoints());
		assertEquals(BigDecimal.valueOf(0.1), result.getMoneyRatio());
		assertEquals(true, result.getActive());
		verify(bonusAdminService).getBonusRule(BonusTransactionType.WELCOME_BONUS);
	}

	@Test
	void getBonusRule_ShouldThrowWhenNotFound() {
		when(bonusAdminService.getBonusRule(BonusTransactionType.WELCOME_BONUS))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		try {
			bonusAdminController.getBonusRule(BonusTransactionType.WELCOME_BONUS);
		} catch (BonusRuleNotFoundException e) {
			assertEquals("Bonus rule 'WELCOME_BONUS' is not configured", e.getMessage());
		}

		verify(bonusAdminService).getBonusRule(BonusTransactionType.WELCOME_BONUS);
	}

	@Test
	void updateBonusRule_ShouldUpdateAndReturnUpdated() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(150).moneyRatio(BigDecimal.valueOf(0.15))
				.active(true).build();

		BonusRulesResponse expectedResponse = BonusRulesResponse.builder().bonusType("WELCOME_BONUS").points(150)
				.moneyRatio(BigDecimal.valueOf(0.15)).active(true).build();

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class)))
				.thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminController.updateBonusRule(BonusTransactionType.WELCOME_BONUS, request);

		assertNotNull(result);
		assertEquals("WELCOME_BONUS", result.getBonusType());
		assertEquals(150, result.getPoints());
		assertEquals(BigDecimal.valueOf(0.15), result.getMoneyRatio());
		assertEquals(true, result.getActive());
		verify(bonusAdminService).updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class));
	}

	@Test
	void updateBonusRule_ShouldThrowWhenNotFound() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(150).build();

		when(bonusAdminService.updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class)))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		try {
			bonusAdminController.updateBonusRule(BonusTransactionType.WELCOME_BONUS, request);
		} catch (BonusRuleNotFoundException e) {
			assertEquals("Bonus rule 'WELCOME_BONUS' is not configured", e.getMessage());
		}

		verify(bonusAdminService).updateBonusRule(eq(BonusTransactionType.WELCOME_BONUS), any(BonusRulesRequest.class));
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("WELCOME_BONUS")
				.pointsChange(100).createdAt(LocalDateTime.now().minusDays(1)).build();

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of(transaction1, transaction2)).currentPage(0).totalPages(1).totalElements(2L).build();

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusAdminController.getUserTransactions(userId, pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals(1L, result.getContent().get(0).getId());
		assertEquals("PURCHASE_BONUS", result.getContent().get(0).getType());
		assertEquals(25, result.getContent().get(0).getPointsChange());
		assertEquals(2L, result.getContent().get(1).getId());
		assertEquals("WELCOME_BONUS", result.getContent().get(1).getType());
		assertEquals(100, result.getContent().get(1).getPointsChange());
		assertEquals(0, result.getCurrentPage());
		assertEquals(1, result.getTotalPages());
		assertEquals(2L, result.getTotalElements());
		verify(bonusAdminService).getUserTransactions(userId, pageable);
	}

	@Test
	void getUserTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of()).currentPage(0).totalPages(0).totalElements(0L).build();

		when(bonusAdminService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusAdminController.getUserTransactions(userId, pageable);

		assertNotNull(result);
		assertEquals(0, result.getContent().size());
		assertEquals(0, result.getTotalElements());
		verify(bonusAdminService).getUserTransactions(userId, pageable);
	}

	@Test
	void getAllTransactions_ShouldReturnAllTransactions() {
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.pointsChange(100).createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("PURCHASE_BONUS")
				.pointsChange(25).createdAt(LocalDateTime.now().minusDays(1)).build();

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of(transaction1, transaction2)).currentPage(0).totalPages(1).totalElements(2L).build();

		when(bonusAdminService.getAllTransactions(any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusAdminController.getAllTransactions(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals(1L, result.getContent().get(0).getId());
		assertEquals("WELCOME_BONUS", result.getContent().get(0).getType());
		assertEquals(100, result.getContent().get(0).getPointsChange());
		assertEquals(2L, result.getContent().get(1).getId());
		assertEquals("PURCHASE_BONUS", result.getContent().get(1).getType());
		assertEquals(25, result.getContent().get(1).getPointsChange());
		assertEquals(0, result.getCurrentPage());
		assertEquals(1, result.getTotalPages());
		assertEquals(2L, result.getTotalElements());
		verify(bonusAdminService).getAllTransactions(pageable);
	}

	@Test
	void getAllTransactions_ShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20);

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of()).currentPage(0).totalPages(0).totalElements(0L).build();

		when(bonusAdminService.getAllTransactions(any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusAdminController.getAllTransactions(pageable);

		assertNotNull(result);
		assertEquals(0, result.getContent().size());
		assertEquals(0, result.getTotalElements());
		verify(bonusAdminService).getAllTransactions(pageable);
	}
}