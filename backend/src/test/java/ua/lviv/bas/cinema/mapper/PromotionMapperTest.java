package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;

public class PromotionMapperTest {

	private PromotionMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(PromotionMapper.class);
	}

	@Test
	void toPromotionResponse_ShouldMapAllFieldsFromPromotion() {
		LocalDate startDate = LocalDate.of(2024, 1, 1);
		LocalDate endDate = LocalDate.of(2024, 12, 31);

		Promotion promotion = Promotion.builder().id(1L).title("Summer Sale")
				.description("Get bonus points for summer purchases").bonusPoints(500).startDate(startDate)
				.endDate(endDate).build();

		PromotionResponse response = mapper.toPromotionResponse(promotion);

		assertThat(response).isNotNull()
				.extracting(PromotionResponse::getId, PromotionResponse::getTitle, PromotionResponse::getDescription,
						PromotionResponse::getBonusPoints, PromotionResponse::getStartDate,
						PromotionResponse::getEndDate)
				.containsExactly(1L, "Summer Sale", "Get bonus points for summer purchases", 500, startDate, endDate);
	}

	@Test
	void toPromotionResponse_ShouldReturnNull_WhenInputIsNull() {
		PromotionResponse response = mapper.toPromotionResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toPromotionResponse_ShouldHandlePromotionWithoutDates() {
		Promotion promotion = Promotion.builder().id(2L).title("No Date Promotion")
				.description("Promotion without dates").bonusPoints(100).startDate(null).endDate(null).build();

		PromotionResponse response = mapper.toPromotionResponse(promotion);

		assertThat(response).isNotNull();
		assertThat(response.getStartDate()).isNull();
		assertThat(response.getEndDate()).isNull();
		assertThat(response.getTitle()).isEqualTo("No Date Promotion");
	}

	@Test
	void toPromotionResponse_ShouldHandlePromotionWithZeroBonusPoints() {
		Promotion promotion = Promotion.builder().id(3L).title("Zero Points").bonusPoints(0).build();

		PromotionResponse response = mapper.toPromotionResponse(promotion);
		assertThat(response.getBonusPoints()).isZero();
	}

	@Test
	void toPromotion_ShouldMapAllFieldsFromCreateRequest() {
		LocalDate startDate = LocalDate.of(2024, 6, 1);
		LocalDate endDate = LocalDate.of(2024, 6, 30);

		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("June Promotion");
		request.setDescription("June special offer");
		request.setBonusPoints(300);
		request.setStartDate(startDate);
		request.setEndDate(endDate);

		Promotion promotion = mapper.toPromotion(request);

		assertThat(promotion).isNotNull()
				.extracting(Promotion::getId, Promotion::getTitle, Promotion::getDescription, Promotion::getBonusPoints,
						Promotion::getStartDate, Promotion::getEndDate)
				.containsExactly(null, "June Promotion", "June special offer", 300, startDate, endDate);
	}

	@Test
	void toPromotion_ShouldReturnNull_WhenRequestIsNull() {
		Promotion promotion = mapper.toPromotion(null);
		assertThat(promotion).isNull();
	}

	@Test
	void toPromotion_ShouldIgnoreId() {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Test");
		request.setBonusPoints(100);

		Promotion promotion = mapper.toPromotion(request);
		assertThat(promotion.getId()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "  ", "\t", "\n" })
	void toPromotion_ShouldHandleEmptyOrBlankTitle(String title) {
		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle(title);
		request.setBonusPoints(100);

		Promotion promotion = mapper.toPromotion(request);
		assertThat(promotion.getTitle()).isEqualTo(title);
	}

	@Test
	void updatePromotionFromRequest_ShouldUpdateOnlyNonNullFields() {
		LocalDate oldStartDate = LocalDate.of(2024, 1, 1);
		LocalDate oldEndDate = LocalDate.of(2024, 1, 31);

		Promotion existing = Promotion.builder().id(1L).title("Old Title").description("Old Description")
				.bonusPoints(100).startDate(oldStartDate).endDate(oldEndDate).build();

		PromotionUpdateRequest update = new PromotionUpdateRequest();
		update.setTitle("New Title");
		update.setBonusPoints(200);

		mapper.updatePromotionFromRequest(existing, update);

		assertThat(existing)
				.extracting(Promotion::getId, Promotion::getTitle, Promotion::getDescription, Promotion::getBonusPoints,
						Promotion::getStartDate, Promotion::getEndDate)
				.containsExactly(1L, "New Title", "Old Description", 200, oldStartDate, oldEndDate);
	}

	@Test
	void updatePromotionFromRequest_ShouldNotUpdate_WhenRequestIsNull() {
		Promotion existing = Promotion.builder().id(1L).title("Original").bonusPoints(100).build();

		mapper.updatePromotionFromRequest(existing, null);

		assertThat(existing.getTitle()).isEqualTo("Original");
		assertThat(existing.getBonusPoints()).isEqualTo(100);
	}

	@Test
	void updatePromotionFromRequest_ShouldUpdateDatesWhenProvided() {
		LocalDate newStartDate = LocalDate.of(2024, 2, 1);
		LocalDate newEndDate = LocalDate.of(2024, 2, 28);

		Promotion existing = Promotion.builder().id(1L).title("Original").bonusPoints(100)
				.startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2024, 1, 31)).build();

		PromotionUpdateRequest update = new PromotionUpdateRequest();
		update.setStartDate(newStartDate);
		update.setEndDate(newEndDate);

		mapper.updatePromotionFromRequest(existing, update);

		assertThat(existing.getStartDate()).isEqualTo(newStartDate);
		assertThat(existing.getEndDate()).isEqualTo(newEndDate);
	}

	@Test
	void updatePromotionFromRequest_ShouldNotUpdateId() {
		Promotion existing = Promotion.builder().id(999L).title("Original").build();

		PromotionUpdateRequest update = new PromotionUpdateRequest();
		update.setTitle("Updated");

		mapper.updatePromotionFromRequest(existing, update);
		assertThat(existing.getId()).isEqualTo(999L);
	}

	@Test
	void updatePromotionFromRequest_ShouldHandleNullTarget() {
		PromotionUpdateRequest update = new PromotionUpdateRequest();
		update.setTitle("Test");

		assertThatThrownBy(() -> mapper.updatePromotionFromRequest(null, update))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void toUserPromotionResponse_ShouldMapAllFieldsFromUserPromotion() {
		Promotion promotion = Promotion.builder().id(10L).title("Welcome Bonus").bonusPoints(500).build();

		User user = User.builder().id(1L).email("test@example.com").build();

		LocalDate redeemedDate = LocalDate.of(2024, 5, 15);

		UserPromotion userPromotion = UserPromotion.builder().id(100L).user(user).promotion(promotion)
				.redeemedAt(redeemedDate.atStartOfDay()).pointsAwarded(500).build();

		UserPromotionResponse response = mapper.toUserPromotionResponse(userPromotion);

		assertThat(response).isNotNull()
				.extracting(UserPromotionResponse::getId, UserPromotionResponse::getPromotionId,
						UserPromotionResponse::getPromotionTitle, UserPromotionResponse::getPointsAwarded)
				.containsExactly(100L, 10L, "Welcome Bonus", 500);
	}

	@Test
	void toUserPromotionResponse_ShouldReturnNull_WhenInputIsNull() {
		UserPromotionResponse response = mapper.toUserPromotionResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toPromotionResponseList_ShouldMapListOfPromotions() {
		List<Promotion> promotions = Arrays.asList(Promotion.builder().id(1L).title("Promo 1").bonusPoints(100).build(),
				Promotion.builder().id(2L).title("Promo 2").bonusPoints(200).build(),
				Promotion.builder().id(3L).title("Promo 3").bonusPoints(300).build());

		List<PromotionResponse> responses = mapper.toPromotionResponseList(promotions);

		assertThat(responses).isNotNull().hasSize(3).extracting(PromotionResponse::getTitle).containsExactly("Promo 1",
				"Promo 2", "Promo 3");
	}

	@Test
	void toPromotionResponseList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<PromotionResponse> responses = mapper.toPromotionResponseList(Collections.emptyList());
		assertThat(responses).isNotNull().isEmpty();
	}

	@Test
	void toPromotionResponseList_ShouldReturnNull_WhenInputIsNull() {
		List<PromotionResponse> responses = mapper.toPromotionResponseList(null);
		assertThat(responses).isNull();
	}

	@Test
	void toUserPromotionResponseList_ShouldMapListOfUserPromotions() {
		Promotion promotion = Promotion.builder().id(1L).title("Test Promotion").build();

		List<UserPromotion> userPromotions = Arrays.asList(
				UserPromotion.builder().id(1L).promotion(promotion).redeemedAt(LocalDate.now().atStartOfDay())
						.pointsAwarded(100).build(),
				UserPromotion.builder().id(2L).promotion(promotion)
						.redeemedAt(LocalDate.now().plusDays(1).atStartOfDay()).pointsAwarded(200).build());

		List<UserPromotionResponse> responses = mapper.toUserPromotionResponseList(userPromotions);

		assertThat(responses).isNotNull().hasSize(2).extracting(UserPromotionResponse::getPromotionTitle)
				.containsExactly("Test Promotion", "Test Promotion");
	}

	@Test
	void consistency_ToPromotionThenToPromotionResponse_ShouldReturnSameValues() {
		LocalDate startDate = LocalDate.of(2024, 7, 1);
		LocalDate endDate = LocalDate.of(2024, 7, 31);

		PromotionCreateRequest request = new PromotionCreateRequest();
		request.setTitle("Consistency Test");
		request.setDescription("Test description");
		request.setBonusPoints(150);
		request.setStartDate(startDate);
		request.setEndDate(endDate);

		Promotion entity = mapper.toPromotion(request);
		PromotionResponse response = mapper.toPromotionResponse(entity);

		assertThat(response.getTitle()).isEqualTo("Consistency Test");
		assertThat(response.getDescription()).isEqualTo("Test description");
		assertThat(response.getBonusPoints()).isEqualTo(150);
		assertThat(response.getStartDate()).isEqualTo(request.getStartDate());
		assertThat(response.getEndDate()).isEqualTo(request.getEndDate());
	}

	@Test
	void updatePromotionFromRequestThenToPromotionResponse_ShouldReflectChanges() {
		Promotion promotion = Promotion.builder().id(1L).title("Before").bonusPoints(100).build();

		PromotionUpdateRequest update = new PromotionUpdateRequest();
		update.setTitle("After");
		update.setBonusPoints(200);

		mapper.updatePromotionFromRequest(promotion, update);
		PromotionResponse response = mapper.toPromotionResponse(promotion);

		assertThat(response.getTitle()).isEqualTo("After");
		assertThat(response.getBonusPoints()).isEqualTo(200);
	}
}