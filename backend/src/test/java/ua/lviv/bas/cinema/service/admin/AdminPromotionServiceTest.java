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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
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
	private AdminPromotionService service;

	@Test
	void createPromotion() {
		PromotionCreateRequest request = new PromotionCreateRequest("Test Promotion", null, 100, LocalDate.now(),
				LocalDate.now().plusDays(7));

		Promotion promotion = Promotion.builder().id(1L).title("Test Promotion").build();
		PromotionResponse response = new PromotionResponse(1L, "Test Promotion", null, 100, null, null);

		when(promotionRepository.existsByTitle("Test Promotion")).thenReturn(false);
		when(promotionMapper.toPromotion(request)).thenReturn(promotion);
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.createPromotion(request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void createPromotionWithExistingTitle() {
		PromotionCreateRequest request = new PromotionCreateRequest("Existing Promotion", null, null, null, null);

		when(promotionRepository.existsByTitle("Existing Promotion")).thenReturn(true);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionAlreadyExistsException.class);
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void createPromotionWithInvalidDates() {
		PromotionCreateRequest request = new PromotionCreateRequest("Test", null, 100, LocalDate.now().plusDays(7),
				LocalDate.now());

		when(promotionRepository.existsByTitle("Test")).thenReturn(false);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionDatesInvalidException.class);
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void updatePromotion() {
		Promotion promotion = Promotion.builder().id(1L).build();
		PromotionUpdateRequest request = new PromotionUpdateRequest("Updated", null, 100, null, null);
		PromotionResponse response = new PromotionResponse(1L, "Updated", null, 100, null, null);

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.updatePromotion(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
		verify(promotionMapper).updatePromotionFromRequest(promotion, request);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void updatePromotionNotFound() {
		PromotionUpdateRequest request = new PromotionUpdateRequest(null, null, null, null, null);

		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updatePromotion(1L, request)).isInstanceOf(PromotionNotFoundException.class);
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void deletePromotion() {
		Promotion promotion = Promotion.builder().id(1L).userRedemptions(Collections.emptyList()).build();

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

		service.deletePromotion(1L);

		verify(promotionRepository).delete(promotion);
	}

	@Test
	void deletePromotionWithRedemptions() {
		List<UserPromotion> redemptions = Arrays.asList(new UserPromotion());
		Promotion promotion = Promotion.builder().id(1L).userRedemptions(redemptions).build();

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

		assertThatThrownBy(() -> service.deletePromotion(1L)).isInstanceOf(PromotionHasRedemptionsException.class);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void deletePromotionNotFound() {
		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.deletePromotion(1L)).isInstanceOf(PromotionNotFoundException.class);
		verify(promotionRepository, never()).delete(any());
	}

	@Test
	void getPromotionById() {
		Promotion promotion = Promotion.builder().id(1L).title("Test").build();
		PromotionResponse response = new PromotionResponse(1L, "Test", null, 100, null, null);

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.getPromotionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
	}

	@Test
	void getPromotionByIdNotFound() {
		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getPromotionById(1L)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getAllPromotions() {
		Pageable pageable = PageRequest.of(0, 10);
		PromotionAdminProjection projection = new PromotionAdminProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getTitle() {
				return "Test";
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

		Page<PromotionAdminProjection> page = new PageImpl<>(Arrays.asList(projection));
		PromotionAdminResponse response = new PromotionAdminResponse(1L, "Test", 100, null, null);

		when(promotionRepository.findAllAdminList(pageable)).thenReturn(page);
		when(promotionMapper.toPromotionAdminResponse(projection)).thenReturn(response);

		PageResponse<PromotionAdminResponse> result = service.getAllPromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(1L);
	}

	@Test
	void getAllPromotionsEmpty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<PromotionAdminProjection> page = new PageImpl<>(Collections.emptyList());

		when(promotionRepository.findAllAdminList(pageable)).thenReturn(page);

		PageResponse<PromotionAdminResponse> result = service.getAllPromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEmpty();
	}

	@Test
	void findByIdOrThrow() {
		Promotion promotion = Promotion.builder().id(1L).build();

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

		Promotion result = service.findByIdOrThrow(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void findByIdOrThrowNotFound() {
		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findByIdOrThrow(1L)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void validateDatesWithInvalidDates() {
		PromotionCreateRequest request = new PromotionCreateRequest("Test", null, 100, LocalDate.now().plusDays(7),
				LocalDate.now());

		when(promotionRepository.existsByTitle("Test")).thenReturn(false);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionDatesInvalidException.class);
	}
}