package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
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

	@Captor
	private ArgumentCaptor<UserPromotion> userPromotionCaptor;

	@Test
	void claimPromotion_ShouldSuccessfullyClaimPromotion() {
		Long promotionId = 1L;
		Integer bonusPoints = 100;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").bonusPoints(bonusPoints)
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

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

		UserPromotionResponse result = promotionServiceInstance.claimPromotion(request, user);

		assertThat(result).isSameAs(expectedResponse);
		assertThat(result.getNewBalance()).isEqualTo(250);

		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
		verify(userPromotionRepository).save(userPromotionCaptor.capture());
		assertThat(userPromotionCaptor.getValue().getPointsAwarded()).isEqualTo(bonusPoints);
		verify(bonusUserService).addPoints(user, bonusPoints);
		verify(promotionMapper).toUserPromotionResponse(userPromotion);
	}

	@Test
	void claimPromotion_ShouldThrowWhenPromotionNotActive() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Expired Promotion")
				.startDate(LocalDate.now().minusDays(10)).endDate(LocalDate.now().minusDays(5)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(PromotionNotActiveException.class)
				.hasMessageContaining("Promotion 'Expired Promotion' is not active or has expired");

		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void claimPromotion_ShouldThrowWhenUserAlreadyClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).email("test@example.com").build();

		UserPromotionCreateRequest request = new UserPromotionCreateRequest();
		request.setPromotionId(promotionId);

		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion")
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> promotionServiceInstance.claimPromotion(request, user))
				.isInstanceOf(AlreadyClaimedException.class)
				.hasMessageContaining("User 'test@example.com' has already claimed promotion 'Test Promotion'");

		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), any());
	}

	@Test
	void getUserPromotions_ShouldReturnListWithBalances() {
		User user = User.builder().id(1L).build();

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

		List<UserPromotionResponse> responses = Arrays.asList(response1, response2);

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(userPromotions);
		when(promotionMapper.toUserPromotionResponseList(userPromotions)).thenReturn(responses);

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getNewBalance()).isEqualTo(350);
		assertThat(result.get(1).getNewBalance()).isEqualTo(350);
		assertThat(result.get(0).getPromotionTitle()).isEqualTo("Promo 1");
		assertThat(result.get(1).getPromotionTitle()).isEqualTo("Promo 2");
	}

	@Test
	void getUserPromotions_ShouldHandleNullBonusCard() {
		User user = User.builder().id(1L).bonusCard(null).build();

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(Collections.emptyList());
		when(promotionMapper.toUserPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void getUserPromotions_ShouldReturnEmptyListForNoPromotions() {
		User user = User.builder().id(1L).bonusCard(BonusCard.builder().build()).build();

		when(userPromotionRepository.findByUserWithPromotion(user)).thenReturn(Collections.emptyList());
		when(promotionMapper.toUserPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<UserPromotionResponse> result = promotionServiceInstance.getUserPromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnTrueWhenClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).build();

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, promotionId);

		assertThat(result).isTrue();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void hasUserClaimedPromotion_ShouldReturnFalseWhenNotClaimed() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).build();

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);

		boolean result = promotionServiceInstance.hasUserClaimedPromotion(user, promotionId);

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

		Long result = promotionServiceInstance.getPromotionRedemptionCount(promotionId);

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

		List<UserPromotion> result = promotionServiceInstance.getPromotionRedemptions(promotionId);

		assertThat(result).isSameAs(expectedRedemptions);
		verify(promotionService).findByIdOrThrow(promotionId);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnTrueWhenAvailable() {
		Long promotionId = 1L;
		User user = User.builder().id(1L).build();

		Promotion promotion = Promotion.builder().id(promotionId).startDate(LocalDate.now().minusDays(1))
				.endDate(LocalDate.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);

		boolean result = promotionServiceInstance.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isTrue();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenNotActive() {
		Long promotionId = 1L;
		User user = User.builder().build();

		Promotion promotion = Promotion.builder().id(promotionId).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(false);

		boolean result = promotionServiceInstance.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenAlreadyClaimed() {
		Long promotionId = 1L;
		User user = User.builder().build();

		Promotion promotion = Promotion.builder().id(promotionId).startDate(LocalDate.now().minusDays(1))
				.endDate(LocalDate.now().plusDays(1)).build();

		when(promotionService.findByIdOrThrow(promotionId)).thenReturn(promotion);
		when(promotionService.isPromotionActive(promotion)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		boolean result = promotionServiceInstance.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService).isPromotionActive(promotion);
		verify(userPromotionRepository).existsByUserAndPromotion(user, promotion);
	}

	@Test
	void isPromotionAvailableForUser_ShouldReturnFalseWhenPromotionNotFound() {
		Long promotionId = 999L;
		User user = User.builder().build();

		when(promotionService.findByIdOrThrow(promotionId)).thenThrow(new PromotionNotFoundException(promotionId));

		boolean result = promotionServiceInstance.isPromotionAvailableForUser(user, promotionId);

		assertThat(result).isFalse();
		verify(promotionService).findByIdOrThrow(promotionId);
		verify(promotionService, never()).isPromotionActive(any());
		verify(userPromotionRepository, never()).existsByUserAndPromotion(any(), any());
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

		PromotionResponse promo3 = new PromotionResponse();
		promo3.setId(3L);
		promo3.setTitle("Inactive Promotion");

		List<PromotionResponse> allPromotions = Arrays.asList(promo1, promo2, promo3);

		when(promotionService.getAllPromotions()).thenReturn(allPromotions);
		when(promotionService.isPromotionActive(any())).thenAnswer(invocation -> {
			Long promotionId = ((Promotion) invocation.getArgument(0)).getId();
			return !promotionId.equals(3L);
		});
		when(promotionService.findByIdOrThrow(1L)).thenReturn(Promotion.builder().id(1L).build());
		when(promotionService.findByIdOrThrow(2L)).thenReturn(Promotion.builder().id(2L).build());
		when(promotionService.findByIdOrThrow(3L)).thenReturn(Promotion.builder().id(3L).build());
		when(userPromotionRepository.existsByUserAndPromotionId(user, 1L)).thenReturn(false);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 2L)).thenReturn(true);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 3L)).thenReturn(false);

		List<PromotionResponse> result = promotionServiceInstance.getAvailablePromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(1L);
		assertThat(result.get(0).getTitle()).isEqualTo("Active Promotion 1");
	}

	@Test
	void getAvailablePromotions_ShouldReturnEmptyListWhenNoPromotions() {
		User user = User.builder().id(1L).email("test@example.com").build();

		when(promotionService.getAllPromotions()).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = promotionServiceInstance.getAvailablePromotions(user);

		assertThat(result).isEmpty();
	}
}