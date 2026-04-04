package ua.lviv.bas.cinema.mapper.bonus;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.repository.bonus.projection.BonusTransactionProjection;

public class BonusMapperTest {

	private final BonusMapper mapper = Mappers.getMapper(BonusMapper.class);

	@Test
	void toBonusRulesResponse() {
		BonusRules rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.active(true).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.bonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.points()).isEqualTo(100);
		assertThat(response.active()).isTrue();
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
		Mockito.when(projection.getPointsChangeRaw()).thenReturn(50);
		Mockito.when(projection.getPointsChange()).thenReturn("+50");
		Mockito.when(projection.getNewBalance()).thenReturn(150);
		Mockito.when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.type()).isEqualTo("PAYMENT_ACCRUAL");
		assertThat(response.pointsChange()).isEqualTo("+50");
		assertThat(response.newBalance()).isEqualTo(150);
		assertThat(response.createdAt()).isNotNull();
	}

	@Test
	void toBonusTransactionResponseFromNullProjection() {
		BonusTransactionResponse response = mapper.toBonusTransactionResponse((BonusTransactionProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void updateBonusRulesFromRequest() {
		BonusRules existing = BonusRules.builder().points(0).active(true).build();

		BonusRulesRequest request = new BonusRulesRequest(150, null, null, null, false);

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void updateBonusRulesFromRequestWithPartialUpdate() {
		BonusRules existing = BonusRules.builder().points(100).moneyRatio(new BigDecimal("0.05"))
				.minPointsPerTransaction(10).maxPointsPerTransaction(500).active(true).build();

		BonusRulesRequest request = new BonusRulesRequest(200, null, null, null, null);

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

		BonusRulesRequest request = new BonusRulesRequest(200, new BigDecimal("0.10"), 50, 1000, false);

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