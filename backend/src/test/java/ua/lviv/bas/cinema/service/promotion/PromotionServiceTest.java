package ua.lviv.bas.cinema.service.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionDatesInvalidException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.UserPromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

	@Mock
	private PromotionRepository promotionRepository;
	@Mock
	private UserPromotionRepository userPromotionRepository;
	@Mock
	private PromotionMapper promotionMapper;
	@Mock
	private BonusService bonusService;
	@Mock
	private AuditService auditService;

	@InjectMocks
	private PromotionService promotionService;

	private final Long PROMOTION_ID = 1L;
	private final String PROMOTION_TITLE = "Summer Sale";
	private final Integer BONUS_POINTS = 100;
	private final LocalDate START_DATE = LocalDate.now().minusDays(1);
	private final LocalDate END_DATE = LocalDate.now().plusDays(30);

	private User user;
	private Promotion promotion;
	private PromotionResponse promotionResponse;
	private PromotionCreateRequest createRequest;
	private PromotionUpdateRequest updateRequest;
	private UserPromotionCreateRequest claimRequest;

	@BeforeEach
	void setUp() {
		user = User.builder().id(1L).email("test@example.com").build();

		promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).description("Summer special promotion")
				.bonusPoints(BONUS_POINTS).startDate(START_DATE).endDate(END_DATE).userRedemptions(new ArrayList<>())
				.build();

		promotionResponse = new PromotionResponse(PROMOTION_ID, PROMOTION_TITLE, "Summer special promotion",
				BONUS_POINTS, START_DATE, END_DATE);

		createRequest = new PromotionCreateRequest(PROMOTION_TITLE, "Summer special promotion", BONUS_POINTS,
				START_DATE, END_DATE);

		updateRequest = new PromotionUpdateRequest("Updated Title", "Updated description", 200, START_DATE, END_DATE);

		claimRequest = new UserPromotionCreateRequest(PROMOTION_ID);
	}

	@Test
	void createPromotion_Success() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);
		when(promotionMapper.toPromotion(createRequest)).thenReturn(promotion);
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

		PromotionResponse result = promotionService.createPromotion(createRequest);

		assertThat(result).isEqualTo(promotionResponse);
		verify(promotionRepository).save(promotion);
		verify(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
	}

	@Test
	void createPromotion_DuplicateTitle_ThrowsException() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(true);

		assertThatThrownBy(() -> promotionService.createPromotion(createRequest))
				.isInstanceOf(PromotionAlreadyExistsException.class);

		verify(promotionRepository, never()).save(any());
	}

	@Test
	void createPromotion_InvalidDates_ThrowsException() {
		PromotionCreateRequest invalidRequest = new PromotionCreateRequest(PROMOTION_TITLE, "Description", BONUS_POINTS,
				END_DATE, START_DATE);

		assertThatThrownBy(() -> promotionService.createPromotion(invalidRequest))
				.isInstanceOf(PromotionDatesInvalidException.class);
	}

	@Test
	void getPromotionById_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

		PromotionResponse result = promotionService.getPromotionById(PROMOTION_ID);

		assertThat(result).isEqualTo(promotionResponse);
	}

	@Test
	void getPromotionById_NotFound_ThrowsException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.getPromotionById(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void updatePromotion_Success() {
		Promotion updatedPromotion = Promotion.builder().id(PROMOTION_ID).title("Updated Title")
				.description("Updated description").bonusPoints(200).startDate(START_DATE).endDate(END_DATE).build();

		PromotionResponse updatedResponse = new PromotionResponse(PROMOTION_ID, "Updated Title", "Updated description",
				200, START_DATE, END_DATE);

		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
		when(promotionRepository.save(promotion)).thenReturn(updatedPromotion);
		when(promotionMapper.toPromotionResponse(updatedPromotion)).thenReturn(updatedResponse);

		PromotionResponse result = promotionService.updatePromotion(PROMOTION_ID, updateRequest);

		assertThat(result).isEqualTo(updatedResponse);
		assertThat(result.title()).isEqualTo("Updated Title");
		verify(promotionMapper).updatePromotionFromRequest(promotion, updateRequest);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void updatePromotion_NotFound_ThrowsException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.updatePromotion(PROMOTION_ID, updateRequest))
				.isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void deletePromotion_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

		promotionService.deletePromotion(PROMOTION_ID);

		verify(promotionRepository).delete(promotion);
		verify(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
	}

	@Test
	void deletePromotion_WithRedemptions_ThrowsException() {
		promotion.getUserRedemptions().add(new UserPromotion());

		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

		assertThatThrownBy(() -> promotionService.deletePromotion(PROMOTION_ID))
				.isInstanceOf(PromotionHasRedemptionsException.class);

		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void deletePromotion_NotFound_ThrowsException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.deletePromotion(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getAllPromotions_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		PromotionAdminProjection projection = createAdminProjection();
		Page<PromotionAdminProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
		PromotionAdminResponse adminResponse = new PromotionAdminResponse(PROMOTION_ID, PROMOTION_TITLE, BONUS_POINTS,
				START_DATE, END_DATE);

		when(promotionRepository.findAllAdminList(pageable)).thenReturn(page);
		when(promotionMapper.toPromotionAdminResponse(projection)).thenReturn(adminResponse);

		PageResponse<PromotionAdminResponse> result = promotionService.getAllPromotions(pageable);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(PROMOTION_ID);
	}

	@Test
	void claimPromotion_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(false);
		when(userPromotionRepository.save(any(UserPromotion.class))).thenAnswer(i -> i.getArgument(0));
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

		PromotionResponse result = promotionService.claimPromotion(claimRequest, user);

		assertThat(result).isEqualTo(promotionResponse);
		verify(bonusService).addPoints(user, BONUS_POINTS, PROMOTION_TITLE);
		verify(userPromotionRepository).save(any(UserPromotion.class));
		verify(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
	}

	@Test
	void claimPromotion_WhenNotActive_ThrowsException() {
		Promotion inactivePromotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE)
				.startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(30)).build();

		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(inactivePromotion));

		assertThatThrownBy(() -> promotionService.claimPromotion(claimRequest, user))
				.isInstanceOf(PromotionNotActiveException.class);

		verify(bonusService, never()).addPoints(any(), any(), any());
		verify(userPromotionRepository, never()).save(any());
	}

	@Test
	void claimPromotion_WhenAlreadyClaimed_ThrowsException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
		when(userPromotionRepository.existsByUserAndPromotion(user, promotion)).thenReturn(true);

		assertThatThrownBy(() -> promotionService.claimPromotion(claimRequest, user))
				.isInstanceOf(AlreadyClaimedException.class);

		verify(bonusService, never()).addPoints(any(), any(), any());
		verify(userPromotionRepository, never()).save(any());
	}

	@Test
	void getAvailablePromotions_ReturnsUnclaimedPromotions() {
		PromotionResponseProjection projection = createResponseProjection();
		List<PromotionResponseProjection> projections = List.of(projection);

		when(promotionRepository.findAllActivePromotions()).thenReturn(projections);
		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);
		when(promotionMapper.toPromotionResponse(projection)).thenReturn(promotionResponse);

		List<PromotionResponse> result = promotionService.getAvailablePromotions(user);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(promotionResponse);
	}

	@Test
	void getAvailablePromotions_WhenAlreadyClaimed_ReturnsEmpty() {
		PromotionResponseProjection projection = createResponseProjection();
		List<PromotionResponseProjection> projections = List.of(projection);

		when(promotionRepository.findAllActivePromotions()).thenReturn(projections);
		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

		List<PromotionResponse> result = promotionService.getAvailablePromotions(user);

		assertThat(result).isEmpty();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsTrue() {
		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

		boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isTrue();
	}

	@Test
	void hasUserClaimedPromotion_ReturnsFalse() {
		when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);

		boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

		assertThat(result).isFalse();
	}

	@Test
	void findByIdOrThrow_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

		Promotion result = promotionService.findByIdOrThrow(PROMOTION_ID);

		assertThat(result).isEqualTo(promotion);
	}

	@Test
	void findByIdOrThrow_NotFound_ThrowsException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.findByIdOrThrow(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);
	}

	private PromotionAdminProjection createAdminProjection() {
		return new PromotionAdminProjection() {
			@Override
			public Long getId() {
				return PROMOTION_ID;
			}

			@Override
			public String getTitle() {
				return PROMOTION_TITLE;
			}

			@Override
			public Integer getBonusPoints() {
				return BONUS_POINTS;
			}

			@Override
			public LocalDate getStartDate() {
				return START_DATE;
			}

			@Override
			public LocalDate getEndDate() {
				return END_DATE;
			}
		};
	}

	private PromotionResponseProjection createResponseProjection() {
		return new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return PROMOTION_ID;
			}

			@Override
			public String getTitle() {
				return PROMOTION_TITLE;
			}

			@Override
			public String getDescription() {
				return "Summer special promotion";
			}

			@Override
			public Integer getBonusPoints() {
				return BONUS_POINTS;
			}

			@Override
			public LocalDate getStartDate() {
				return START_DATE;
			}

			@Override
			public LocalDate getEndDate() {
				return END_DATE;
			}
		};
	}
}