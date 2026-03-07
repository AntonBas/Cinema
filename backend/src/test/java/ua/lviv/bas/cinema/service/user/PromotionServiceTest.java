package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.UserPromotionResponseProjection;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.UserPromotionRepository;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

	@Mock
	private UserPromotionRepository userPromotionRepository;
	@Mock
	private AdminPromotionService promotionService;
	@Mock
	private BonusService bonusUserService;
	@Mock
	private PromotionMapper promotionMapper;
	@InjectMocks
	private PromotionService promotionServiceInstance;

	private final Long PROMOTION_ID = 1L;
	private final Integer BONUS_POINTS = 100;
	private final Integer NEW_BALANCE = 250;
	private final String PROMOTION_TITLE = "Test Promotion";
	private final String USER_EMAIL = "test@example.com";

	@Test
	void claimPromotion_Success() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).bonusPoints(BONUS_POINTS)
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

		UserPromotionResponse expectedResponse = new UserPromotionResponse();
		expectedResponse.setNewBalance(NEW_BALANCE);

		when(promotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);
		when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(new UserPromotion());
		when(bonusUserService.addPoints(user, BONUS_POINTS, promotion.getTitle())).thenReturn(NEW_BALANCE);
		when(promotionMapper.toUserPromotionResponse(any(UserPromotion.class))).thenReturn(expectedResponse);

		UserPromotionResponse result = promotionServiceInstance.claimPromotion(request, user);

		assertThat(result.getNewBalance()).isEqualTo(NEW_BALANCE);
		verify(userPromotionRepository).save(any(UserPromotion.class));
		verify(bonusUserService).addPoints(user, BONUS_POINTS, promotion.getTitle());
	}

	@Test
	void claimPromotion_WhenNotActive_ThrowsException() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).build();

		when(promotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(PromotionNotActiveException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), anyInt(), anyString());
	}

	@Test
	void claimPromotion_WhenAlreadyClaimed_ThrowsException() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).build();

		when(promotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(AlreadyClaimedException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), anyInt(), anyString());
	}

	@Test
	void getAvailablePromotions_ReturnsOnlyUnclaimed() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();

		PromotionResponse promo1 = new PromotionResponse();
		promo1.setId(1L);
		PromotionResponse promo2 = new PromotionResponse();
		promo2.setId(2L);
		List<PromotionResponse> activePromotionsList = Arrays.asList(promo1, promo2);

		PageResponse<PromotionResponse> pageResponse = new PageResponse<>();
		pageResponse.setContent(activePromotionsList);

		when(promotionService.getActivePromotions(any(Pageable.class))).thenReturn(pageResponse);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 1L)).thenReturn(false);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 2L)).thenReturn(true);

		List<PromotionResponse> result = promotionServiceInstance.getAvailablePromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(1L);
	}

	@Test
	void getUserPromotions_ReturnsListWithCurrentBalance() {
		User user = User.builder().id(1L).build();
		BonusCard bonusCard = BonusCard.builder().pointsBalance(NEW_BALANCE).build();
		user.setBonusCard(bonusCard);

		List<UserPromotionResponseProjection> projections = Collections.emptyList();
		List<UserPromotionResponse> responses = Collections.singletonList(new UserPromotionResponse());

		when(userPromotionRepository.findUserPromotionResponsesByUser(user)).thenReturn(projections);
		when(promotionMapper.toUserPromotionResponseListFromProjections(projections)).thenReturn(responses);

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getNewBalance()).isEqualTo(NEW_BALANCE);
	}

	@Test
	void getUserPromotions_WhenNoBonusCard_ReturnsZeroBalance() {
		User user = User.builder().id(1L).bonusCard(null).build();

		List<UserPromotionResponseProjection> projections = Collections.emptyList();
		List<UserPromotionResponse> responses = Collections.singletonList(new UserPromotionResponse());

		when(userPromotionRepository.findUserPromotionResponsesByUser(user)).thenReturn(projections);
		when(promotionMapper.toUserPromotionResponseListFromProjections(projections)).thenReturn(responses);

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getNewBalance()).isZero();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsTrueWhenClaimed() {
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isTrue();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsFalseWhenNotClaimed() {
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isFalse();
	}
}