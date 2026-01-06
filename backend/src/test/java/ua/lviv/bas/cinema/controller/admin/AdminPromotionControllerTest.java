package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@ExtendWith(MockitoExtension.class)
class AdminPromotionControllerTest {

	@Mock
	private AdminPromotionService promotionService;

	@InjectMocks
	private AdminPromotionController adminPromotionController;

	private PromotionResponse createPromotionResponse(Long id, String title, Integer bonusPoints) {
		PromotionResponse response = new PromotionResponse();
		response.setId(id);
		response.setTitle(title);
		response.setDescription("Test description");
		response.setBonusPoints(bonusPoints);
		response.setStartDate(LocalDateTime.now().plusDays(1));
		response.setEndDate(LocalDateTime.now().plusDays(10));
		return response;
	}

	@Test
	void createPromotion_ShouldReturnCreatedPromotion() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("New Promotion");
		request.setDescription("Test description");
		request.setBonusPoints(100);
		request.setStartDate(LocalDateTime.now().plusDays(1));
		request.setEndDate(LocalDateTime.now().plusDays(10));

		PromotionResponse response = createPromotionResponse(1L, "New Promotion", 100);

		when(promotionService.createPromotion(any(PromotionCreateRequest.class))).thenReturn(response);

		ResponseEntity<PromotionResponse> result = adminPromotionController.createPromotion(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(1L, result.getBody().getId());
		assertEquals("New Promotion", result.getBody().getTitle());
		assertEquals(100, result.getBody().getBonusPoints());
		verify(promotionService).createPromotion(request);
	}

	@Test
	void createPromotion_ShouldThrowWhenDuplicateTitle() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Existing Promotion");

		when(promotionService.createPromotion(any(PromotionCreateRequest.class)))
				.thenThrow(PromotionAlreadyExistsException.class);

		assertThrows(PromotionAlreadyExistsException.class, () -> adminPromotionController.createPromotion(request));
	}

	@Test
	void getPromotion_ShouldReturnPromotion() {
		Long promotionId = 1L;
		PromotionResponse response = createPromotionResponse(promotionId, "Test Promotion", 150);

		when(promotionService.getPromotionById(promotionId)).thenReturn(response);

		ResponseEntity<PromotionResponse> result = adminPromotionController.getPromotion(promotionId);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(promotionId, result.getBody().getId());
		assertEquals("Test Promotion", result.getBody().getTitle());
		assertEquals(150, result.getBody().getBonusPoints());
		verify(promotionService).getPromotionById(promotionId);
	}

	@Test
	void getPromotion_ShouldThrowWhenNotFound() {
		Long promotionId = 999L;

		when(promotionService.getPromotionById(promotionId)).thenThrow(new PromotionNotFoundException(promotionId));

		assertThrows(PromotionNotFoundException.class, () -> adminPromotionController.getPromotion(promotionId));
	}

	@Test
	void getAllPromotions_ShouldReturnAllPromotions() {
		PromotionResponse promotion1 = createPromotionResponse(1L, "Promo 1", 100);
		PromotionResponse promotion2 = createPromotionResponse(2L, "Promo 2", 200);
		List<PromotionResponse> promotions = Arrays.asList(promotion1, promotion2);

		when(promotionService.getAllPromotions()).thenReturn(promotions);

		ResponseEntity<List<PromotionResponse>> result = adminPromotionController.getAllPromotions();

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertEquals("Promo 1", result.getBody().get(0).getTitle());
		assertEquals("Promo 2", result.getBody().get(1).getTitle());
		verify(promotionService).getAllPromotions();
	}

	@Test
	void getAllPromotions_ShouldReturnEmptyList() {
		when(promotionService.getAllPromotions()).thenReturn(Collections.emptyList());

		ResponseEntity<List<PromotionResponse>> result = adminPromotionController.getAllPromotions();

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
	}

	@Test
	void getActivePromotions_ShouldReturnActivePromotions() {
		PromotionResponse activePromo1 = createPromotionResponse(1L, "Active Promo 1", 100);
		PromotionResponse activePromo2 = createPromotionResponse(2L, "Active Promo 2", 150);
		List<PromotionResponse> activePromotions = Arrays.asList(activePromo1, activePromo2);

		when(promotionService.getActivePromotions()).thenReturn(activePromotions);

		ResponseEntity<List<PromotionResponse>> result = adminPromotionController.getActivePromotions();

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertEquals("Active Promo 1", result.getBody().get(0).getTitle());
		assertEquals("Active Promo 2", result.getBody().get(1).getTitle());
		verify(promotionService).getActivePromotions();
	}

	@Test
	void getActivePromotions_ShouldReturnEmptyListWhenNoActive() {
		when(promotionService.getActivePromotions()).thenReturn(Collections.emptyList());

		ResponseEntity<List<PromotionResponse>> result = adminPromotionController.getActivePromotions();

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());
	}

	@Test
	void updatePromotion_ShouldReturnUpdatedPromotion() {
		Long promotionId = 1L;
		PromotionUpdateRequest request = new PromotionUpdateRequest();
		request.setTitle("Updated Title");
		request.setBonusPoints(250);

		PromotionResponse response = createPromotionResponse(promotionId, "Updated Title", 250);

		when(promotionService.updatePromotion(eq(promotionId), any(PromotionUpdateRequest.class))).thenReturn(response);

		ResponseEntity<PromotionResponse> result = adminPromotionController.updatePromotion(promotionId, request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(promotionId, result.getBody().getId());
		assertEquals("Updated Title", result.getBody().getTitle());
		assertEquals(250, result.getBody().getBonusPoints());
		verify(promotionService).updatePromotion(promotionId, request);
	}

	@Test
	void updatePromotion_ShouldThrowWhenNotFound() {
		Long promotionId = 999L;
		PromotionUpdateRequest request = new PromotionUpdateRequest();

		when(promotionService.updatePromotion(eq(promotionId), any(PromotionUpdateRequest.class)))
				.thenThrow(new PromotionNotFoundException(promotionId));

		assertThrows(PromotionNotFoundException.class,
				() -> adminPromotionController.updatePromotion(promotionId, request));
	}

	@Test
	void deletePromotion_ShouldReturnNoContent() {
		Long promotionId = 1L;

		ResponseEntity<Void> result = adminPromotionController.deletePromotion(promotionId);

		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(promotionService).deletePromotion(promotionId);
	}

	@Test
	void deletePromotion_ShouldThrowWhenNotFound() {
		Long promotionId = 999L;

		doThrow(new PromotionNotFoundException(promotionId)).when(promotionService).deletePromotion(promotionId);

		assertThrows(PromotionNotFoundException.class, () -> adminPromotionController.deletePromotion(promotionId));
	}

	@Test
	void deletePromotion_ShouldThrowWhenHasRedemptions() {
		Long promotionId = 1L;

		doThrow(new PromotionHasRedemptionsException(promotionId, 5)).when(promotionService)
				.deletePromotion(promotionId);

		assertThrows(PromotionHasRedemptionsException.class,
				() -> adminPromotionController.deletePromotion(promotionId));
	}
}