package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
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

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getUserId()).isEqualTo(42L);
		assertThat(response.getPointsBalance()).isEqualTo(250);
	}

	@Test
	void toBonusTransactionResponseFromTransaction() {
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.PAYMENT_ACCRUAL)
				.createdAt(LocalDateTime.now()).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("PAYMENT_ACCRUAL");
	}

	@Test
	void toBonusRulesResponse() {
		BonusRules rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.active(true).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPoints()).isEqualTo(100);
		assertThat(response.getActive()).isTrue();
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
	void updateBonusRulesFromRequestWithNull() {
		BonusRules existing = BonusRules.builder().points(100).active(true).build();

		BonusRulesRequest request = BonusRulesRequest.builder().active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(100);
		assertThat(existing.getActive()).isFalse();
	}

	@Test
	void toBonusCardResponseFromNull() {
		BonusCardResponse response = mapper.toBonusCardResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toBonusRulesResponseFromNull() {
		BonusRulesResponse response = mapper.toBonusRulesResponse(null);
		assertThat(response).isNull();
	}
}