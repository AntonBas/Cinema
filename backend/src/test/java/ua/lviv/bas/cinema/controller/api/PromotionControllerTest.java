package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;
import ua.lviv.bas.cinema.service.user.PromotionService;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTest {

	@Mock
	private AdminPromotionService promotionService;

	@Mock
	private AdminPromotionService userPromotionService;

	@InjectMocks
	private PromotionController promotionController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		return user;
	}

	private Promotion createPromotionEntity(Long id, String title) {
		return Promotion.builder().id(id).title(title).bonusPoints(100).startDate(LocalDateTime.now().minusDays(1))
				.endDate(LocalDateTime.now().plusDays(5)).build();
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
		PromotionResponse promo3 = createPromotionResponse(3L, "Inactive Promo", 200);

		List<PromotionResponse> allPromotions = Arrays.asList(promo1, promo2, promo3);

		when(promotionService.getAllPromotions()).thenReturn(allPromotions);

		Promotion promo1Entity = createPromotionEntity(1L, "Active Promo 1");
		Promotion promo2Entity = createPromotionEntity(2L, "Active Promo 2");
		Promotion promo3Entity = createPromotionEntity(3L, "Inactive Promo");

		when(promotionService.findByIdOrThrow(1L)).thenReturn(promo1Entity);
		when(promotionService.isPromotionActive(promo1Entity)).thenReturn(true);
		when(userPromotionService.hasUserClaimedPromotion(user, 1L)).thenReturn(false);

		when(promotionService.findByIdOrThrow(2L)).thenReturn(promo2Entity);
		when(promotionService.isPromotionActive(promo2Entity)).thenReturn(true);
		when(userPromotionService.hasUserClaimedPromotion(user, 2L)).thenReturn(false);

		when(promotionService.findByIdOrThrow(3L)).thenReturn(promo3Entity);
		when(promotionService.isPromotionActive(promo3Entity)).thenReturn(false);

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertEquals("Active Promo 1", result.getBody().get(0).getTitle());
		assertEquals("Active Promo 2", result.getBody().get(1).getTitle());

		verify(promotionService).getAllPromotions();
	}

	@Test
	void getAvailablePromotions_ShouldFilterOutClaimedPromotions() {
		User user = createUser(1L, "test@example.com");

		PromotionResponse promo1 = createPromotionResponse(1L, "Not Claimed", 100);
		PromotionResponse promo2 = createPromotionResponse(2L, "Already Claimed", 150);

		List<PromotionResponse> allPromotions = Arrays.asList(promo1, promo2);

		when(promotionService.getAllPromotions()).thenReturn(allPromotions);

		Promotion promo1Entity = createPromotionEntity(1L, "Not Claimed");
		Promotion promo2Entity = createPromotionEntity(2L, "Already Claimed");

		when(promotionService.findByIdOrThrow(1L)).thenReturn(promo1Entity);
		when(promotionService.isPromotionActive(promo1Entity)).thenReturn(true);
		when(userPromotionService.hasUserClaimedPromotion(user, 1L)).thenReturn(false);

		when(promotionService.findByIdOrThrow(2L)).thenReturn(promo2Entity);
		when(promotionService.isPromotionActive(promo2Entity)).thenReturn(true);
		when(userPromotionService.hasUserClaimedPromotion(user, 2L)).thenReturn(true);

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().size());
		assertEquals("Not Claimed", result.getBody().get(0).getTitle());
	}

	@Test
	void getAvailablePromotions_ShouldReturnEmptyListWhenNoAvailable() {
		User user = createUser(1L, "test@example.com");

		when(promotionService.getAllPromotions()).thenReturn(Collections.emptyList());

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
	}

	@Test
	void getAvailablePromotions_ShouldHandlePromotionNotFoundGracefully() {
		User user = createUser(1L, "test@example.com");

		PromotionResponse promo = createPromotionResponse(1L, "Test Promo", 100);
		List<PromotionResponse> allPromotions = Arrays.asList(promo);

		when(promotionService.getAllPromotions()).thenReturn(allPromotions);

		when(promotionService.findByIdOrThrow(1L)).thenThrow(new PromotionNotFoundException(1L));

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
	}

	@Test
	void getUserPromotions_ShouldReturnUserPromotions() {
		User user = createUser(1L, "test@example.com");

		UserPromotionResponse userPromo1 = createUserPromotionResponse(1L, 1L, "Promo 1", 100);
		UserPromotionResponse userPromo2 = createUserPromotionResponse(2L, 2L, "Promo 2", 150);
		List<UserPromotionResponse> userPromotions = Arrays.asList(userPromo1, userPromo2);

		when(userPromotionService.getUserPromotions(user)).thenReturn(userPromotions);

		ResponseEntity<List<UserPromotionResponse>> result = promotionController.getUserPromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertEquals("Promo 1", result.getBody().get(0).getPromotionTitle());
		assertEquals("Promo 2", result.getBody().get(1).getPromotionTitle());

		verify(userPromotionService).getUserPromotions(user);
	}

	@Test
	void getUserPromotions_ShouldReturnEmptyList() {
		User user = createUser(1L, "test@example.com");

		when(userPromotionService.getUserPromotions(user)).thenReturn(Collections.emptyList());

		ResponseEntity<List<UserPromotionResponse>> result = promotionController.getUserPromotions(user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
	}

	@Test
	void claimPromotion_ShouldClaimSuccessfully() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);

		UserPromotionResponse response = createUserPromotionResponse(1L, 1L, "Test Promotion", 100);

		when(userPromotionService.claimPromotion(eq(request), eq(user))).thenReturn(response);

		ResponseEntity<UserPromotionResponse> result = promotionController.claimPromotion(request, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(1L, result.getBody().getPromotionId());
		assertEquals("Test Promotion", result.getBody().getPromotionTitle());
		assertEquals(100, result.getBody().getPointsAwarded());

		verify(userPromotionService).claimPromotion(request, user);
	}

	@Test
	void claimPromotion_ShouldThrowWhenNotActive() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);

		when(userPromotionService.claimPromotion(eq(request), eq(user)))
				.thenThrow(new PromotionNotActiveException("Test Promotion"));

		assertThrows(PromotionNotActiveException.class, () -> promotionController.claimPromotion(request, user));
	}

	@Test
	void claimPromotion_ShouldThrowWhenAlreadyClaimed() {
		User user = createUser(1L, "test@example.com");
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(1L);

		when(userPromotionService.claimPromotion(eq(request), eq(user)))
				.thenThrow(new AlreadyClaimedException("test@example.com", "Test Promotion"));

		assertThrows(AlreadyClaimedException.class, () -> promotionController.claimPromotion(request, user));
	}

	@Test
	void checkPromotionStatus_ShouldReturnTrueWhenAvailable() {
		User user = createUser(1L, "test@example.com");
		Long promotionId = 1L;

		when(userPromotionService.isPromotionAvailableForUser(user, promotionId)).thenReturn(true);

		ResponseEntity<Boolean> result = promotionController.checkPromotionStatus(promotionId, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody());

		verify(userPromotionService).isPromotionAvailableForUser(user, promotionId);
	}

	@Test
	void checkPromotionStatus_ShouldReturnFalseWhenNotAvailable() {
		User user = createUser(1L, "test@example.com");
		Long promotionId = 1L;

		when(userPromotionService.isPromotionAvailableForUser(user, promotionId)).thenReturn(false);

		ResponseEntity<Boolean> result = promotionController.checkPromotionStatus(promotionId, user);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(!result.getBody());
	}
}