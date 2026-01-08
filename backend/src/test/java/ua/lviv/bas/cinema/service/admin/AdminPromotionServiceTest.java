package ua.lviv.bas.cinema.service.admin;

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
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class AdminPromotionServiceTest {

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
	private final LocalDateTime START_DATE = LocalDateTime.now().plusDays(1);
	private final LocalDateTime END_DATE = LocalDateTime.now().plusDays(30);
	private final Integer BONUS_POINTS = 1000;

	@BeforeEach
	void setUp() {
		testPromotion = Promotion.builder().id(PROMOTION_ID).title(PROMOTION_TITLE).description(DESCRIPTION)
				.bonusPoints(BONUS_POINTS).startDate(START_DATE).endDate(END_DATE).build();

		anotherPromotion = Promotion.builder().id(ANOTHER_PROMOTION_ID).title(ANOTHER_TITLE)
				.description("Winter promotion").bonusPoints(2000).startDate(LocalDateTime.now().plusDays(10))
				.endDate(LocalDateTime.now().plusDays(60)).build();

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
		when(promotionMapper.toEntity(createRequest)).thenReturn(testPromotion);
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.createPromotion(createRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(PROMOTION_ID);
		assertThat(result.getTitle()).isEqualTo(PROMOTION_TITLE);
		assertThat(result.getBonusPoints()).isEqualTo(BONUS_POINTS);
		verify(promotionRepository).existsByTitle(PROMOTION_TITLE);
		verify(promotionMapper).toEntity(createRequest);
		verify(promotionRepository).save(testPromotion);
		verify(promotionMapper).toResponse(testPromotion);
	}

	@Test
	void createPromotion_WhenTitleAlreadyExists_ShouldThrowException() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(true);

		assertThatThrownBy(() -> adminPromotionService.createPromotion(createRequest))
				.isInstanceOf(PromotionAlreadyExistsException.class).hasMessageContaining(PROMOTION_TITLE);

		verify(promotionRepository).existsByTitle(PROMOTION_TITLE);
		verify(promotionMapper, never()).toEntity(any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void updatePromotion_Success() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.updatePromotion(PROMOTION_ID, updateRequest);

		assertThat(result).isNotNull();
		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper).updateEntity(testPromotion, updateRequest);
		verify(promotionRepository).save(testPromotion);
		verify(promotionMapper).toResponse(testPromotion);
	}

	@Test
	void updatePromotion_WhenPromotionNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.updatePromotion(PROMOTION_ID, updateRequest))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper, never()).updateEntity(any(), any());
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
		UserPromotion userPromotion = new UserPromotion();
		userPromotion.setId(1L);
		userPromotion.setPromotion(testPromotion);

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
		when(promotionMapper.toResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.getPromotionById(PROMOTION_ID);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(PROMOTION_ID);
		assertThat(result.getBonusPoints()).isEqualTo(BONUS_POINTS);
		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper).toResponse(testPromotion);
	}

	@Test
	void getPromotionById_WhenPromotionNotFound_ShouldThrowException() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminPromotionService.getPromotionById(PROMOTION_ID))
				.isInstanceOf(PromotionNotFoundException.class);

		verify(promotionRepository).findById(PROMOTION_ID);
		verify(promotionMapper, never()).toResponse(any());
	}

	@Test
	void getAllPromotions_Success() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(promotions);
	}

	@Test
	void getAllPromotions_WhenNoPromotions_ShouldReturnEmptyList() {
		when(promotionRepository.findAll()).thenReturn(Collections.emptyList());
		when(promotionMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(Collections.emptyList());
	}

	@Test
	void getActivePromotions_Success() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getActivePromotions();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(promotions);
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
		Promotion futurePromotion = Promotion.builder().startDate(LocalDateTime.now().plusHours(1))
				.endDate(LocalDateTime.now().plusDays(1)).build();

		boolean result = adminPromotionService.isPromotionActive(futurePromotion);
		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_WhenCurrentTimeIsAfterEnd_ShouldReturnFalse() {
		Promotion pastPromotion = Promotion.builder().startDate(LocalDateTime.now().minusDays(2))
				.endDate(LocalDateTime.now().minusHours(1)).build();

		boolean result = adminPromotionService.isPromotionActive(pastPromotion);
		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_WhenCurrentTimeIsBetweenStartAndEnd_ShouldReturnTrue() {
		Promotion activePromotion = Promotion.builder().startDate(LocalDateTime.now().minusHours(1))
				.endDate(LocalDateTime.now().plusHours(1)).build();

		boolean result = adminPromotionService.isPromotionActive(activePromotion);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenStartIsNullAndCurrentTimeIsBeforeEnd_ShouldReturnTrue() {
		Promotion promotionWithoutStart = Promotion.builder().startDate(null).endDate(LocalDateTime.now().plusHours(1))
				.build();

		boolean result = adminPromotionService.isPromotionActive(promotionWithoutStart);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenEndIsNullAndCurrentTimeIsAfterStart_ShouldReturnTrue() {
		Promotion promotionWithoutEnd = Promotion.builder().startDate(LocalDateTime.now().minusHours(1)).endDate(null)
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
	void isPromotionActive_WhenExactlyAtStartTime_ShouldReturnTrue() {
		LocalDateTime startTime = LocalDateTime.now();
		Promotion promotion = Promotion.builder().startDate(startTime).endDate(startTime.plusHours(1)).build();

		boolean result = adminPromotionService.isPromotionActive(promotion);
		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_WhenExactlyAtEndTime_ShouldReturnFalse() {
		LocalDateTime endTime = LocalDateTime.now();
		Promotion promotion = Promotion.builder().startDate(endTime.minusHours(1)).endDate(endTime).build();

		boolean result = adminPromotionService.isPromotionActive(promotion);
		assertThat(result).isFalse();
	}

	@Test
	void createPromotion_ShouldBeTransactional() {
		when(promotionRepository.existsByTitle(PROMOTION_TITLE)).thenReturn(false);
		when(promotionMapper.toEntity(createRequest)).thenReturn(testPromotion);
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toResponse(testPromotion)).thenReturn(promotionResponse);

		PromotionResponse result = adminPromotionService.createPromotion(createRequest);

		assertThat(result).isNotNull();
		verify(promotionRepository).save(testPromotion);
	}

	@Test
	void updatePromotion_ShouldBeTransactional() {
		when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(testPromotion));
		when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);
		when(promotionMapper.toResponse(testPromotion)).thenReturn(promotionResponse);

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
		when(promotionMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = adminPromotionService.getAllPromotions();

		assertThat(result).isEmpty();
		verify(promotionRepository).findAll();
	}

	@Test
	void getActivePromotions_ShouldReturnAllPromotions() {
		List<Promotion> promotions = Arrays.asList(testPromotion, anotherPromotion);
		List<PromotionResponse> responses = Arrays.asList(promotionResponse, promotionResponse);

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toResponseList(promotions)).thenReturn(responses);

		List<PromotionResponse> result = adminPromotionService.getActivePromotions();

		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
	}
}