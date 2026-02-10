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

import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse.BookingDetails;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
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

		BonusCardResponse cardResponse = BonusCardResponse.builder().id(1L).userId(userId).pointsBalance(250)
				.welcomeBonusReceived(true).build();

		when(bonusService.getCard(userId)).thenReturn(cardResponse);

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

		when(bonusService.getCard(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBonusCard(userId));
	}

	@Test
	void getMyBalance_ShouldReturnBalance() {
		Long userId = 1L;

		BonusBalanceResponse balanceResponse = BonusBalanceResponse.builder().pointsBalance(250)
				.pointValue(new BigDecimal("1.00")).balanceValue(new BigDecimal("250.00")).build();

		when(bonusService.getBalance(userId)).thenReturn(balanceResponse);

		BonusBalanceResponse response = bonusController.getMyBalance(userId);

		assertNotNull(response);
		assertEquals(250, response.getPointsBalance());
		assertEquals(new BigDecimal("1.00"), response.getPointValue());
		assertEquals(new BigDecimal("250.00"), response.getBalanceValue());
	}

	@Test
	void getMyBalance_ShouldThrowWhenCardNotFound() {
		Long userId = 1L;
		when(bonusService.getBalance(userId)).thenThrow(new BonusCardNotFoundException(userId));

		assertThrows(BonusCardNotFoundException.class, () -> bonusController.getMyBalance(userId));
	}

	@Test
	void getMyBalance_ShouldThrowWhenRuleNotFound() {
		Long userId = 1L;
		when(bonusService.getBalance(userId))
				.thenThrow(new BonusRuleNotFoundException(BonusTransactionType.WELCOME_BONUS));

		assertThrows(BonusRuleNotFoundException.class, () -> bonusController.getMyBalance(userId));
	}

	@Test
	void getMyTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		BookingDetails bookingDetails = BookingDetails.builder().movieTitle("Interstellar").bookingReference("BK-12345")
				.cinemaHall("Hall 1").sessionDateTime(LocalDateTime.now().plusDays(7)).build();

		BonusTransactionResponse transaction1 = BonusTransactionResponse.builder().id(1L).type("WELCOME_BONUS")
				.typeDisplay("Welcome Bonus").pointsChange("+150").createdAt(LocalDateTime.now()).newBalance(150)
				.build();

		BonusTransactionResponse transaction2 = BonusTransactionResponse.builder().id(2L).type("BOOKING_SPEND")
				.typeDisplay("Booking Spend").pointsChange("-25").createdAt(LocalDateTime.now()).newBalance(125)
				.bookingDetails(bookingDetails).build();

		Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

		when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(2, response.getContent().size());
		assertEquals(2, response.getTotalElements());
		assertEquals(1L, response.getContent().get(0).getId());
		assertEquals("WELCOME_BONUS", response.getContent().get(0).getType());
		assertEquals("Welcome Bonus", response.getContent().get(0).getTypeDisplay());
		assertEquals("+150", response.getContent().get(0).getPointsChange());
		assertEquals(2L, response.getContent().get(1).getId());
		assertEquals("BOOKING_SPEND", response.getContent().get(1).getType());
		assertEquals("-25", response.getContent().get(1).getPointsChange());
		assertNotNull(response.getContent().get(1).getBookingDetails());
		assertEquals("Interstellar", response.getContent().get(1).getBookingDetails().getMovieTitle());
	}

	@Test
	void getMyTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);

		Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

		PageResponse<BonusTransactionResponse> response = bonusController.getMyTransactions(userId, pageable);

		assertNotNull(response);
		assertEquals(0, response.getContent().size());
		assertEquals(0, response.getTotalElements());
		assertEquals(true, response.isEmpty());
	}
}