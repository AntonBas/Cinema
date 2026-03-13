package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.service.user.PromotionService;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerTest {

	@Mock
	private PromotionService promotionService;
	@InjectMocks
	private PromotionController promotionController;

	private final Long USER_ID = 1L;
	private final String EMAIL = "test@example.com";
	private final Long PROMOTION_ID = 1L;
	private final String TITLE = "Test Promotion";
	private final Integer BONUS_POINTS = 100;

	private User createUser() {
		User user = new User();
		user.setId(USER_ID);
		user.setEmail(EMAIL);
		return user;
	}

	private PromotionResponse createPromotionResponse() {
		PromotionResponse response = new PromotionResponse();
		response.setId(PROMOTION_ID);
		response.setTitle(TITLE);
		response.setBonusPoints(BONUS_POINTS);
		response.setStartDate(LocalDate.now().minusDays(1));
		response.setEndDate(LocalDate.now().plusDays(5));
		return response;
	}

	@Test
	void getAvailablePromotions_ReturnsList() {
		User user = createUser();
		List<PromotionResponse> promotions = List.of(createPromotionResponse(), createPromotionResponse());

		when(promotionService.getAvailablePromotions(user)).thenReturn(promotions);

		ResponseEntity<List<PromotionResponse>> result = promotionController.getAvailablePromotions(user);

		assertThat(result.getStatusCode().value()).isEqualTo(200);
		assertThat(result.getBody()).hasSize(2);
		verify(promotionService).getAvailablePromotions(user);
	}

	@Test
	void claimPromotion_Success() {
		User user = createUser();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);
		PromotionResponse response = createPromotionResponse();

		when(promotionService.claimPromotion(eq(request), eq(user))).thenReturn(response);

		ResponseEntity<PromotionResponse> result = promotionController.claimPromotion(request, user);

		assertThat(result.getStatusCode().value()).isEqualTo(200);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getId()).isEqualTo(PROMOTION_ID);
		verify(promotionService).claimPromotion(request, user);
	}

	@Test
	void claimPromotion_WhenNotActive_Throws() {
		User user = createUser();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);

		when(promotionService.claimPromotion(any(), any())).thenThrow(new PromotionNotActiveException(TITLE));

		assertThatThrownBy(() -> promotionController.claimPromotion(request, user))
				.isInstanceOf(PromotionNotActiveException.class);
	}

	@Test
	void claimPromotion_WhenAlreadyClaimed_Throws() {
		User user = createUser();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);

		when(promotionService.claimPromotion(any(), any())).thenThrow(new AlreadyClaimedException(EMAIL, TITLE));

		assertThatThrownBy(() -> promotionController.claimPromotion(request, user))
				.isInstanceOf(AlreadyClaimedException.class);
	}
}