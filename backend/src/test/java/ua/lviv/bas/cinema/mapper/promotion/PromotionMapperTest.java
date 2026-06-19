package ua.lviv.bas.cinema.mapper.promotion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionRequest;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionListProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromotionMapperTest {

    private final PromotionMapper mapper = Mappers.getMapper(PromotionMapper.class);

    @Mock
    private PromotionResponseProjection responseProjection;

    @Mock
    private PromotionListProjection listProjection;

    @Test
    void toPromotionResponseFromEntity() {
        var promotion = Promotion.builder().id(1L).title("Summer Sale").description("Summer special promotion")
                .bonusPoints(500).startDate(LocalDate.of(2024, 6, 1)).endDate(LocalDate.of(2024, 6, 30)).build();

        var response = mapper.toPromotionResponse(promotion);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Summer Sale");
        assertThat(response.description()).isEqualTo("Summer special promotion");
        assertThat(response.bonusPoints()).isEqualTo(500);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    void toPromotionResponseFromProjection() {
        when(responseProjection.getId()).thenReturn(1L);
        when(responseProjection.getTitle()).thenReturn("Projection Title");
        when(responseProjection.getDescription()).thenReturn("Projection Description");
        when(responseProjection.getBonusPoints()).thenReturn(300);
        when(responseProjection.getStartDate()).thenReturn(LocalDate.of(2024, 7, 1));
        when(responseProjection.getEndDate()).thenReturn(LocalDate.of(2024, 7, 31));

        var response = mapper.toPromotionResponse(responseProjection);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Projection Title");
        assertThat(response.description()).isEqualTo("Projection Description");
        assertThat(response.bonusPoints()).isEqualTo(300);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 7, 31));
    }

    @Test
    void toPromotionFromRequest() {
        var request = new PromotionRequest("New Promotion", "New promotion description", 200, LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 31));

        var promotion = mapper.toPromotion(request);

        assertThat(promotion.getTitle()).isEqualTo("New Promotion");
        assertThat(promotion.getDescription()).isEqualTo("New promotion description");
        assertThat(promotion.getBonusPoints()).isEqualTo(200);
        assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 7, 31));
        assertThat(promotion.getId()).isNull();
        assertThat(promotion.getUserRedemptions()).isEmpty();
    }

    @Test
    void updatePromotionFromRequest() {
        var promotion = Promotion.builder().id(1L).title("Old Title").description("Old description").bonusPoints(100)
                .build();

        var request = new PromotionRequest("New Title", "New description", 200, LocalDate.of(2024, 8, 1),
                LocalDate.of(2024, 8, 31));

        mapper.updatePromotionFromRequest(request, promotion);

        assertThat(promotion.getTitle()).isEqualTo("New Title");
        assertThat(promotion.getDescription()).isEqualTo("New description");
        assertThat(promotion.getBonusPoints()).isEqualTo(200);
        assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 8, 1));
        assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 8, 31));
    }

    @Test
    void updatePromotionFromRequestWithNullValues() {
        var promotion = Promotion.builder().id(1L).title("Old Title").description("Old description").bonusPoints(100)
                .startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2024, 1, 31)).build();

        var request = new PromotionRequest(null, null, null, null, null);

        mapper.updatePromotionFromRequest(request, promotion);

        assertThat(promotion.getTitle()).isEqualTo("Old Title");
        assertThat(promotion.getDescription()).isEqualTo("Old description");
        assertThat(promotion.getBonusPoints()).isEqualTo(100);
        assertThat(promotion.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(promotion.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    }

    @Test
    void toPromotionListResponseFromProjection() {
        when(listProjection.getId()).thenReturn(1L);
        when(listProjection.getTitle()).thenReturn("List Projection");
        when(listProjection.getBonusPoints()).thenReturn(250);
        when(listProjection.getStartDate()).thenReturn(LocalDate.of(2024, 10, 1));
        when(listProjection.getEndDate()).thenReturn(LocalDate.of(2024, 10, 31));

        var response = mapper.toPromotionListResponse(listProjection);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("List Projection");
        assertThat(response.bonusPoints()).isEqualTo(250);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 10, 31));
    }

    @Test
    void toPromotionResponseWithNullEntity() {
        var response = mapper.toPromotionResponse((Promotion) null);
        assertThat(response).isNull();
    }

    @Test
    void toPromotionResponseWithNullProjection() {
        var response = mapper.toPromotionResponse((PromotionResponseProjection) null);
        assertThat(response).isNull();
    }

    @Test
    void toPromotionWithNullRequest() {
        var promotion = mapper.toPromotion(null);
        assertThat(promotion).isNull();
    }

    @Test
    void toPromotionListResponseWithNullProjection() {
        var response = mapper.toPromotionListResponse(null);
        assertThat(response).isNull();
    }
}