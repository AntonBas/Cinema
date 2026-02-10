package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.UserPromotionResponseProjection;
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

	@Test
	void claimPromotion_ShouldSuccessfullyClaimPromotion() {
		Long promotionId = 1L;
		Integer bonusPoints = 100;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").bonusPoints(bonusPoints)
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

		UserPromotionResponse expectedResponse = new UserPromotionResponse();
		expectedResponse.setNewBalance(250);

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);
		when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(new UserPromotion());
		when(bonusUserService.addPoints(user, bonusPoints)).thenReturn(250);
		when(promotionMapper.toUserPromotionResponse(any(UserPromotion.class))).thenReturn(expectedResponse);

		UserPromotionResponse result = promotionServiceInstance.claimPromotion(request, user);

		assertThat(result.getNewBalance()).isEqualTo(250);
		verify(userPromotionRepository).save(any(UserPromotion.class));
		verify(bonusUserService).addPoints(user, bonusPoints);
	}

	@Test
	void claimPromotion_ShouldThrowWhenPromotionNotActive() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Expired Promotion").build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(PromotionNotActiveException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void claimPromotion_ShouldThrowWhenUserAlreadyClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(AlreadyClaimedException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void getAvailablePromotions_ShouldReturnOnlyAvailablePromotions() {
		User user = User.builder().id(1L).email("test@example.com").build();

		PromotionResponse promo1 = new PromotionResponse();
		promo1.setId(1L);
		promo1.setTitle("Active Promotion 1");

		PromotionResponse promo2 = new PromotionResponse();
		promo2.setId(2L);
		promo2.setTitle("Active Promotion 2");

		List<PromotionResponse> activePromotions = Arrays.asList(promo1, promo2);

		when(promotionService.getActivePromotions()).thenReturn(activePromotions);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 1L)).thenReturn(false);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 2L)).thenReturn(true);

		List<PromotionResponse> result = promotionServiceInstance.getAvailablePromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(1L);
		verify(promotionService).getActivePromotions();
	}

	@Test
	void getUserPromotions_ShouldReturnListWithBalances() {
		User user = User.builder().id(1L).build();
		BonusCard bonusCard = BonusCard.builder().id(1L).user(user).pointsBalance(350).build();
		user.setBonusCard(bonusCard);

		List<UserPromotionResponseProjection> projections = Collections.emptyList();
		List<UserPromotionResponse> responses = Collections.singletonList(new UserPromotionResponse());

		when(userPromotionRepository.findUserPromotionResponsesByUser(user)).thenReturn(projections);
		when(promotionMapper.toUserPromotionResponseListFromProjections(projections)).thenReturn(responses);

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getNewBalance()).isEqualTo(350);
		verify(userPromotionRepository).findUserPromotionResponsesByUser(user);
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnTrueWhenClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, promotionId)).thenReturn(true);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, promotionId);

		assertThat(result).isTrue();
		verify(userPromotionRepository).existsByUserAndPromotionId(user, promotionId);
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnFalseWhenNotClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, promotionId)).thenReturn(false);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, promotionId);

		assertThat(result).isFalse();
		verify(userPromotionRepository).existsByUserAndPromotionId(user, promotionId);
	}
}