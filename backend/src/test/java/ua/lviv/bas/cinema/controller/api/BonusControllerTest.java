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

import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class BonusControllerTest {

	@Mock
	private BonusService bonusService;

	@InjectMocks
	private BonusController bonusController;

	@Test
	void getMyBonusCard_ShouldReturnBonusCard() {
		Long userId = 1L;

		BonusCardResponse cardResponse = new BonusCardResponse(1L, 250, null, true, userId);

		when(bonusService.getCard(userId)).thenReturn(cardResponse);

		BonusCardResponse response = bonusController.getMyBonusCard(userId);

		assertNotNull(response);
		assertEquals(1L, response.id());
		assertEquals(userId, response.userId());
		assertEquals(250, response.pointsBalance());
		assertEquals(true, response.welcomeBonusReceived());
	}

	@Test
	void getMyBonusCard_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;

		when(bonusService.getCard(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBonusCard(userId));
	}

	@Test
	void getMyBalance_ShouldReturnBalance() {
		Long userId = 1L;

		BonusBalanceResponse balanceResponse = new BonusBalanceResponse(250, new BigDecimal("1.00"),
				new BigDecimal("250.00"), 100, 1000, new BigDecimal("100.00"), new BigDecimal("1000.00"));

		when(bonusService.getBalance(userId)).thenReturn(balanceResponse);

		BonusBalanceResponse response = bonusController.getMyBalance(userId);

		assertNotNull(response);
		assertEquals(250, response.pointsBalance());
		assertEquals(new BigDecimal("1.00"), response.pointValue());
		assertEquals(new BigDecimal("250.00"), response.balanceValue());
	}

	@Test
	void getMyBalance_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;
		when(bonusService.getBalance(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBalance(userId));
	}

	@Test
	void getMyTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BonusTransactionResponse transaction1 = new BonusTransactionResponse(1L, "WELCOME_BONUS", "+150",
				LocalDateTime.now(), 150);

		BonusTransactionResponse transaction2 = new BonusTransactionResponse(2L, "BOOKING_SPEND", "-25",
				LocalDateTime.now(), 125);

		Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

		when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(2, response.content().size());
		assertEquals(2, response.totalElements());
		assertEquals(1L, response.content().get(0).id());
		assertEquals("WELCOME_BONUS", response.content().get(0).type());
		assertEquals("+150", response.content().get(0).pointsChange());
		assertEquals(2L, response.content().get(1).id());
		assertEquals("BOOKING_SPEND", response.content().get(1).type());
		assertEquals("-25", response.content().get(1).pointsChange());
	}

	@Test
	void getMyTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(0, response.content().size());
		assertEquals(0, response.totalElements());
		assertEquals(true, response.empty());
	}
}