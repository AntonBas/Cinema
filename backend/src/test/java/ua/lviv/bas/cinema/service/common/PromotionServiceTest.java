package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
class PromotionServiceTest {

	@Mock
	private PromotionRepository promotionRepository;

	@Mock
	private PromotionMapper promotionMapper;

	@InjectMocks
	private PromotionService promotionService;

	@Test
	void createPromotion_ShouldCreateSuccessfully() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("New Promotion");
		request.setDescription("Test description");
		request.setBonusPoints(100);
		request.setStartDate(LocalDateTime.now().plusDays(1));
		request.setEndDate(LocalDateTime.now().plusDays(10));

		Promotion promotion = Promotion.builder().id(1L).title("New Promotion").description("Test description")
				.bonusPoints(100).startDate(request.getStartDate()).endDate(request.getEndDate()).build();

		PromotionResponse expectedResponse = new PromotionResponse();
		expectedResponse.setId(1L);
		expectedResponse.setTitle("New Promotion");
		expectedResponse.setBonusPoints(100);

		when(promotionRepository.existsByTitle("New Promotion")).thenReturn(false);
		when(promotionMapper.toEntity(request)).thenReturn(promotion);
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toResponse(promotion)).thenReturn(expectedResponse);

		PromotionResponse result = promotionService.createPromotion(request);

		assertThat(result).isSameAs(expectedResponse);
		verify(promotionRepository).existsByTitle("New Promotion");
		verify(promotionMapper).toEntity(request);
		verify(promotionRepository).save(promotion);
		verify(promotionMapper).toResponse(promotion);
	}

	@Test
	void createPromotion_ShouldThrowWhenTitleExists() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Existing Promotion");

		when(promotionRepository.existsByTitle("Existing Promotion")).thenReturn(true);

		assertThatThrownBy(() -> promotionService.createPromotion(request))
				.isInstanceOf(PromotionAlreadyExistsException.class)
				.hasMessageContaining("Promotion with title 'Existing Promotion' already exists");

		verify(promotionRepository).existsByTitle("Existing Promotion");
		verify(promotionMapper, never()).toEntity(any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void updatePromotion_ShouldUpdateSuccessfully() {
		Long promotionId = 1L;

		PromotionUpdateRequest request = new PromotionUpdateRequest();
		request.setTitle("Updated Title");
		request.setBonusPoints(200);

		Promotion existingPromotion = Promotion.builder().id(promotionId).title("Old Title")
				.description("Old Description").bonusPoints(100).startDate(LocalDateTime.now())
				.endDate(LocalDateTime.now().plusDays(5)).build();

		Promotion updatedPromotion = Promotion.builder().id(promotionId).title("Updated Title")
				.description("Old Description").bonusPoints(200).startDate(existingPromotion.getStartDate())
				.endDate(existingPromotion.getEndDate()).build();

		PromotionResponse expectedResponse = new PromotionResponse();
		expectedResponse.setId(promotionId);
		expectedResponse.setTitle("Updated Title");
		expectedResponse.setBonusPoints(200);

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(existingPromotion));
		when(promotionRepository.save(existingPromotion)).thenReturn(updatedPromotion);
		when(promotionMapper.toResponse(updatedPromotion)).thenReturn(expectedResponse);

		PromotionResponse result = promotionService.updatePromotion(promotionId, request);

		assertThat(result).isSameAs(expectedResponse);
		verify(promotionRepository).findById(promotionId);
		verify(promotionMapper).updateEntity(existingPromotion, request);
		verify(promotionRepository).save(existingPromotion);
		verify(promotionMapper).toResponse(updatedPromotion);
	}

	@Test
	void updatePromotion_ShouldThrowWhenPromotionNotFound() {
		Long promotionId = 999L;
		PromotionUpdateRequest request = new PromotionUpdateRequest();

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.updatePromotion(promotionId, request))
				.isInstanceOf(PromotionNotFoundException.class)
				.hasMessageContaining("Promotion not found with id: " + promotionId);

		verify(promotionRepository).findById(promotionId);
		verify(promotionMapper, never()).updateEntity(any(), any());
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void deletePromotion_ShouldDeleteSuccessfully() {
		Long promotionId = 1L;
		Promotion promotion = Promotion.builder().id(promotionId).userRedemptions(Collections.emptyList()).build();

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));

		promotionService.deletePromotion(promotionId);

		verify(promotionRepository).findById(promotionId);
		verify(promotionRepository).delete(promotion);
	}

	@Test
	void deletePromotion_ShouldThrowWhenPromotionNotFound() {
		Long promotionId = 999L;

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.deletePromotion(promotionId))
				.isInstanceOf(PromotionNotFoundException.class)
				.hasMessageContaining("Promotion not found with id: " + promotionId);

		verify(promotionRepository).findById(promotionId);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void deletePromotion_ShouldThrowWhenHasRedemptions() {
		Long promotionId = 1L;
		int redemptionCount = 2;
		Promotion promotion = Promotion.builder().id(promotionId)
				.userRedemptions(
						Arrays.asList(UserPromotion.builder().id(1L).build(), UserPromotion.builder().id(2L).build()))
				.build();

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));

		assertThatThrownBy(() -> promotionService.deletePromotion(promotionId))
				.isInstanceOf(PromotionHasRedemptionsException.class)
				.hasMessageContaining("Cannot delete promotion with ID: " + promotionId)
				.hasMessageContaining("because it has " + redemptionCount + " user redemption(s)");

		verify(promotionRepository).findById(promotionId);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void getPromotionById_ShouldReturnPromotion() {
		Long promotionId = 1L;
		Promotion promotion = Promotion.builder().id(promotionId).title("Test Promotion").bonusPoints(150).build();

		PromotionResponse expectedResponse = new PromotionResponse();
		expectedResponse.setId(promotionId);
		expectedResponse.setTitle("Test Promotion");
		expectedResponse.setBonusPoints(150);

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
		when(promotionMapper.toResponse(promotion)).thenReturn(expectedResponse);

		PromotionResponse result = promotionService.getPromotionById(promotionId);

		assertThat(result).isSameAs(expectedResponse);
		verify(promotionRepository).findById(promotionId);
		verify(promotionMapper).toResponse(promotion);
	}

	@Test
	void getPromotionById_ShouldThrowWhenNotFound() {
		Long promotionId = 999L;

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.getPromotionById(promotionId))
				.isInstanceOf(PromotionNotFoundException.class)
				.hasMessageContaining("Promotion not found with id: " + promotionId);

		verify(promotionRepository).findById(promotionId);
		verify(promotionMapper, never()).toResponse(any());
	}

	@Test
	void getAllPromotions_ShouldReturnAllPromotions() {
		List<Promotion> promotions = Arrays.asList(Promotion.builder().id(1L).title("Promo 1").build(),
				Promotion.builder().id(2L).title("Promo 2").build());

		PromotionResponse response1 = new PromotionResponse();
		response1.setId(1L);
		response1.setTitle("Promo 1");

		PromotionResponse response2 = new PromotionResponse();
		response2.setId(2L);
		response2.setTitle("Promo 2");

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toResponseList(promotions)).thenReturn(Arrays.asList(response1, response2));

		List<PromotionResponse> result = promotionService.getAllPromotions();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getTitle()).isEqualTo("Promo 1");
		assertThat(result.get(1).getTitle()).isEqualTo("Promo 2");
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(promotions);
	}

	@Test
	void getAllPromotions_ShouldReturnEmptyList() {
		when(promotionRepository.findAll()).thenReturn(Collections.emptyList());
		when(promotionMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PromotionResponse> result = promotionService.getAllPromotions();

		assertThat(result).isEmpty();
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(Collections.emptyList());
	}

	@Test
	void findByIdOrThrow_ShouldReturnPromotionWhenExists() {
		Long promotionId = 1L;
		Promotion expectedPromotion = Promotion.builder().id(promotionId).build();

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(expectedPromotion));

		Promotion result = promotionService.findByIdOrThrow(promotionId);

		assertThat(result).isSameAs(expectedPromotion);
		verify(promotionRepository).findById(promotionId);
	}

	@Test
	void findByIdOrThrow_ShouldThrowWhenNotFound() {
		Long promotionId = 999L;

		when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> promotionService.findByIdOrThrow(promotionId))
				.isInstanceOf(PromotionNotFoundException.class)
				.hasMessageContaining("Promotion not found with id: " + promotionId);

		verify(promotionRepository).findById(promotionId);
	}

	@Test
	void isPromotionActive_ShouldReturnFalseWhenPromotionIsNull() {
		boolean result = promotionService.isPromotionActive(null);

		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_ShouldReturnTrueWhenNoDates() {
		Promotion promotion = Promotion.builder().startDate(null).endDate(null).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_ShouldReturnTrueWhenCurrentWithinDates() {
		Promotion promotion = Promotion.builder().startDate(LocalDateTime.now().minusDays(1))
				.endDate(LocalDateTime.now().plusDays(1)).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_ShouldReturnTrueWhenCurrentEqualsStartDate() {
		LocalDateTime startDate = LocalDateTime.now();
		Promotion promotion = Promotion.builder().startDate(startDate).endDate(startDate.plusDays(5)).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_ShouldReturnTrueWhenCurrentEqualsEndDate() {
		LocalDateTime fixedNow = LocalDateTime.of(2024, 1, 15, 12, 0, 0);

		Promotion promotion = Promotion.builder().startDate(fixedNow.minusDays(5)).endDate(fixedNow).build();

		try (var mockedNow = mockStatic(LocalDateTime.class)) {
			mockedNow.when(LocalDateTime::now).thenReturn(fixedNow);

			boolean result = promotionService.isPromotionActive(promotion);
			assertThat(result).isTrue();
		}
	}

	@Test
	void isPromotionActive_ShouldReturnFalseWhenBeforeStartDate() {
		Promotion promotion = Promotion.builder().startDate(LocalDateTime.now().plusDays(1))
				.endDate(LocalDateTime.now().plusDays(10)).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_ShouldReturnFalseWhenAfterEndDate() {
		Promotion promotion = Promotion.builder().startDate(LocalDateTime.now().minusDays(10))
				.endDate(LocalDateTime.now().minusDays(1)).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActive_ShouldHandleStartDateOnly() {
		Promotion promotion = Promotion.builder().startDate(LocalDateTime.now().minusDays(1)).endDate(null).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActive_ShouldHandleEndDateOnly() {
		Promotion promotion = Promotion.builder().startDate(null).endDate(LocalDateTime.now().plusDays(1)).build();

		boolean result = promotionService.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void getActivePromotions_ShouldReturnAllPromotions() {
		List<Promotion> promotions = Arrays.asList(Promotion.builder().id(1L).title("Active Promo 1").build(),
				Promotion.builder().id(2L).title("Active Promo 2").build());

		PromotionResponse response1 = new PromotionResponse();
		response1.setId(1L);
		response1.setTitle("Active Promo 1");

		PromotionResponse response2 = new PromotionResponse();
		response2.setId(2L);
		response2.setTitle("Active Promo 2");

		when(promotionRepository.findAll()).thenReturn(promotions);
		when(promotionMapper.toResponseList(promotions)).thenReturn(Arrays.asList(response1, response2));

		List<PromotionResponse> result = promotionService.getActivePromotions();

		assertThat(result).hasSize(2);
		verify(promotionRepository).findAll();
		verify(promotionMapper).toResponseList(promotions);
	}
}