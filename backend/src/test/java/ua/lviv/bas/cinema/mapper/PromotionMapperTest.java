package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;

public class PromotionMapperTest {

	private PromotionMapper mapper = Mappers.getMapper(PromotionMapper.class);

	@Test
	void toPromotionResponse() {
		Promotion promotion = Promotion.builder().id(1L).title("Summer Sale").bonusPoints(500)
				.startDate(LocalDate.of(2024, 6, 1)).endDate(LocalDate.of(2024, 6, 30)).build();

		PromotionResponse response = mapper.toPromotionResponse(promotion);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Summer Sale");
		assertThat(response.getBonusPoints()).isEqualTo(500);
	}

	@Test
	void toPromotionResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Projection Title");
		Mockito.when(projection.getBonusPoints()).thenReturn(300);

		PromotionResponse response = mapper.toPromotionResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Projection Title");
		assertThat(response.getBonusPoints()).isEqualTo(300);
	}

	@Test
	void toPromotion() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("New Promotion");
		request.setBonusPoints(200);
		request.setStartDate(LocalDate.of(2024, 7, 1));

		Promotion promotion = mapper.toPromotion(request);

		assertThat(promotion.getTitle()).isEqualTo("New Promotion");
		assertThat(promotion.getBonusPoints()).isEqualTo(200);
		assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
	}

	@Test
	void updatePromotionFromRequest() {
		Promotion promotion = Promotion.builder().id(1L).title("Old Title").bonusPoints(100).build();

		PromotionUpdateRequest request = new PromotionUpdateRequest();
		request.setTitle("New Title");
		request.setBonusPoints(200);

		mapper.updatePromotionFromRequest(promotion, request);

		assertThat(promotion.getTitle()).isEqualTo("New Title");
		assertThat(promotion.getBonusPoints()).isEqualTo(200);
	}

	@Test
	void toUserPromotionResponse() {
		Promotion promotion = Promotion.builder().id(1L).title("Promo Title").build();

		User user = User.builder().id(1L).build();

		UserPromotion userPromotion = UserPromotion.builder().id(100L).user(user).promotion(promotion)
				.pointsAwarded(500).redeemedAt(LocalDateTime.now()).build();

		UserPromotionResponse response = mapper.toUserPromotionResponse(userPromotion);

		assertThat(response.getId()).isEqualTo(100L);
		assertThat(response.getPromotionId()).isEqualTo(1L);
		assertThat(response.getPromotionTitle()).isEqualTo("Promo Title");
		assertThat(response.getPointsAwarded()).isEqualTo(500);
	}

	@Test
	void toUserPromotionResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.UserPromotionResponseProjection.class);
		Mockito.when(projection.getId()).thenReturn(100L);
		Mockito.when(projection.getPromotionId()).thenReturn(1L);
		Mockito.when(projection.getPromotionTitle()).thenReturn("Projection Promo");
		Mockito.when(projection.getPointsAwarded()).thenReturn(300);
		Mockito.when(projection.getClaimedAt()).thenReturn(LocalDateTime.now());

		UserPromotionResponse response = mapper.toUserPromotionResponse(projection);

		assertThat(response.getId()).isEqualTo(100L);
		assertThat(response.getPromotionId()).isEqualTo(1L);
		assertThat(response.getPromotionTitle()).isEqualTo("Projection Promo");
		assertThat(response.getPointsAwarded()).isEqualTo(300);
	}

	@Test
	void toPromotionResponseList() {
		List<Promotion> promotions = Arrays.asList(Promotion.builder().id(1L).title("Promo 1").build(),
				Promotion.builder().id(2L).title("Promo 2").build());

		List<PromotionResponse> responses = mapper.toPromotionResponseList(promotions);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getTitle()).isEqualTo("Promo 1");
		assertThat(responses.get(1).getTitle()).isEqualTo("Promo 2");
	}

	@Test
	void toUserPromotionResponseList() {
		Promotion promotion = Promotion.builder().id(1L).title("Promo").build();

		List<UserPromotion> userPromotions = Arrays.asList(UserPromotion.builder().id(1L).promotion(promotion).build(),
				UserPromotion.builder().id(2L).promotion(promotion).build());

		List<UserPromotionResponse> responses = mapper.toUserPromotionResponseList(userPromotions);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getPromotionTitle()).isEqualTo("Promo");
		assertThat(responses.get(1).getPromotionTitle()).isEqualTo("Promo");
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
	void toUserPromotionResponseWithNull() {
		UserPromotionResponse response = mapper.toUserPromotionResponse((UserPromotion) null);
		assertThat(response).isNull();
	}
}