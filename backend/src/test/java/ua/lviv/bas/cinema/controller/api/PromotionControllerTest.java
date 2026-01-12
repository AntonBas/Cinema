package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.service.user.PromotionService;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerTest {

	@Mock
	private PromotionService promotionService;

	@InjectMocks
	private PromotionController promotionController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		return user;
	}

	private PromotionResponse createPromotionResponse(Long id, String title, Integer bonusPoints) {
		PromotionResponse response = new PromotionResponse();
		response.setId(id);
		response.setTitle(title);
		response.setBonusPoints(bonusPoints);
		response.setStartDate(LocalDateTime.now().minusDays(1));
		response.setEndDate(LocalDateTime.now().plusDays(5));
		return response;
	}

	private UserPromotionResponse createUserPromotionResponse(Long id, Long promotionId, String promotionTitle,
			Integer pointsAwarded) {
		return UserPromotionResponse.builder().id(id).promotionId(promotionId).promotionTitle(promotionTitle)
				.pointsAwarded(pointsAwarded).newBalance(250).claimedAt(LocalDateTime.now()).build();
	}

	@Test
	void getAvailablePromotions_ShouldReturnAvailablePromotions() {
		User user = createUser(1L, "test@example.com");
		PromotionResponse promo1 = createPromotionResponse(1L, "Active Promo 1", 100);
		PromotionResponse promo2 = createPromotionResponse(2L, "Active Promo 2", 150);
		PromotionResponse promo3 = createPromotionResponse(3L, "Active Promo 3", 200);
		List<PromotionResponse> availablePromotions = Arrays.asList(promo1, promo2, promo3);

		when(promotionService.getAvailablePromotions(user)).thenReturn(availablePromotions);

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(3, result.getBody().size());
		assertEquals("Active Promo 1", result.getBody().get(0).getTitle());
		assertEquals("Active Promo 2", result.getBody().get(1).getTitle());
		assertEquals("Active Promo 3", result.getBody().get(2).getTitle());
		verify(promotionService).getAvailablePromotions(user);
	}

	@Test
	void getAvailablePromotions_ShouldReturnEmptyListWhenNoAvailable() {
		User user = createUser(1L, "test@example.com");
		when(promotionService.getAvailablePromotions(user)).thenReturn(Collections.emptyList());

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
		verify(promotionService).getAvailablePromotions(user);
	}

	@Test
	void getAvailablePromotions_ShouldHandleServiceExceptionGracefully() {
		User user = createUser(1L, "test@example.com");
		when(promotionService.getAvailablePromotions(user)).thenThrow(new RuntimeException("Service error"));

		assertThrows(RuntimeException.class, () -> promotionController.getAvailablePromotions(user));
		verify(promotionService).getAvailablePromotions(user);
	}

	@Test
	void getUserPromotions_ShouldReturnUserPromotions() {
		User user = createUser(1L, "test@example.com");
		UserPromotionResponse userPromo1 = createUserPromotionResponse(1L, 1L, "Promo 1", 100);
		UserPromotionResponse userPromo2 = createUserPromotionResponse(2L, 2L, "Promo 2", 150);
		List<UserPromotionResponse> userPromotions = Arrays.asList(userPromo1, userPromo2);

		when(promotionService.getUserPromotions(user)).thenReturn(userPromotions);

		ResponseEntity<List<UserPromotionResponse>> result = promotionController.getUserPromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertEquals("Promo 1", result.getBody().get(0).getPromotionTitle());
		assertEquals("Promo 2", result.getBody().get(1).getPromotionTitle());
		verify(promotionService).getUserPromotions(user);
	}

	@Test
	void getUserPromotions_ShouldReturnEmptyList() {
		User user = createUser(1L, "test@example.com");
		when(promotionService.getUserPromotions(user)).thenReturn(Collections.emptyList());

		ResponseEntity<List<UserPromotionResponse>> result = promotionController.getUserPromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
		verify(promotionService).getUserPromotions(user);
	}

	@Test
	void claimPromotion_ShouldClaimSuccessfully() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);
		UserPromotionResponse response = createUserPromotionResponse(1L, 1L, "Test Promotion", 100);

		when(promotionService.claimPromotion(eq(request), eq(user))).thenReturn(response);

		ResponseEntity<UserPromotionResponse> result = promotionController.claimPromotion(request, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(1L, result.getBody().getPromotionId());
		assertEquals("Test Promotion", result.getBody().getPromotionTitle());
		assertEquals(100, result.getBody().getPointsAwarded());
		verify(promotionService).claimPromotion(request, user);
	}

	@Test
	void claimPromotion_ShouldThrowWhenNotActive() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);

		when(promotionService.claimPromotion(eq(request), eq(user)))
				.thenThrow(new PromotionNotActiveException("Test Promotion"));

		assertThrows(PromotionNotActiveException.class, () -> promotionController.claimPromotion(request, user));
		verify(promotionService).claimPromotion(request, user);
	}

	@Test
	void claimPromotion_ShouldThrowWhenAlreadyClaimed() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);

		when(promotionService.claimPromotion(eq(request), eq(user)))
				.thenThrow(new AlreadyClaimedException("test@example.com", "Test Promotion"));

		assertThrows(AlreadyClaimedException.class, () -> promotionController.claimPromotion(request, user));
		verify(promotionService).claimPromotion(request, user);
	}

	@Test
	void checkPromotionStatus_ShouldReturnTrueWhenAvailable() {
		User user = createUser(1L, "test@example.com");
		Long promotionId = 1L;

		when(promotionService.isPromotionAvailableForUser(user, promotionId)).thenReturn(true);

		ResponseEntity<Boolean> result = promotionController.checkPromotionStatus(promotionId, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody());
		verify(promotionService).isPromotionAvailableForUser(user, promotionId);
	}

	@Test
	void checkPromotionStatus_ShouldReturnFalseWhenNotAvailable() {
		User user = createUser(1L, "test@example.com");
		Long promotionId = 1L;

		when(promotionService.isPromotionAvailableForUser(user, promotionId)).thenReturn(false);

		ResponseEntity<Boolean> result = promotionController.checkPromotionStatus(promotionId, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertFalse(result.getBody());
		verify(promotionService).isPromotionAvailableForUser(user, promotionId);
	}

	@Test
	void checkPromotionStatus_ShouldThrowException() {
		User user = createUser(1L, "test@example.com");
		Long promotionId = 1L;

		when(promotionService.isPromotionAvailableForUser(user, promotionId))
				.thenThrow(new RuntimeException("Promotion not found"));

		assertThrows(RuntimeException.class, () -> promotionController.checkPromotionStatus(promotionId, user));
		verify(promotionService).isPromotionAvailableForUser(user, promotionId);
	}
}