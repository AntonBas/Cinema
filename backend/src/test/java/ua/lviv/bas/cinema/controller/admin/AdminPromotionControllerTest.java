package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

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
		PromotionResponse response = new PromotionResponse();
		response.setId(PROMOTION_ID);
		response.setTitle(TITLE);
		response.setBonusPoints(BONUS_POINTS);
		response.setStartDate(LocalDate.now().plusDays(1));
		response.setEndDate(LocalDate.now().plusDays(10));
		return response;
	}

	private PromotionAdminResponse createAdminResponse() {
		PromotionAdminResponse response = new PromotionAdminResponse();
		response.setId(PROMOTION_ID);
		response.setTitle(TITLE);
		response.setBonusPoints(BONUS_POINTS);
		response.setStartDate(LocalDate.now().plusDays(1));
		response.setEndDate(LocalDate.now().plusDays(10));
		return response;
	}

	@Test
	void createPromotion_ReturnsCreated() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle(TITLE);
		PromotionResponse response = createPromotionResponse();

		when(promotionService.createPromotion(any(PromotionCreateRequest.class))).thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.createPromotion(request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getId()).isEqualTo(PROMOTION_ID);
		verify(promotionService).createPromotion(request);
	}

	@Test
	void getPromotion_ReturnsOk() {
		PromotionResponse response = createPromotionResponse();

		when(promotionService.getPromotionById(PROMOTION_ID)).thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.getPromotion(PROMOTION_ID);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getId()).isEqualTo(PROMOTION_ID);
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
		PageResponse<PromotionAdminResponse> pageResponse = new PageResponse<>();
		pageResponse.setContent(java.util.Arrays.asList(adminResponse));

		when(promotionService.getAllPromotions(pageable)).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PromotionAdminResponse>> result = controller.getAllPromotions(pageable);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getContent()).hasSize(1);
		assertThat(result.getBody().getContent().get(0).getId()).isEqualTo(PROMOTION_ID);
		verify(promotionService).getAllPromotions(pageable);
	}

	@Test
	void updatePromotion_ReturnsOk() {
		PromotionUpdateRequest request = new PromotionUpdateRequest();
		PromotionResponse response = createPromotionResponse();

		when(promotionService.updatePromotion(eq(PROMOTION_ID), any(PromotionUpdateRequest.class)))
				.thenReturn(response);

		ResponseEntity<PromotionResponse> result = controller.updatePromotion(PROMOTION_ID, request);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getId()).isEqualTo(PROMOTION_ID);
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