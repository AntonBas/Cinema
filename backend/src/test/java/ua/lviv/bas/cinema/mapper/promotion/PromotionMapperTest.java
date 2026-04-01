package ua.lviv.bas.cinema.mapper.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

@ExtendWith(MockitoExtension.class)
public class PromotionMapperTest {

	private PromotionMapper mapper = Mappers.getMapper(PromotionMapper.class);

	@Mock
	private PromotionResponseProjection responseProjection;

	@Mock
	private PromotionAdminProjection adminProjection;

	@Test
	void toPromotionResponse_FromEntity_ShouldMapAllFields() {
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
	void toPromotionResponse_FromProjection_ShouldMapAllFields() {
		when(responseProjection.getId()).thenReturn(1L);
		when(responseProjection.getTitle()).thenReturn("Projection Title");
		when(responseProjection.getDescription()).thenReturn("Projection Description");
		when(responseProjection.getBonusPoints()).thenReturn(300);
		when(responseProjection.getStartDate()).thenReturn(LocalDate.of(2024, 7, 1));
		when(responseProjection.getEndDate()).thenReturn(LocalDate.of(2024, 7, 31));

		PromotionResponse response = mapper.toPromotionResponse(responseProjection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Projection Title");
		assertThat(response.description()).isEqualTo("Projection Description");
		assertThat(response.bonusPoints()).isEqualTo(300);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 7, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 7, 31));
	}

	@Test
	void toPromotion_ShouldMapCreateRequestToEntity() {
		PromotionCreateRequest request = new PromotionCreateRequest("New Promotion", "New promotion description", 200,
				LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 31));

		Promotion promotion = mapper.toPromotion(request);

		assertThat(promotion.getTitle()).isEqualTo("New Promotion");
		assertThat(promotion.getDescription()).isEqualTo("New promotion description");
		assertThat(promotion.getBonusPoints()).isEqualTo(200);
		assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
		assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 7, 31));
		assertThat(promotion.getId()).isNull();
		assertThat(promotion.getUserRedemptions()).isEmpty();
		assertThat(promotion.getCreatedBy()).isNull();
		assertThat(promotion.getCreatedDate()).isNull();
		assertThat(promotion.getLastModifiedBy()).isNull();
		assertThat(promotion.getLastModifiedDate()).isNull();
	}

	@Test
	void updatePromotionFromRequest_ShouldUpdateAllFields() {
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
	void updatePromotionFromRequest_WithNullValues_ShouldIgnoreNull() {
		Promotion promotion = Promotion.builder().id(1L).title("Old Title").description("Old description")
				.bonusPoints(100).startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2024, 1, 31)).build();

		PromotionUpdateRequest request = new PromotionUpdateRequest(null, null, null, null, null);

		mapper.updatePromotionFromRequest(promotion, request);

		assertThat(promotion.getTitle()).isEqualTo("Old Title");
		assertThat(promotion.getDescription()).isEqualTo("Old description");
		assertThat(promotion.getBonusPoints()).isEqualTo(100);
		assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
		assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 31));
	}

	@Test
	void toPromotionAdminResponse_FromProjection_ShouldMapAllFields() {
		when(adminProjection.getId()).thenReturn(1L);
		when(adminProjection.getTitle()).thenReturn("Admin Projection");
		when(adminProjection.getBonusPoints()).thenReturn(250);
		when(adminProjection.getStartDate()).thenReturn(LocalDate.of(2024, 10, 1));
		when(adminProjection.getEndDate()).thenReturn(LocalDate.of(2024, 10, 31));

		PromotionAdminResponse response = mapper.toPromotionAdminResponse(adminProjection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Admin Projection");
		assertThat(response.bonusPoints()).isEqualTo(250);
		assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 10, 1));
		assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 10, 31));
	}

	@Test
	void toPromotionResponse_WithNullEntity_ShouldReturnNull() {
		PromotionResponse response = mapper.toPromotionResponse((Promotion) null);
		assertThat(response).isNull();
	}

	@Test
	void toPromotionResponse_WithNullProjection_ShouldReturnNull() {
		PromotionResponse response = mapper.toPromotionResponse((PromotionResponseProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toPromotion_WithNullRequest_ShouldReturnNull() {
		Promotion promotion = mapper.toPromotion(null);
		assertThat(promotion).isNull();
	}

	@Test
	void toPromotionAdminResponse_WithNullProjection_ShouldReturnNull() {
		PromotionAdminResponse response = mapper.toPromotionAdminResponse((PromotionAdminProjection) null);
		assertThat(response).isNull();
	}
}