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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.PromotionRepository;
import ua.lviv.bas.cinema.repository.UserPromotionRepository;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

	@Mock
	private PromotionRepository promotionRepository;
	@Mock
	private UserPromotionRepository userPromotionRepository;
	@Mock
	private AdminPromotionService adminPromotionService;
	@Mock
	private BonusService bonusUserService;
	@Mock
	private PromotionMapper promotionMapper;
	@InjectMocks
	private PromotionService promotionService;

	private final Long PROMOTION_ID = 1L;
	private final Integer BONUS_POINTS = 100;
	private final String PROMOTION_TITLE = "Test Promotion";
	private final String USER_EMAIL = "test@example.com";

	@Test
	void claimPromotion_Success() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).bonusPoints(BONUS_POINTS)
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

		PromotionResponse expectedResponse = new PromotionResponse(PROMOTION_ID, PROMOTION_TITLE, null, BONUS_POINTS,
				null, null);

		when(adminPromotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(expectedResponse);

		PromotionResponse result = promotionService.claimPromotion(request, user);

		assertThat(result.id()).isEqualTo(PROMOTION_ID);
		verify(userPromotionRepository).save(any(UserPromotion.class));
		verify(bonusUserService).addPoints(user, BONUS_POINTS, promotion.getTitle());
	}

	@Test
	void claimPromotion_WhenNotActive_ThrowsException() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE)
				.startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(2)).build();

		when(adminPromotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);

		assertThatThrownBy(() -> promotionService.claimPromotion(request, user))
				.isInstanceOf(PromotionNotActiveException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), anyInt(), anyString());
	}

	@Test
	void claimPromotion_WhenAlreadyClaimed_ThrowsException() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();
		UserPromotionCreateRequest request = new UserPromotionCreateRequest(PROMOTION_ID);

		Promotion promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE)
				.startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();

		when(adminPromotionService.findByIdOrThrow(PROMOTION_ID)).thenReturn(promotion);
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> promotionService.claimPromotion(request, user))
				.isInstanceOf(AlreadyClaimedException.class);

		verify(userPromotionRepository, never()).save(any());
		verify(bonusUserService, never()).addPoints(any(), anyInt(), anyString());
	}

	@Test
	void getAvailablePromotions_ReturnsOnlyUnclaimed() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();

		PromotionResponseProjection projection1 = createPromotionProjection(1L, "Promo 1");
		PromotionResponseProjection projection2 = createPromotionProjection(2L, "Promo 2");
		List<PromotionResponseProjection> activePromotions = Arrays.asList(projection1, projection2);

		PromotionResponse response1 = new PromotionResponse(1L, "Promo 1", null, 100, null, null);

		when(promotionRepository.findAllActivePromotions()).thenReturn(activePromotions);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 1L)).thenReturn(false);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 2L)).thenReturn(true);
		when(promotionMapper.toPromotionResponse(projection1)).thenReturn(response1);

		List<PromotionResponse> result = promotionService.getAvailablePromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(1L);
	}

	@Test
	void getAvailablePromotions_WhenAllClaimed_ReturnsEmpty() {
		User user = User.builder().id(1L).email(USER_EMAIL).build();

		PromotionResponseProjection projection1 = createPromotionProjection(1L, "Promo 1");
		List<PromotionResponseProjection> activePromotions = Arrays.asList(projection1);

		when(promotionRepository.findAllActivePromotions()).thenReturn(activePromotions);
		when(userPromotionRepository.existsByUserAndPromotionId(user, 1L)).thenReturn(true);

		List<PromotionResponse> result = promotionService.getAvailablePromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsTrueWhenClaimed() {
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

		boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isTrue();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsFalseWhenNotClaimed() {
		User user = User.builder().id(1L).build();

		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);

		boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isFalse();
	}

	private PromotionResponseProjection createPromotionProjection(Long id, String title) {
		return new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return id;
			}

			@Override
			public String getTitle() {
				return title;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public Integer getBonusPoints() {
				return 100;
			}

			@Override
			public LocalDate getStartDate() {
				return null;
			}

			@Override
			public LocalDate getEndDate() {
				return null;
			}
		};
	}
}