package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.service.promotion.AdminPromotionService;

@ExtendWith(MockitoExtension.class)
public class AdminPromotionControllerTest {

	@Mock
	private AdminPromotionService promotionService;
	@InjectMocks
	private AdminPromotionController controller;

	private final Long PROMOTION_ID = 1L;
	private final String TITLE = "Test Promotion";
	private final Integer BONUS_POINTS = 100;
	private final Pageable pageable = PageRequest.of(0, 10);

	private PromotionResponse createPromotionResponse() {
		return new PromotionResponse(PROMOTION_ID, TITLE, null, BONUS_POINTS, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(10));
	}

	private PromotionAdminResponse createAdminResponse() {
		return new PromotionAdminResponse(PROMOTION_ID, TITLE, BONUS_POINTS, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(10));
	}

	@Test
	void createPromotion_ReturnsCreated() {
		PromotionCreateRequest request = new PromotionCreateRequest(TITLE, null, BONUS_POINTS,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
		PromotionResponse response = createPromotionResponse();

		when(promotionService.createPromotion(any(PromotionCreateRequest.class))).thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.createPromotion(request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().id()).isEqualTo(PROMOTION_ID);
		verify(promotionService).createPromotion(request);
	}

	@Test
	void getPromotion_ReturnsOk() {
		PromotionResponse response = createPromotionResponse();

		when(promotionService.getPromotionById(PROMOTION_ID)).thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.getPromotion(PROMOTION_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().id()).isEqualTo(PROMOTION_ID);
		verify(promotionService).getPromotionById(PROMOTION_ID);
	}

	@Test
	void getPromotion_ThrowsWhenNotFound() {
		when(promotionService.getPromotionById(PROMOTION_ID)).thenThrow(new PromotionNotFoundException(PROMOTION_ID));

		assertThatThrownBy(() -> controller.getPromotion(PROMOTION_ID)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getAllPromotions_ReturnsOk() {
		PromotionAdminResponse adminResponse = createAdminResponse();
		PageResponse<PromotionAdminResponse> pageResponse = new PageResponse<>(Arrays.asList(adminResponse), 0, 10, 1,
				1, true, true, false, false, false, 1, null);

		when(promotionService.getAllPromotions(pageable)).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PromotionAdminResponse>> result = controller.getAllPromotions(pageable);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().content()).hasSize(1);
		assertThat(result.getBody().content().get(0).id()).isEqualTo(PROMOTION_ID);
		verify(promotionService).getAllPromotions(pageable);
	}

	@Test
	void updatePromotion_ReturnsOk() {
		PromotionUpdateRequest request = new PromotionUpdateRequest(TITLE, null, BONUS_POINTS,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
		PromotionResponse response = createPromotionResponse();

		when(promotionService.updatePromotion(eq(PROMOTION_ID), any(PromotionUpdateRequest.class)))
				.thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.updatePromotion(PROMOTION_ID, request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().id()).isEqualTo(PROMOTION_ID);
		verify(promotionService).updatePromotion(PROMOTION_ID, request);
	}

	@Test
	void deletePromotion_ReturnsNoContent() {
		ResponseEntity<Void> result = controller.deletePromotion(PROMOTION_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(promotionService).deletePromotion(PROMOTION_ID);
	}

	@Test
	void deletePromotion_ThrowsWhenNotFound() {
		doThrow(new PromotionNotFoundException(PROMOTION_ID)).when(promotionService).deletePromotion(PROMOTION_ID);

		assertThatThrownBy(() -> controller.deletePromotion(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);
	}
}