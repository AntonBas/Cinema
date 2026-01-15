package ua.lviv.bas.cinema.service.admin;

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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionDatesInvalidException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
public class AdminPromotionServiceTest {

	@Mock
	private PromotionRepository promotionRepository;

	@Mock
	private PromotionMapper promotionMapper;

	@InjectMocks
	private AdminPromotionService adminPromotionService;

	private Promotion testPromotion;
	private Promotion anotherPromotion;
	private PromotionCreateRequest createRequest;
	private PromotionUpdateRequest updateRequest;
	private PromotionResponse promotionResponse;
	private final Long PROMOTION_ID = 1L;
	private final Long ANOTHER_PROMOTION_ID = 2L;
	private final String PROMOTION_TITLE = "Summer Sale";
	private final String ANOTHER_TITLE = "Winter Discount";
	private final String DESCRIPTION = "Special summer promotion";
	private final LocalDate START_DATE = LocalDate.now().plusDays(1);
	private final LocalDate END_DATE = LocalDate.now().plusDays(30);
	private final Integer BONUS_POINTS = 1000;

	@BeforeEach
	void setUp() {
		testPromotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).description(DESCRIPTION)
				.bonusPoints(BONUS_POINTS).startDate(START_DATE).endDate(END_DATE).build();

		anotherPromotion = Promotion.builder().id(ANOTHER_PROMOTION_ID).title(ANOTHER_TITLE)
				.description("Winter promotion").bonusPoints(2000).startDate(LocalDate.now().plusDays(10))
				.endDate(LocalDate.now().plusDays(60)).build();

		createRequest = new PromotionCreateRequest();
		createRequest.setTitle(PROMOTION_TITLE);
		createRequest.setDescription(DESCRIPTION);
		createRequest.setBonusPoints(BONUS_POINTS);
		createRequest.setStartDate(START_DATE);
		createRequest.setEndDate(END_DATE);

		updateRequest = new PromotionUpdateRequest();
		updateRequest.setTitle("Updated Title");
		updateRequest.setDescription("Updated Description");
		updateRequest.setBonusPoints(500);
		updateRequest.setStartDate(START_DATE.plusDays(1));
		updateRequest.setEndDate(END_DATE.minusDays(1));

