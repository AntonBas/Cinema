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
import ua.lviv.bas.cinema.repository.bonus.projection.BonusTransactionProjection;

public class BonusMapperTest {

	private final BonusMapper mapper = Mappers.getMapper(BonusMapper.class);

	@Test
	void toResponseFromBonusRules() {
		var rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100).active(true)
				.build();

		var response = mapper.toResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.bonusType()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
		assertThat(response.points()).isEqualTo(100);
		assertThat(response.active()).isTrue();
	}

	@Test
	void toResponseFromNullBonusRules() {
		var response = mapper.toResponse((BonusRules) null);
		assertThat(response).isNull();
	}

	@Test
	void toResponseFromProjection() {
		var projection = Mockito.mock(BonusTransactionProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getType()).thenReturn("PAYMENT_ACCRUAL");
		Mockito.when(projection.getPointsChangeRaw()).thenReturn(50);
		Mockito.when(projection.getPointsChange()).thenReturn("+50");
		Mockito.when(projection.getNewBalance()).thenReturn(150);
		Mockito.when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

		var response = mapper.toResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.type()).isEqualTo(BonusTransactionType.PAYMENT_ACCRUAL);
		assertThat(response.pointsChange()).isEqualTo("+50");
		assertThat(response.newBalance()).isEqualTo(150);
		assertThat(response.createdAt()).isNotNull();
	}

	@Test
	void toResponseFromNullProjection() {
		var response = mapper.toResponse((BonusTransactionProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void updateFromRequest() {
		var existing = BonusRules.builder().points(0).active(true).build();
		var request = new BonusRulesRequest(150, null, null, null, false);

		mapper.updateFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void updateFromRequestWithPartialUpdate() {
		var existing = BonusRules.builder().points(100).moneyRatio(new BigDecimal("0.05")).minPointsPerTransaction(10)
				.maxPointsPerTransaction(500).active(true).build();

		var request = new BonusRulesRequest(200, null, null, null, null);

		mapper.updateFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(200);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.05"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
		assertThat(existing.getActive()).isTrue();
	}

	@Test
	void updateFromRequestWithAllFields() {
		var existing = BonusRules.builder().build();
		var request = new BonusRulesRequest(200, new BigDecimal("0.10"), 50, 1000, false);

		mapper.updateFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(200);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(1000);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void updateFromRequestWithNullRequest() {
		var existing = BonusRules.builder().points(100).active(true).build();

		mapper.updateFromRequest(null, existing);

		assertThat(existing.getPoints()).isEqualTo(100);
		assertThat(existing.getActive()).isTrue();
	}
}