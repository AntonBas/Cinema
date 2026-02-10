package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionDatesInvalidException;
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

		assertThat(result.getId()).isEqualTo(1L);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void createPromotionWithExistingTitle() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Existing Promotion");

		when(promotionRepository.existsByTitle("Existing Promotion")).thenReturn(true);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionAlreadyExistsException.class);
	}

	@Test
	void createPromotionWithInvalidDates() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Test");
		request.setStartDate(LocalDate.now().plusDays(7));
		request.setEndDate(LocalDate.now());

		when(promotionRepository.existsByTitle("Test")).thenReturn(false);

		assertThatThrownBy(() -> service.createPromotion(request)).isInstanceOf(PromotionDatesInvalidException.class);
	}

	@Test
	void updatePromotion() {
		Promotion promotion = Promotion.builder().id(1L).build();
		PromotionUpdateRequest request = new PromotionUpdateRequest();
		request.setTitle("Updated");
		PromotionResponse response = new PromotionResponse();

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
		when(promotionRepository.save(promotion)).thenReturn(promotion);
		when(promotionMapper.toPromotionResponse(promotion)).thenReturn(response);

		PromotionResponse result = service.updatePromotion(1L, request);

		assertThat(result).isNotNull();
		verify(promotionMapper).updatePromotionFromRequest(promotion, request);
		verify(promotionRepository).save(promotion);
	}

	@Test
	void updatePromotionNotFound() {
		PromotionUpdateRequest request = new PromotionUpdateRequest();

		when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updatePromotion(1L, request)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void deletePromotion() {
		Promotion promotion = Promotion.builder().id(1L).build();

		when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

		service.deletePromotion(1L);

		verify(promotionRepository).delete(promotion);
	}

	@Test
	void getPromotionById() {
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

		PromotionResponse response = new PromotionResponse();
		response.setId(1L);

		when(promotionRepository.findPromotionById(1L)).thenReturn(projection);
		when(promotionMapper.toPromotionResponse(projection)).thenReturn(response);

		PromotionResponse result = service.getPromotionById(1L);

		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getPromotionByIdNotFound() {
		when(promotionRepository.findPromotionById(1L)).thenReturn(null);

		assertThatThrownBy(() -> service.getPromotionById(1L)).isInstanceOf(PromotionNotFoundException.class);
	}

	@Test
	void getAllPromotions() {
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

		when(promotionRepository.findAllPromotions(false)).thenReturn(projections);
		when(promotionMapper.toPromotionResponseListFromProjections(projections)).thenReturn(Arrays.asList(response));

		List<PromotionResponse> result = service.getAllPromotions();

		assertThat(result).hasSize(1);
	}

	@Test
	void getAllPromotionsEmpty() {
		when(promotionRepository.findAllPromotions(false)).thenReturn(Collections.emptyList());
		when(promotionMapper.toPromotionResponseListFromProjections(Collections.emptyList()))
				.thenReturn(Collections.emptyList());

		List<PromotionResponse> result = service.getAllPromotions();

		assertThat(result).isEmpty();
	}

	@Test
	void getActivePromotions() {
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

		when(promotionRepository.findAllPromotions(true)).thenReturn(projections);
		when(promotionMapper.toPromotionResponseListFromProjections(projections)).thenReturn(Arrays.asList(response));

		List<PromotionResponse> result = service.getActivePromotions();

		assertThat(result).hasSize(1);
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
	void isPromotionActiveWithNull() {
		boolean result = service.isPromotionActive(null);
		assertThat(result).isFalse();
	}
}