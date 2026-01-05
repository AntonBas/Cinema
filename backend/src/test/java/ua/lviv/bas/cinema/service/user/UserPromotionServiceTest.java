package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.UserPromotionRepository;
import ua.lviv.bas.cinema.service.common.PromotionService;

@ExtendWith(MockitoExtension.class)
class UserPromotionServiceTest {

	@Mock
	private UserPromotionRepository userPromotionRepository;

	@Mock
	private PromotionService promotionService;

	@Mock
	private UserBonusService bonusUserService;

	@Mock
	private PromotionMapper promotionMapper;

	@InjectMocks
	private UserPromotionService userPromotionService;

	@Test
	void claimPromotion_ShouldSuccessfullyClaimPromotion() {
		Long promotionId = 1L;
		Integer bonusPoints = 100;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").bonusPoints(bonusPoints)
				.startDate(LocalDateTime.now().minusDays(1)).endDate(LocalDateTime.now().plusDays(1)).build();

		UserPromotion userPromotion = UserPromotion.builder().id(1L).user(user).promotion(promotion)
				.redeemedAt(LocalDateTime.now()).pointsAwarded(bonusPoints).build();

		UserPromotionResponse expectedResponse = new UserPromotionResponse();
		expectedResponse.setId(1L);
		expectedResponse.setPromotionId(promotionId);
		expectedResponse.setPromotionTitle("Test Promotion");
		expectedResponse.setPointsAwarded(bonusPoints);

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);
		when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(userPromotion);
		when(bonusUserService.addPoints(user, bonusPoints)).thenReturn(250);
		when(promotionMapper.toUserPromotionResponse(userPromotion)).thenReturn(expectedResponse);

		UserPromotionResponse result = userPromotionService.claimPromotion(request, user);

		assertThat(result).isSameAs(expectedResponse);
		assertThat(result.getNewBalance()).isEqualTo(250);

		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
		verify(userPromotionRepository).save(any(UserPromotion.class));
		verify(bonusUserService).addPoints(user, bonusPoints);
		verify(promotionMapper).toUserPromotionResponse(userPromotion);
	}

	@Test
	void claimPromotion_ShouldThrowWhenPromotionNotActive() {
		Long promotionId = 1L;
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Expired Promotion")
				.startDate(LocalDateTime.now().minusDays(10)).endDate(LocalDateTime.now().minusDays(5)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		assertThatThrownBy(() -> userPromotionService.claimPromotion(request, user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Promotion is not active or has expired");

		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void claimPromotion_ShouldThrowWhenUserAlreadyClaimed() {
		Long promotionId = 1L;
		User user = new User();
		user.setId(1L);

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).startDate(LocalDateTime.now().minusDays(1))
				.endDate(LocalDateTime.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> userPromotionService.claimPromotion(request, user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("User has already claimed this promotion");

		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void getUserPromotions_ShouldReturnListWithBalances() {
		User user = new User();
		user.setId(1L);

		BonusCard bonusCard = BonusCard.builder().id(1L).user(user).pointsBalance(350).build();
		user.setBonusCard(bonusCard);

		Promotion promotion1 = Promotion.builder().id(1L).title("Promo 1").build();

		Promotion promotion2 = Promotion.builder().id(2L).title("Promo 2").build();

		List<UserPromotion> userPromotions = Arrays.asList(
				UserPromotion.builder().id(1L).promotion(promotion1).redeemedAt(LocalDateTime.now().minusDays(5))
						.pointsAwarded(100).build(),
				UserPromotion.builder().id(2L).promotion(promotion2).redeemedAt(LocalDateTime.now().minusDays(2))
						.pointsAwarded(250).build());

		UserPromotionResponse response1 = new UserPromotionResponse();
		response1.setId(1L);
		response1.setPromotionId(1L);
		response1.setPromotionTitle("Promo 1");
		response1.setPointsAwarded(100);

		UserPromotionResponse response2 = new UserPromotionResponse();
		response2.setId(2L);
		response2.setPromotionId(2L);
		response2.setPromotionTitle("Promo 2");
		response2.setPointsAwarded(250);

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(userPromotions);
		when(promotionMapper.toUserPromotionResponseList(userPromotions))
				.thenReturn(Arrays.asList(response1, response2));

		List<UserPromotionResponse> result = userPromotionService.getUserPromotions(user);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getNewBalance()).isEqualTo(350);
		assertThat(result.get(1).getNewBalance()).isEqualTo(350);
		assertThat(result.get(0).getPromotionTitle()).isEqualTo("Promo 1");
		assertThat(result.get(1).getPromotionTitle()).isEqualTo("Promo 2");
	}

	@Test
	void getUserPromotions_ShouldHandleNullBonusCard() {
		User user = new User();
		user.setId(1L);
		user.setBonusCard(null);

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(Collections.emptyList());
		when(promotionMapper.toUserPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<UserPromotionResponse> result = userPromotionService.getUserPromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void getUserPromotions_ShouldReturnEmptyListForNoPromotions() {
		User user = new User();
		user.setId(1L);
		user.setBonusCard(new BonusCard());

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(Collections.emptyList());
		when(promotionMapper.toUserPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<UserPromotionResponse> result = userPromotionService.getUserPromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnTrueWhenClaimed() {
		Long promotionId = 1L;
		User user = new User();
		user.setId(1L);

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		boolean result = userPromotionService.hasUserClaimedPromotion(user, promotionId);

		assertThat(result).isTrue();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnFalseWhenNotClaimed() {
		Long promotionId = 1L;
		User user = new User();
		user.setId(1L);

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);

		boolean result = userPromotionService.hasUserClaimedPromotion(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void getPromotionRedemptionCount_ShouldReturnCount() {
		Long promotionId = 1L;
		Long expectedCount = 25L;
		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(userPromotionRepository.countByPromotion(promotion)).thenReturn(expectedCount);

		Long result = userPromotionService.getPromotionRedemptionCount(promotionId);

		assertThat(result).isEqualTo(expectedCount);
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(userPromotionRepository).countByPromotion(promotion);
	}

	@Test
	void getPromotionRedemptions_ShouldReturnRedemptions() {
		Long promotionId = 1L;
		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").build();

		List<UserPromotion> expectedRedemptions = Arrays.asList(UserPromotion.builder().id(1L).build(),
				UserPromotion.builder().id(2L).build());
		promotion.setUserRedemptions(expectedRedemptions);

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);

		List<UserPromotion> result = userPromotionService.getPromotionRedemptions(promotionId);

		assertThat(result).isSameAs(expectedRedemptions);
		verify(promotionService).findByIdOrThrow(promotionId);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnTrueWhenAvailable() {
		Long promotionId = 1L;
		User user = new User();
		user.setId(1L);

		Promotion promotion = Promotion.builder().id(promotionId).startDate(LocalDateTime.now().minusDays(1))
				.endDate(LocalDateTime.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);

		boolean result = userPromotionService.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isTrue();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenNotActive() {
		Long promotionId = 1L;
		User user = new User();

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		boolean result = userPromotionService.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenAlreadyClaimed() {
		Long promotionId = 1L;
		User user = new User();

		Promotion promotion = Promotion.builder().id(promotionId).startDate(LocalDateTime.now().minusDays(1))
				.endDate(LocalDateTime.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		boolean result = userPromotionService.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenPromotionNotFound() {
		Long promotionId = 999L;
		User user = new User();

		when(promotionService.findByIdOrThrow(promotionId))
				.thenThrow(new IllegalArgumentException("Promotion not found"));

		boolean result = userPromotionService.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService, never()).isPromotionActive(any());
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
	}
}