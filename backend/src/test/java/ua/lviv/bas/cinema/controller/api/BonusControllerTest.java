package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.service.user.BonusUserService;

@ExtendWith(MockitoExtension.class)
class BonusControllerTest {

	@Mock
	private BonusUserService bonusUserService;

	@InjectMocks
	private BonusController bonusController;

	@Test
	void getMyBonusCard_ShouldReturnBonusCard() {
		Long userId = 1L;

		BonusCardResponse cardResponse = new BonusCardResponse();
		cardResponse.setId(1L);
		cardResponse.setUserId(userId);
		cardResponse.setPointsBalance(250);
		cardResponse.setWelcomeBonusReceived(true);

		when(bonusUserService.getBonusCard(userId)).thenReturn(cardResponse);

		BonusCardResponse response = bonusController.getMyBonusCard(userId);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals(userId, response.getUserId());
		assertEquals(250, response.getPointsBalance());
		assertEquals(true, response.getWelcomeBonusReceived());
	}

	@Test
	void getMyBonusCard_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;

		when(bonusUserService.getBonusCard(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBonusCard(userId));
	}

	@Test
	void getMyBalance_ShouldReturnBalance() {
		Long userId = 1L;

		BonusBalanceResponse balanceResponse = new BonusBalanceResponse();
		balanceResponse.setPointsBalance(250);
		balanceResponse.setPointValue(new BigDecimal("1.00"));
		balanceResponse.setBalanceValue(new BigDecimal("250.00"));
		balanceResponse.setMinUsablePoints(50);
		balanceResponse.setMaxUsablePoints(300);
		balanceResponse.setMinRedemptionValue(new BigDecimal("50.00"));
		balanceResponse.setMaxRedemptionValue(new BigDecimal("300.00"));

		when(bonusUserService.getBalance(userId)).thenReturn(balanceResponse);

		BonusBalanceResponse response = bonusController.getMyBalance(userId);

		assertNotNull(response);
		assertEquals(250, response.getPointsBalance());
		assertEquals(new BigDecimal("1.00"), response.getPointValue());
		assertEquals(new BigDecimal("250.00"), response.getBalanceValue());
		assertEquals(50, response.getMinUsablePoints());
		assertEquals(300, response.getMaxUsablePoints());
		assertEquals(new BigDecimal("50.00"), response.getMinRedemptionValue());
		assertEquals(new BigDecimal("300.00"), response.getMaxRedemptionValue());
	}

	@Test
	void getMyBalance_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;

		when(bonusUserService.getBalance(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBalance(userId));
	}

	@Test
	void getMyBalance_ShouldThrowWhenRuleNotFound() {
		Long userId = 1L;

		when(bonusUserService.getBalance(userId)).thenThrow(new BonusRuleNotFoundException(
				ua.lviv.bas.cinema.domain.enums.BonusTransactionType.PURCHASE_WRITE_OFF));

		assertThrows(BonusRuleNotFoundException.class, () -> bonusController.getMyBalance(userId));
	}

	@Test
	void getMyTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
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
		PageResponse<BonusTransactionResponse> pageResponse = PageResponse.of(page);

		when(bonusUserService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(pageResponse);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(2, response.getContent().size());
		assertEquals(2, response.getTotalElements());
		assertEquals(1L, response.getContent().get(0).getId());
		assertEquals("WELCOME_BONUS", response.getContent().get(0).getType());
		assertEquals(150, response.getContent().get(0).getPointsChange());
		assertEquals(2L, response.getContent().get(1).getId());
		assertEquals("PURCHASE_BONUS", response.getContent().get(1).getType());
		assertEquals(25, response.getContent().get(1).getPointsChange());
	}

	@Test
	void getMyTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
		PageResponse<BonusTransactionResponse> pageResponse = PageResponse.of(emptyPage);

		when(bonusUserService.getUserTransactions(eq(userId), any(Pageable.class))).thenReturn(pageResponse);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(0, response.getContent().size());
		assertEquals(0, response.getTotalElements());
		assertEquals(0, response.getTotalPages());
	}
}