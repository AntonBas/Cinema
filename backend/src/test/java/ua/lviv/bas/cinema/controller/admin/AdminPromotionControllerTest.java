package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionListResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.service.promotion.PromotionService;

@ExtendWith(MockitoExtension.class)
public class AdminPromotionControllerTest {

	@Mock
	private PromotionService promotionService;

	@InjectMocks
	private AdminPromotionController controller;

	private final Long PROMOTION_ID = 1L;
	private final String TITLE = "Test Promotion";
	private final Integer BONUS_POINTS = 100;

	private PromotionResponse createPromotionResponse() {
		return new PromotionResponse(PROMOTION_ID, TITLE, "Description", BONUS_POINTS, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(10));
	}

	private PromotionListResponse createPromotionListResponse() {
		return new PromotionListResponse(PROMOTION_ID, TITLE, BONUS_POINTS, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(10));
	}

	@Test
	void createPromotionShouldReturnCreated() {
		PromotionRequest request = new PromotionRequest(TITLE, "Description", BONUS_POINTS, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(10));
		PromotionResponse response = createPromotionResponse();

		when(promotionService.createPromotion(any(PromotionRequest.class))).thenReturn(response);

		PromotionResponse result = controller.createPromotion(request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(PROMOTION_ID);
		assertThat(result.title()).isEqualTo(TITLE);
		verify(promotionService).createPromotion(request);
	}

	@Test
	void getPromotionShouldReturnPromotion() {
		PromotionResponse response = createPromotionResponse();

		when(promotionService.getPromotion(PROMOTION_ID)).thenReturn(response);

		PromotionResponse result = controller.getPromotion(PROMOTION_ID);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(PROMOTION_ID);
		assertThat(result.title()).isEqualTo(TITLE);
		verify(promotionService).getPromotion(PROMOTION_ID);
	}

	@Test
	void getPromotionShouldThrowWhenNotFound() {
		when(promotionService.getPromotion(999L)).thenThrow(new PromotionNotFoundException(999L));

		assertThatThrownBy(() -> controller.getPromotion(999L)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getPromotionsWithoutQueryShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
		PromotionListResponse listResponse = createPromotionListResponse();
		Page<PromotionListResponse> page = new PageImpl<>(List.of(listResponse), pageable, 1);

		when(promotionService.getPromotions(isNull(), eq(pageable))).thenReturn(page);

		PageResponse<PromotionListResponse> result = controller.getPromotions(null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(PROMOTION_ID);
		assertThat(result.content().get(0).title()).isEqualTo(TITLE);
		verify(promotionService).getPromotions(isNull(), eq(pageable));
	}

	@Test
	void getPromotionsWithQueryShouldReturnFilteredPage() {
		String query = "Test";
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
		PromotionListResponse listResponse = createPromotionListResponse();
		Page<PromotionListResponse> page = new PageImpl<>(List.of(listResponse), pageable, 1);

		when(promotionService.getPromotions(eq(query), eq(pageable))).thenReturn(page);

		PageResponse<PromotionListResponse> result = controller.getPromotions(query, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(PROMOTION_ID);
		assertThat(result.content().get(0).title()).isEqualTo(TITLE);
		verify(promotionService).getPromotions(eq(query), eq(pageable));
	}

	@Test
	void getPromotionsShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<PromotionListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(promotionService.getPromotions(isNull(), eq(pageable))).thenReturn(emptyPage);

		PageResponse<PromotionListResponse> result = controller.getPromotions(null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
		verify(promotionService).getPromotions(isNull(), eq(pageable));
	}

	@Test
	void updatePromotionShouldReturnUpdated() {
		PromotionRequest request = new PromotionRequest("Updated Title", "Updated Description", 200,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
		PromotionResponse response = new PromotionResponse(PROMOTION_ID, "Updated Title", "Updated Description", 200,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));

		when(promotionService.updatePromotion(eq(PROMOTION_ID), any(PromotionRequest.class))).thenReturn(response);

		PromotionResponse result = controller.updatePromotion(PROMOTION_ID, request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(PROMOTION_ID);
		assertThat(result.title()).isEqualTo("Updated Title");
		assertThat(result.bonusPoints()).isEqualTo(200);
		verify(promotionService).updatePromotion(PROMOTION_ID, request);
	}

	@Test
	void updatePromotionShouldThrowWhenNotFound() {
		PromotionRequest request = new PromotionRequest("Updated Title", "Updated Description", 200,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));

		when(promotionService.updatePromotion(eq(999L), any(PromotionRequest.class)))
				.thenThrow(new PromotionNotFoundException(999L));

		assertThatThrownBy(() -> controller.updatePromotion(999L, request))
				.isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void deletePromotionShouldCallService() {
		controller.deletePromotion(PROMOTION_ID);

		verify(promotionService).deletePromotion(PROMOTION_ID);
	}

	@Test
	void deletePromotionShouldThrowWhenNotFound() {
		doThrow(new PromotionNotFoundException(999L)).when(promotionService).deletePromotion(999L);

		assertThatThrownBy(() -> controller.deletePromotion(999L)).isInstanceOf(PromotionNotFoundException.class);
	}
}