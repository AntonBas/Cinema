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

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.promotion.request.ClaimPromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.service.promotion.PromotionService;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerTest {

	@Mock
	private PromotionService promotionService;

	@Mock
	private CustomUserDetails customUserDetails;

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
		return new PromotionResponse(PROMOTION_ID, TITLE, "Description", BONUS_POINTS, LocalDate.now().minusDays(1),
				LocalDate.now().plusDays(5));
	}

	@Test
	void getAvailablePromotionsWithAuthenticatedUserShouldReturnList() {
		User user = createUser();
		List<PromotionResponse> promotions = List.of(createPromotionResponse(), createPromotionResponse());

		when(customUserDetails.getUser()).thenReturn(user);
		when(promotionService.getAvailablePromotions(user)).thenReturn(promotions);

		List<PromotionResponse> result = promotionController.getAvailablePromotions(customUserDetails);

		assertThat(result).hasSize(2);
		verify(promotionService).getAvailablePromotions(user);
	}

	@Test
	void getAvailablePromotionsWithAnonymousUserShouldReturnList() {
		List<PromotionResponse> promotions = List.of(createPromotionResponse());

		when(promotionService.getAvailablePromotions(null)).thenReturn(promotions);

		List<PromotionResponse> result = promotionController.getAvailablePromotions(null);

		assertThat(result).hasSize(1);
		verify(promotionService).getAvailablePromotions(null);
	}

	@Test
	void claimPromotionShouldSucceed() {
		User user = createUser();
		ClaimPromotionRequest request = new ClaimPromotionRequest(PROMOTION_ID);
		PromotionResponse response = createPromotionResponse();

		when(customUserDetails.getUser()).thenReturn(user);
		when(promotionService.claimPromotion(eq(request), eq(user))).thenReturn(response);

		PromotionResponse result = promotionController.claimPromotion(request, customUserDetails);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(PROMOTION_ID);
		verify(promotionService).claimPromotion(request, user);
	}

	@Test
	void claimPromotionWhenNotActiveShouldThrow() {
		User user = createUser();
		ClaimPromotionRequest request = new ClaimPromotionRequest(PROMOTION_ID);

		when(customUserDetails.getUser()).thenReturn(user);
		when(promotionService.claimPromotion(any(), any())).thenThrow(new PromotionNotActiveException(TITLE));

		assertThatThrownBy(() -> promotionController.claimPromotion(request, customUserDetails))
				.isInstanceOf(PromotionNotActiveException.class);
	}

	@Test
	void claimPromotionWhenAlreadyClaimedShouldThrow() {
		User user = createUser();
		ClaimPromotionRequest request = new ClaimPromotionRequest(PROMOTION_ID);

		when(customUserDetails.getUser()).thenReturn(user);
		when(promotionService.claimPromotion(any(), any())).thenThrow(new AlreadyClaimedException(EMAIL, TITLE));

		assertThatThrownBy(() -> promotionController.claimPromotion(request, customUserDetails))
				.isInstanceOf(AlreadyClaimedException.class);
	}

	@Test
	void getClaimedPromotionsShouldReturnList() {
		User user = createUser();
		List<PromotionResponse> promotions = List.of(createPromotionResponse());

		when(customUserDetails.getUser()).thenReturn(user);
		when(promotionService.getClaimedPromotions(user)).thenReturn(promotions);

		List<PromotionResponse> result = promotionController.getClaimedPromotions(customUserDetails);

		assertThat(result).hasSize(1);
		verify(promotionService).getClaimedPromotions(user);
	}
}