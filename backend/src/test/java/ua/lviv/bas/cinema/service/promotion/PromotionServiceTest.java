package ua.lviv.bas.cinema.service.promotion;

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
import ua.lviv.bas.cinema.dto.promotion.request.ClaimPromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionListResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.*;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.UserPromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionListProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private PromotionRequest createRequest;
    private PromotionRequest updateRequest;
    private ClaimPromotionRequest claimRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").build();

        promotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).description("Summer special promotion")
                .bonusPoints(BONUS_POINTS).startDate(START_DATE).endDate(END_DATE).userRedemptions(new ArrayList<>())
                .build();

        promotionResponse = new PromotionResponse(PROMOTION_ID, PROMOTION_TITLE, "Summer special promotion",
                BONUS_POINTS, START_DATE, END_DATE);

        createRequest = new PromotionRequest(PROMOTION_TITLE, "Summer special promotion", BONUS_POINTS, START_DATE,
                END_DATE);

        updateRequest = new PromotionRequest("Updated Title", "Updated description", 200, START_DATE, END_DATE);

        claimRequest = new ClaimPromotionRequest(PROMOTION_ID);

        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void createPromotionShouldSucceed() {
        when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);
        when(promotionMapper.toPromotion(createRequest)).thenReturn(promotion);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

        PromotionResponse result = promotionService.createPromotion(createRequest);

        assertThat(result).isEqualTo(promotionResponse);
        verify(promotionRepository).save(promotion);
    }

    @Test
    void createPromotionWithDuplicateTitleShouldThrowException() {
        when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(true);

        assertThatThrownBy(() -> promotionService.createPromotion(createRequest))
                .isInstanceOf(PromotionAlreadyExistsException.class);

        verify(promotionRepository, never()).save(any());
    }

    @Test
    void getPromotionsShouldReturnPage() {
        String query = "Summer";
        Pageable pageable = PageRequest.of(0, 10);
        PromotionListProjection projection = createAdminProjection();
        Page<PromotionListProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
        PromotionListResponse listResponse = new PromotionListResponse(PROMOTION_ID, PROMOTION_TITLE, BONUS_POINTS,
                START_DATE, END_DATE);

        when(promotionRepository.findAllAdminProjections(eq(query), eq(pageable))).thenReturn(page);
        when(promotionMapper.toPromotionListResponse(projection)).thenReturn(listResponse);

        Page<PromotionListResponse> result = promotionService.getPromotions(query, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(PROMOTION_ID);
    }

    @Test
    void getPromotionsWithNullQueryShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        PromotionListProjection projection = createAdminProjection();
        Page<PromotionListProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
        PromotionListResponse listResponse = new PromotionListResponse(PROMOTION_ID, PROMOTION_TITLE, BONUS_POINTS,
                START_DATE, END_DATE);

        when(promotionRepository.findAllAdminProjections(eq(null), eq(pageable))).thenReturn(page);
        when(promotionMapper.toPromotionListResponse(projection)).thenReturn(listResponse);

        Page<PromotionListResponse> result = promotionService.getPromotions(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(PROMOTION_ID);
    }

    @Test
    void getPromotionShouldReturnPromotion() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

        PromotionResponse result = promotionService.getPromotion(PROMOTION_ID);

        assertThat(result).isEqualTo(promotionResponse);
    }

    @Test
    void getPromotionWhenNotFoundShouldThrowException() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getPromotion(PROMOTION_ID))
                .isInstanceOf(PromotionNotFoundException.class);
    }

    @Test
    void getAvailablePromotionsShouldReturnList() {
        PromotionResponseProjection projection = createResponseProjection();
        List<PromotionResponseProjection> projections = List.of(projection);

        when(promotionRepository.findAllActivePromotions()).thenReturn(projections);
        when(promotionMapper.toPromotionResponse(projection)).thenReturn(promotionResponse);

        List<PromotionResponse> result = promotionService.getAvailablePromotions(user);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(promotionResponse);
    }

    @Test
    void getClaimedPromotionsShouldReturnList() {
        PromotionResponseProjection projection = createResponseProjection();
        List<PromotionResponseProjection> projections = List.of(projection);

        when(promotionRepository.findClaimedPromotionsByUser(user)).thenReturn(projections);
        when(promotionMapper.toPromotionResponse(projection)).thenReturn(promotionResponse);

        List<PromotionResponse> result = promotionService.getClaimedPromotions(user);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(promotionResponse);
    }

    @Test
    void updatePromotionShouldSucceed() {
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
        verify(promotionMapper).updatePromotionFromRequest(updateRequest, promotion);
        verify(promotionRepository).save(promotion);
    }

    @Test
    void updatePromotionWhenNotFoundShouldThrowException() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.updatePromotion(PROMOTION_ID, updateRequest))
                .isInstanceOf(PromotionNotFoundException.class);
    }

    @Test
    void deletePromotionShouldSucceed() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

        promotionService.deletePromotion(PROMOTION_ID);

        verify(promotionRepository).delete(promotion);
    }

    @Test
    void deletePromotionWithRedemptionsShouldThrowException() {
        promotion.getUserRedemptions().add(new UserPromotion());

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

        assertThatThrownBy(() -> promotionService.deletePromotion(PROMOTION_ID))
                .isInstanceOf(PromotionHasRedemptionsException.class);

        verify(promotionRepository, never()).delete(any());
    }

    @Test
    void deletePromotionWhenNotFoundShouldThrowException() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.deletePromotion(PROMOTION_ID))
                .isInstanceOf(PromotionNotFoundException.class);
    }

    @Test
    void claimPromotionShouldSucceed() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);
        when(userPromotionRepository.save(any(UserPromotion.class))).thenAnswer(i -> i.getArgument(0));
        when(promotionMapper.toPromotionResponse(promotion)).thenReturn(promotionResponse);

        PromotionResponse result = promotionService.claimPromotion(claimRequest, user);

        assertThat(result).isEqualTo(promotionResponse);
        verify(bonusService).addPromotionPoints(user, BONUS_POINTS, PROMOTION_TITLE);
        verify(userPromotionRepository).save(any(UserPromotion.class));
    }

    @Test
    void claimPromotionWhenNotActiveShouldThrowException() {
        Promotion inactivePromotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE)
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(30)).build();

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(inactivePromotion));

        assertThatThrownBy(() -> promotionService.claimPromotion(claimRequest, user))
                .isInstanceOf(PromotionNotActiveException.class);

        verify(bonusService, never()).addPromotionPoints(any(), any(), any());
        verify(userPromotionRepository, never()).save(any());
    }

    @Test
    void claimPromotionWhenAlreadyClaimedShouldThrowException() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

        assertThatThrownBy(() -> promotionService.claimPromotion(claimRequest, user))
                .isInstanceOf(AlreadyClaimedException.class);

        verify(bonusService, never()).addPromotionPoints(any(), any(), any());
        verify(userPromotionRepository, never()).save(any());
    }

    @Test
    void hasUserClaimedPromotionShouldReturnTrue() {
        when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(true);

        boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

        assertThat(result).isTrue();
    }

    @Test
    void hasUserClaimedPromotionShouldReturnFalse() {
        when(userPromotionRepository.existsByUserAndPromotionId(user, PROMOTION_ID)).thenReturn(false);

        boolean result = promotionService.hasUserClaimedPromotion(user, PROMOTION_ID);

        assertThat(result).isFalse();
    }

    private PromotionListProjection createAdminProjection() {
        return new PromotionListProjection() {
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