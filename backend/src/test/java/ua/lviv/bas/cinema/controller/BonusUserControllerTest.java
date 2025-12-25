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

import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.user.BonusUserService;

@ExtendWith(MockitoExtension.class)
class BonusUserControllerTest {

	@Mock
	private BonusUserService bonusUserService;

	@InjectMocks
	private BonusUserController bonusUserController;

	@Test
	void getMyBonusCard_ShouldReturnCard() {
		Long userId = 1L;
		BonusCardResponse expectedResponse = BonusCardResponse.builder().id(1L).userId(userId).pointsBalance(250)
				.welcomeBonusReceived(true).lastBirthdayBonusDate(LocalDateTime.now().toLocalDate()).build();

		when(bonusUserService.getBonusCard(userId)).thenReturn(expectedResponse);

		BonusCardResponse result = bonusUserController.getMyBonusCard(userId);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(userId, result.getUserId());
		assertEquals(250, result.getPointsBalance());
		assertEquals(true, result.getWelcomeBonusReceived());
		verify(bonusUserService).getBonusCard(userId);
	}

	@Test
	void getMyBalance_ShouldReturnBalance() {
		Long userId = 1L;
		BonusBalanceResponse expectedResponse = BonusBalanceResponse.builder().pointsBalance(250)
				.pointValue(BigDecimal.valueOf(1.00)).balanceValue(BigDecimal.valueOf(250.00)).minUsablePoints(50)
				.maxUsablePoints(300).minRedemptionValue(BigDecimal.valueOf(50.00))
				.maxRedemptionValue(BigDecimal.valueOf(300.00)).build();

		when(bonusUserService.getBalance(userId)).thenReturn(expectedResponse);

		BonusBalanceResponse result = bonusUserController.getMyBalance(userId);

		assertNotNull(result);
		assertEquals(250, result.getPointsBalance());
		assertEquals(BigDecimal.valueOf(1.00), result.getPointValue());
		assertEquals(BigDecimal.valueOf(250.00), result.getBalanceValue());
		assertEquals(50, result.getMinUsablePoints());
		assertEquals(300, result.getMaxUsablePoints());
		assertEquals(BigDecimal.valueOf(50.00), result.getMinRedemptionValue());
		assertEquals(BigDecimal.valueOf(300.00), result.getMaxRedemptionValue());
		verify(bonusUserService).getBalance(userId);
	}

	@Test
	void getMyTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("WELCOME_BONUS")
				.pointsChange(100).createdAt(LocalDateTime.now().minusDays(1)).build();

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of(transaction1, transaction2)).currentPage(0).totalPages(1).totalElements(2L).build();

		when(bonusUserService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusUserController.getMyTransactions(userId, pageable);

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
		verify(bonusUserService).getUserTransactions(userId, pageable);
	}

	@Test
	void getMyTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of()).currentPage(0).totalPages(0).totalElements(0L).build();

		when(bonusUserService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusUserController.getMyTransactions(userId, pageable);

		assertNotNull(result);
		assertEquals(0, result.getContent().size());
		assertEquals(0, result.getTotalElements());
		verify(bonusUserService).getUserTransactions(userId, pageable);
	}

	@Test
	void getMyTransactions_WithDifferentPageParameters_ShouldReturnCorrectPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(1, 10);

		BonusTransactionResponse transaction = BonusTransactionResponse.builder().id(1L).type("PURCHASE_BONUS")
				.pointsChange(25).createdAt(LocalDateTime.now()).build();

		PageResponse<BonusTransactionResponse> expectedResponse = PageResponse.<BonusTransactionResponse>builder()
				.content(List.of(transaction)).currentPage(1).totalPages(3).totalElements(25L).build();

		when(bonusUserService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(expectedResponse);

		PageResponse<BonusTransactionResponse> result = bonusUserController.getMyTransactions(userId, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getCurrentPage());
		assertEquals(3, result.getTotalPages());
		assertEquals(25L, result.getTotalElements());
		verify(bonusUserService).getUserTransactions(userId, pageable);
	}
}