		promotionResponse = new PromotionResponse();
		promotionResponse.setId(PROMOTION_ID);
		promotionResponse.setTitle(PROMOTION_TITLE);
		promotionResponse.setDescription(DESCRIPTION);
		promotionResponse.setBonusPoints(BONUS_POINTS);
		promotionResponse.setStartDate(START_DATE);
		promotionResponse.setEndDate(END_DATE);
	}

	@Test
	void createPromotion_Success() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);
		when(promotionMapper.toPromotion(createRequest)).thenReturn(testPromotion);
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toPromotionResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.createPromotion(createRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(PROMOTION_ID);
		assertThat(result.getTitle()).isEqualTo(PROMOTION_TITLE);
		assertThat(result.getBonusPoints()).isEqualTo(BONUS_POINTS);
		verify(promotionRepository).existsByTitle(PROMOTION_TITLE);
		verify(promotionMapper).toPromotion(createRequest);
		verify(promotionRepository).save(testPromotion);
		verify(promotionMapper).toPromotionResponse(testPromotion);
	}

	@Test
	void createPromotion_WhenTitleAlreadyExists_ShouldThrowException() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(true);

		assertThatThrownBy(() -> adminPromotionService.createPromotion(createRequest))
				.isInstanceOf(PromotionAlreadyExistsException.class).hasMessageContaining(PROMOTION_TITLE);

		verify(promotionRepository).existsByTitle(PROMOTION_TITLE);
		verify(promotionMapper, never()).toPromotion(any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void createPromotion_WhenEndDateBeforeStartDate_ShouldThrowException() {
		createRequest.setStartDate(LocalDate.now().plusDays(10));
		createRequest.setEndDate(LocalDate.now().plusDays(5));

		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);

		assertThatThrownBy(() -> adminPromotionService.createPromotion(createRequest))
				.isInstanceOf(PromotionDatesInvalidException.class);

		verify(promotionRepository).existsByTitle(PROMOTION_TITLE);
		verify(promotionMapper, never()).toPromotion(any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void updatePromotion_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toPromotionResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.updatePromotion(PROMOTION_ID, updateRequest);

		assertThat(result).isNotNull();
		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper).updatePromotionFromRequest(testPromotion, updateRequest);
		verify(promotionRepository).save(testPromotion);
		verify(promotionMapper).toPromotionResponse(testPromotion);
	}

	@Test
	void updatePromotion_WhenPromotionNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.updatePromotion(PROMOTION_ID, updateRequest))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper, never()).updatePromotionFromRequest(any(), any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void deletePromotion_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));

		adminPromotionService.deletePromotion(PROMOTION_ID);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionRepository).delete(testPromotion);
	}

	@Test
	void deletePromotion_WhenPromotionNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.deletePromotion(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void deletePromotion_WhenHasRedemptions_ShouldThrowException() {
		UserPromotion userPromotion = UserPromotion.builder().id(1L).promotion(testPromotion).build();
		testPromotion.setUserRedemptions(Arrays.asList(userPromotion));

		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));

		assertThatThrownBy(() -> adminPromotionService.deletePromotion(PROMOTION_ID))
				.isInstanceOf(PromotionHasRedemptionsException.class).hasMessageContaining("1");

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void getPromotionById_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));
		when(promotionMapper.toPromotionResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.getPromotionById(PROMOTION_ID);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(PROMOTION_ID);
		assertThat(result.getBonusPoints()).isEqualTo(BONUS_POINTS);
		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper).toPromotionResponse(testPromotion);
	}

	@Test
	void getPromotionById_WhenPromotionNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.getPromotionById(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper, never()).toPromotionResponse(any());
	}

	@Test
	void getAllPromotions_Success() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toPromotionResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
		verify(promotionMapper).toPromotionResponseList(promotions);
	}

	@Test
	void getAllPromotions_WhenNoPromotions_ShouldReturnEmptyList() {
		when(promotionRepository.findAll()).thenReturn(Collections.emptyList());
		when(promotionMapper.toPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		verify(promotionRepository).findAll();
		verify(promotionMapper).toPromotionResponseList(Collections.emptyList());
	}

	@Test
	void getActivePromotions_Success() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toPromotionResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getActivePromotions();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
		verify(promotionMapper).toPromotionResponseList(promotions);
	}

	@Test
	void findByIdOrThrow_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));

		Promotion result = adminPromotionService.findByIdOrThrow(PROMOTION_ID);

		assertThat(result).isEqualTo(testPromotion);
		verify(promotionRepository).findById(PROMOTION_ID);
	}

	@Test
	void findByIdOrThrow_WhenNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.findByIdOrThrow(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
	}

	@Test
	void isPromotionActive_WhenPromotionIsNull_ShouldReturnFalse() {
		boolean result = adminPromotionService.isPromotionActive(null);
		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_WhenCurrentTimeIsBeforeStart_ShouldReturnFalse() {
		Promotion futurePromotion = Promotion.builder().startDate(LocalDate.now().plusDays(1))
				.endDate(LocalDate.now().plusDays(2)).build();

		boolean result = adminPromotionService.isPromotionActive(futurePromotion);
		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_WhenCurrentTimeIsAfterEnd_ShouldReturnFalse() {
		Promotion pastPromotion = Promotion.builder().startDate(LocalDate.now().minusDays(2))
				.endDate(LocalDate.now().minusDays(1)).build();

		boolean result = adminPromotionService.isPromotionActive(pastPromotion);
		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_WhenCurrentTimeIsBetweenStartAndEnd_ShouldReturnTrue() {
		Promotion activePromotion = Promotion.builder().startDate(LocalDate.now().minusDays(1))
				.endDate(LocalDate.now().plusDays(1)).build();

		boolean result = adminPromotionService.isPromotionActive(activePromotion);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenStartIsNullAndCurrentTimeIsBeforeEnd_ShouldReturnTrue() {
		Promotion promotionWithoutStart = Promotion.builder().startDate(null).endDate(LocalDate.now().plusDays(1))
				.build();

		boolean result = adminPromotionService.isPromotionActive(promotionWithoutStart);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenEndIsNullAndCurrentTimeIsAfterStart_ShouldReturnTrue() {
		Promotion promotionWithoutEnd = Promotion.builder().startDate(LocalDate.now().minusDays(1)).endDate(null)
				.build();

		boolean result = adminPromotionService.isPromotionActive(promotionWithoutEnd);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenBothDatesAreNull_ShouldReturnTrue() {
		Promotion promotionWithoutDates = Promotion.builder().startDate(null).endDate(null).build();

		boolean result = adminPromotionService.isPromotionActive(promotionWithoutDates);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenExactlyAtStartDate_ShouldReturnTrue() {
		LocalDate startDate = LocalDate.now();
		Promotion promotion = Promotion.builder().startDate(startDate).endDate(startDate.plusDays(1)).build();

		boolean result = adminPromotionService.isPromotionActive(promotion);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenExactlyAtEndDate_ShouldReturnTrue() {
		LocalDate endDate = LocalDate.now();
		Promotion promotion = Promotion.builder().startDate(endDate.minusDays(1)).endDate(endDate).build();

		boolean result = adminPromotionService.isPromotionActive(promotion);
		assertThat(result).isTrue();
	}

	@Test
	void createPromotion_ShouldBeTransactional() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);
		when(promotionMapper.toPromotion(createRequest)).thenReturn(testPromotion);
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toPromotionResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.createPromotion(createRequest);

		assertThat(result).isNotNull();
		verify(promotionRepository).save(testPromotion);
	}

	@Test
	void updatePromotion_ShouldBeTransactional() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toPromotionResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.updatePromotion(PROMOTION_ID, updateRequest);

		assertThat(result).isNotNull();
		verify(promotionRepository).save(testPromotion);
	}

	@Test
	void deletePromotion_ShouldBeTransactional() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));

		adminPromotionService.deletePromotion(PROMOTION_ID);

		verify(promotionRepository).delete(testPromotion);
	}

	@Test
	void getAllPromotions_ShouldReturnEmptyListWhenRepositoryEmpty() {
		when(promotionRepository.findAll()).thenReturn(Collections.emptyList());
		when(promotionMapper.toPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isEmpty();
		verify(promotionRepository).findAll();
	}

	@Test
	void getActivePromotions_ShouldReturnAllPromotions() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toPromotionResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getActivePromotions();

		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
	}
}