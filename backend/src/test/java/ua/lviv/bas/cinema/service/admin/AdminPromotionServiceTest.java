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
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.dto.common.PageResponse;
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
	private AdminPromotionService service;

	@Test
	void createPromotion() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Test Promotion");
		request.setStartDate(LocalDate.now());
		request.setEndDate(LocalDate.now().plusDays(7));

		Promotion promotion = Promotion.builder().id(1L).title("Test Promotion").build();
		PromotionResponse response = new PromotionResponse();
		response.setId(1L);

		when(promotionRepository.existsByTitle("Test Promotion")).thenReturn(false);
		when(promotionMapper.toPromotion(request)).thenReturn(promotion);
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.createPromotion(request);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void createPromotionWithExistingTitle() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Existing Promotion");

		when(promotionRepository.existsByTitle("Existing Promotion")).thenReturn(true);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionAlreadyExistsException.class);
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void createPromotionWithInvalidDates() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Test");
		request.setStartDate(LocalDate.now().plusDays(7));
		request.setEndDate(LocalDate.now());

		when(promotionRepository.existsByTitle("Test")).thenReturn(false);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionDatesInvalidException.class);
		verify(promotionRepository, never()).save(any());
	}

	@Test
	void updatePromotion() {
		Promotion promotion = Promotion.builder().id(1L).build();
		PromotionUpdateRequest request = new PromotionUpdateRequest();
		request.setTitle("Updated");
		PromotionResponse response = new PromotionResponse();
		response.setId(1L);

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.updatePromotion(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(promotionMapper).updatePromotionFromRequest(promotion, request);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void updatePromotionNotFound() {
		PromotionUpdateRequest request = new PromotionUpdateRequest();

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
		PromotionResponse response = new PromotionResponse();
		response.setId(1L);

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.getPromotionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getPromotionByIdNotFound() {
		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getPromotionById(1L)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getAllPromotions() {
		Pageable pageable = PageRequest.of(0, 10);
		Promotion promotion = Promotion.builder().id(1L).build();
		Page<Promotion> page = new PageImpl<>(Arrays.asList(promotion));
		PromotionResponse response = new PromotionResponse();

		when(promotionRepository.findAll(pageable)).thenReturn(page);
		when(promotionMapper.toPromotionResponseList(page.getContent())).thenReturn(Arrays.asList(response));

		PageResponse<PromotionResponse> result = service.getAllPromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getAllPromotionsEmpty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Promotion> page = new PageImpl<>(Collections.emptyList());

		when(promotionRepository.findAll(pageable)).thenReturn(page);
		when(promotionMapper.toPromotionResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

		PageResponse<PromotionResponse> result = service.getAllPromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void getActivePromotions() {
		Pageable pageable = PageRequest.of(0, 10);
		PromotionResponseProjection projection = new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getTitle() {
				return "Test";
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

		List<PromotionResponseProjection> projections = Arrays.asList(projection);
		PromotionResponse response = new PromotionResponse();
		response.setId(1L);

		when(promotionRepository.findAllPromotions(true)).thenReturn(projections);
		when(promotionMapper.toPromotionResponseListFromProjections(projections)).thenReturn(Arrays.asList(response));

		PageResponse<PromotionResponse> result = service.getActivePromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getActivePromotionsEmpty() {
		Pageable pageable = PageRequest.of(0, 10);

		when(promotionRepository.findAllPromotions(true)).thenReturn(Collections.emptyList());
		when(promotionMapper.toPromotionResponseListFromProjections(Collections.emptyList()))
				.thenReturn(Collections.emptyList());

		PageResponse<PromotionResponse> result = service.getActivePromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void getActivePromotionsWithPagination() {
		Pageable pageable = PageRequest.of(1, 2);
		PromotionResponseProjection projection1 = new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getTitle() {
				return "Test1";
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
		PromotionResponseProjection projection2 = new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return 2L;
			}

			@Override
			public String getTitle() {
				return "Test2";
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
		PromotionResponseProjection projection3 = new PromotionResponseProjection() {
			@Override
			public Long getId() {
				return 3L;
			}

			@Override
			public String getTitle() {
				return "Test3";
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

		List<PromotionResponseProjection> projections = Arrays.asList(projection1, projection2, projection3);
		List<PromotionResponse> responses = Arrays.asList(new PromotionResponse(), new PromotionResponse(),
				new PromotionResponse());

		when(promotionRepository.findAllPromotions(true)).thenReturn(projections);
		when(promotionMapper.toPromotionResponseListFromProjections(projections)).thenReturn(responses);

		PageResponse<PromotionResponse> result = service.getActivePromotions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getNumber()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(2);
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
	void isPromotionActive() {
		Promotion promotion = Promotion.builder().startDate(LocalDate.now().minusDays(1))
				.endDate(LocalDate.now().plusDays(1)).build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActiveBeforeStart() {
		Promotion promotion = Promotion.builder().startDate(LocalDate.now().plusDays(1))
				.endDate(LocalDate.now().plusDays(2)).build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActiveAfterEnd() {
		Promotion promotion = Promotion.builder().startDate(LocalDate.now().minusDays(2))
				.endDate(LocalDate.now().minusDays(1)).build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isFalse();
	}

	@Test
	void isPromotionActiveWithNoDates() {
		Promotion promotion = Promotion.builder().build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActiveWithNoStartDate() {
		Promotion promotion = Promotion.builder().endDate(LocalDate.now().plusDays(1)).build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActiveWithNoEndDate() {
		Promotion promotion = Promotion.builder().startDate(LocalDate.now().minusDays(1)).build();

		boolean result = service.isPromotionActive(promotion);

		assertThat(result).isTrue();
	}

	@Test
	void isPromotionActiveWithNull() {
		boolean result = service.isPromotionActive(null);
		assertThat(result).isFalse();
	}
}