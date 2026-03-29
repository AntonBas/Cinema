package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

public class PromotionMapperTest {

	private PromotionMapper mapper = Mappers.getMapper(PromotionMapper.class);

	@Test
	void toPromotionResponse() {
		Promotion promotion = Promotion.builder().id(1L).title("Summer Sale").description("Summer special promotion")
				.bonusPoints(500).startDate(LocalDate.of(2024, 6, 1)).endDate(LocalDate.of(2024, 6, 30)).build();

		PromotionResponse response = mapper.toPromotionResponse(promotion);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Summer Sale");
		assertThat(response.description()).isEqualTo("Summer special promotion");
		assertThat(response.bonusPoints()).isEqualTo(500);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 6, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 6, 30));
	}

	@Test
	void toPromotionResponseFromProjection() {
		var projection = Mockito.mock(PromotionResponseProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Projection Title");
		Mockito.when(projection.getDescription()).thenReturn("Projection Description");
		Mockito.when(projection.getBonusPoints()).thenReturn(300);
		Mockito.when(projection.getStartDate()).thenReturn(LocalDate.of(2024, 7, 1));
		Mockito.when(projection.getEndDate()).thenReturn(LocalDate.of(2024, 7, 31));

		PromotionResponse response = mapper.toPromotionResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Projection Title");
		assertThat(response.description()).isEqualTo("Projection Description");
		assertThat(response.bonusPoints()).isEqualTo(300);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 7, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 7, 31));
	}

	@Test
	void toPromotion() {
		PromotionCreateRequest request = new PromotionCreateRequest("New Promotion", "New promotion description", 200,
				LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 31));

		Promotion promotion = mapper.toPromotion(request);

		assertThat(promotion.getTitle()).isEqualTo("New Promotion");
		assertThat(promotion.getDescription()).isEqualTo("New promotion description");
		assertThat(promotion.getBonusPoints()).isEqualTo(200);
		assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
		assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 7, 31));
		assertThat(promotion.getId()).isNull();
		assertThat(promotion.getCreatedAt()).isNull();
		assertThat(promotion.getUserRedemptions()).isEmpty();
	}

	@Test
	void updatePromotionFromRequest() {
		Promotion promotion = Promotion.builder().id(1L).title("Old Title").description("Old description")
				.bonusPoints(100).build();

		PromotionUpdateRequest request = new PromotionUpdateRequest("New Title", "New description", 200,
				LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31));

		mapper.updatePromotionFromRequest(promotion, request);

		assertThat(promotion.getTitle()).isEqualTo("New Title");
		assertThat(promotion.getDescription()).isEqualTo("New description");
		assertThat(promotion.getBonusPoints()).isEqualTo(200);
		assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 8, 1));
		assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 8, 31));
	}

	@Test
	void toPromotionAdminResponse() {
		Promotion promotion = Promotion.builder().id(1L).title("Admin View").bonusPoints(150)
				.startDate(LocalDate.of(2024, 9, 1)).endDate(LocalDate.of(2024, 9, 30)).build();

		PromotionAdminResponse response = mapper.toPromotionAdminResponse(promotion);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Admin View");
		assertThat(response.bonusPoints()).isEqualTo(150);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 9, 30));
	}

	@Test
	void toPromotionAdminResponseFromProjection() {
		var projection = Mockito.mock(PromotionAdminProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Admin Projection");
		Mockito.when(projection.getBonusPoints()).thenReturn(250);
		Mockito.when(projection.getStartDate()).thenReturn(LocalDate.of(2024, 10, 1));
		Mockito.when(projection.getEndDate()).thenReturn(LocalDate.of(2024, 10, 31));

		PromotionAdminResponse response = mapper.toPromotionAdminResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Admin Projection");
		assertThat(response.bonusPoints()).isEqualTo(250);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 10, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 10, 31));
	}

	@Test
	void toPromotionResponseWithNull() {
		PromotionResponse response = mapper.toPromotionResponse((Promotion) null);
		assertThat(response).isNull();
	}

	@Test
	void toPromotionWithNull() {
		Promotion promotion = mapper.toPromotion(null);
		assertThat(promotion).isNull();
	}

	@Test
	void toPromotionAdminResponseWithNull() {
		PromotionAdminResponse response = mapper.toPromotionAdminResponse((Promotion) null);
		assertThat(response).isNull();
	}
}