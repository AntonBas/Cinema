package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.projection.BonusTransactionProjection;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;

public class BonusMapperTest {

	private final BonusMapper mapper = Mappers.getMapper(BonusMapper.class);

	@Test
	void toBonusCardResponse() {
		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(42L);

		BonusCard bonusCard = BonusCard.builder().id(1L).user(user).pointsBalance(250).build();

		BonusCardResponse response = mapper.toBonusCardResponse(bonusCard);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getUserId()).isEqualTo(42L);
		assertThat(response.getPointsBalance()).isEqualTo(250);
	}

	@Test
	void toBonusCardResponseFromNull() {
		BonusCardResponse response = mapper.toBonusCardResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toBonusRulesResponse() {
		BonusRules rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.active(true).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPoints()).isEqualTo(100);
		assertThat(response.getActive()).isTrue();
	}

	@Test
	void toBonusRulesResponseFromNull() {
		BonusRulesResponse response = mapper.toBonusRulesResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toBonusTransactionResponseFromProjection() {
		BonusTransactionProjection projection = Mockito.mock(BonusTransactionProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getType()).thenReturn("PAYMENT_ACCRUAL");
		Mockito.when(projection.getTypeDisplay()).thenReturn("Payment accrual");
		Mockito.when(projection.getPointsChange()).thenReturn("+50");
		Mockito.when(projection.getNewBalance()).thenReturn(150);
		Mockito.when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("PAYMENT_ACCRUAL");
		assertThat(response.getTypeDisplay()).isEqualTo("Payment accrual");
		assertThat(response.getPointsChange()).isEqualTo("+50");
		assertThat(response.getNewBalance()).isEqualTo(150);
		assertThat(response.getCreatedAt()).isNotNull();
		assertThat(response.getBookingDetails()).isNull();
	}

	@Test
	void toBonusTransactionResponseFromNullProjection() {
		BonusTransactionResponse response = mapper.toBonusTransactionResponse((BonusTransactionProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toBookingDetails() {
		BonusTransactionProjection projection = Mockito.mock(BonusTransactionProjection.class);
		Mockito.when(projection.getMovieTitle()).thenReturn("Inception");
		Mockito.when(projection.getBookingReference()).thenReturn("BK-12345");
		Mockito.when(projection.getCinemaHall()).thenReturn("Hall 1");
		Mockito.when(projection.getSessionDateTime()).thenReturn(LocalDateTime.now());

		BonusTransactionResponse.BookingDetails details = mapper.toBookingDetails(projection);

		assertThat(details).isNotNull();
		assertThat(details.getMovieTitle()).isEqualTo("Inception");
		assertThat(details.getBookingReference()).isEqualTo("BK-12345");
		assertThat(details.getCinemaHall()).isEqualTo("Hall 1");
		assertThat(details.getSessionDateTime()).isNotNull();
	}

	@Test
	void updateBonusRulesFromRequest() {
		BonusRules existing = BonusRules.builder().points(0).active(true).build();

		BonusRulesRequest request = BonusRulesRequest.builder().points(150).active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void updateBonusRulesFromRequestWithPartialUpdate() {
		BonusRules existing = BonusRules.builder().points(100).moneyRatio(new BigDecimal("0.05"))
				.minPointsPerTransaction(10).maxPointsPerTransaction(500).active(true).build();

		BonusRulesRequest request = BonusRulesRequest.builder().points(200).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(200);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.05"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
		assertThat(existing.getActive()).isTrue();
	}

	@Test
	void updateBonusRulesFromRequestWithAllFields() {
		BonusRules existing = BonusRules.builder().build();

		BonusRulesRequest request = BonusRulesRequest.builder().points(200).moneyRatio(new BigDecimal("0.10"))
				.minPointsPerTransaction(50).maxPointsPerTransaction(1000).active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(200);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(1000);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void updateBonusRulesFromRequestWithNullRequest() {
		BonusRules existing = BonusRules.builder().points(100).active(true).build();

		mapper.updateBonusRulesFromRequest(null, existing);

		assertThat(existing.getPoints()).isEqualTo(100);
		assertThat(existing.getActive()).isTrue();
	}
}