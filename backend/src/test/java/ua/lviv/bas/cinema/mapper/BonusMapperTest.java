package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
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

class BonusMapperTest {

	private final BonusMapper mapper = Mappers.getMapper(BonusMapper.class);

	@Test
	void toBonusCardResponse_ShouldMapCorrectly() {
		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(42L);

		BonusCard bonusCard = BonusCard.builder().id(1L).user(user).pointsBalance(250)
				.lastBirthdayBonusDate(LocalDate.of(2024, 5, 15)).welcomeBonusReceived(true).build();

		BonusCardResponse response = mapper.toBonusCardResponse(bonusCard);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getUserId()).isEqualTo(42L);
		assertThat(response.getPointsBalance()).isEqualTo(250);
		assertThat(response.getLastBirthdayBonusDate()).isEqualTo(LocalDate.of(2024, 5, 15));
		assertThat(response.getWelcomeBonusReceived()).isTrue();
	}

	@Test
	void toBonusCardResponse_ShouldMapNullUser() {
		BonusCard bonusCard = BonusCard.builder().id(1L).user(null).pointsBalance(100).welcomeBonusReceived(false)
				.build();

		BonusCardResponse response = mapper.toBonusCardResponse(bonusCard);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getUserId()).isNull();
		assertThat(response.getPointsBalance()).isEqualTo(100);
		assertThat(response.getWelcomeBonusReceived()).isFalse();
	}

	@Test
	void toBonusTransactionResponse_ShouldMapCorrectly() {
		BonusCard bonusCard = Mockito.mock(BonusCard.class);
		Mockito.when(bonusCard.getPointsBalance()).thenReturn(150);

		BonusTransaction transaction = BonusTransaction.builder().id(1L).bonusCard(bonusCard)
				.type(BonusTransactionType.PURCHASE_BONUS).pointsChange(25).referenceId("PAYMENT_123")
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0)).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("PURCHASE_BONUS");
		assertThat(response.getPointsChange()).isEqualTo(25);
		assertThat(response.getReferenceId()).isEqualTo("PAYMENT_123");
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
		assertThat(response.getNewBalance()).isEqualTo(150);
	}

	@Test
	void toBonusTransactionResponse_ShouldHandleNullValues() {
		BonusCard bonusCard = Mockito.mock(BonusCard.class);
		Mockito.when(bonusCard.getPointsBalance()).thenReturn(100);

		BonusTransaction transaction = BonusTransaction.builder().id(1L).bonusCard(bonusCard)
				.type(BonusTransactionType.WELCOME_BONUS).pointsChange(100).referenceId(null).createdAt(null).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPointsChange()).isEqualTo(100);
		assertThat(response.getReferenceId()).isNull();
		assertThat(response.getCreatedAt()).isNull();
		assertThat(response.getNewBalance()).isEqualTo(100);
	}

	@Test
	void toBonusRulesResponse_ShouldMapCorrectly() {
		BonusRules rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.moneyRatio(new BigDecimal("0.1")).pointValue(new BigDecimal("1.00")).minPointsPerTransaction(50)
				.maxPointsPerTransaction(300).active(true).updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPoints()).isEqualTo(100);
		assertThat(response.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(response.getPointValue()).isEqualTo(new BigDecimal("1.00"));
		assertThat(response.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(response.getMaxPointsPerTransaction()).isEqualTo(300);
		assertThat(response.getActive()).isTrue();
		assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
	}

	@Test
	void toBonusRulesResponse_ShouldMapNullValues() {
		BonusRules rules = BonusRules.builder().id(2L).bonusType(BonusTransactionType.BIRTHDAY_BONUS).points(200)
				.moneyRatio(null).pointValue(null).minPointsPerTransaction(null).maxPointsPerTransaction(null)
				.active(false).updatedAt(null).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(2L);
		assertThat(response.getBonusType()).isEqualTo("BIRTHDAY_BONUS");
		assertThat(response.getPoints()).isEqualTo(200);
		assertThat(response.getMoneyRatio()).isNull();
		assertThat(response.getPointValue()).isNull();
		assertThat(response.getMinPointsPerTransaction()).isNull();
		assertThat(response.getMaxPointsPerTransaction()).isNull();
		assertThat(response.getActive()).isFalse();
		assertThat(response.getUpdatedAt()).isNull();
	}

	@Test
	void updateBonusRulesFromRequest_ShouldUpdateNonNullFields() {
		BonusRules existing = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS).points(0)
				.moneyRatio(new BigDecimal("0.05")).pointValue(new BigDecimal("0.50")).minPointsPerTransaction(10)
				.maxPointsPerTransaction(500).active(true).build();

		BonusRulesRequest request = BonusRulesRequest.builder().points(150).moneyRatio(new BigDecimal("0.1"))
				.active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(existing.isActive()).isFalse();
		assertThat(existing.getPointValue()).isEqualTo(new BigDecimal("0.50"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
	}

	@Test
	void toBonusRules_ShouldMapFromRequestAndType() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(100).moneyRatio(new BigDecimal("0.1"))
				.pointValue(new BigDecimal("1.00")).minPointsPerTransaction(50).maxPointsPerTransaction(300)
				.active(true).build();

		BonusRules result = mapper.toBonusRules(request, BonusTransactionType.WELCOME_BONUS);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isNull();
		assertThat(result.getBonusType()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
		assertThat(result.getPoints()).isEqualTo(100);
		assertThat(result.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(result.getPointValue()).isEqualTo(new BigDecimal("1.00"));
		assertThat(result.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(result.getMaxPointsPerTransaction()).isEqualTo(300);
		assertThat(result.isActive()).isTrue();
		assertThat(result.getUpdatedAt()).isNull();
	}

	@Test
	void toBonusRules_ShouldHandleNullValues() {
		BonusRulesRequest request = new BonusRulesRequest();

		BonusRules result = mapper.toBonusRules(request, BonusTransactionType.BIRTHDAY_BONUS);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isNull();
		assertThat(result.getBonusType()).isEqualTo(BonusTransactionType.BIRTHDAY_BONUS);
		assertThat(result.getPoints()).isNull();
		assertThat(result.getMoneyRatio()).isNull();
		assertThat(result.getPointValue()).isNull();
		assertThat(result.getMinPointsPerTransaction()).isNull();
		assertThat(result.getMaxPointsPerTransaction()).isNull();
		assertThat(result.isActive()).isFalse();
		assertThat(result.getUpdatedAt()).isNull();
	}

	@Test
	void toBonusRules_ShouldMapPartialRequest() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(50).active(true).build();

		BonusRules result = mapper.toBonusRules(request, BonusTransactionType.PURCHASE_BONUS);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isNull();
		assertThat(result.getBonusType()).isEqualTo(BonusTransactionType.PURCHASE_BONUS);
		assertThat(result.getPoints()).isEqualTo(50);
		assertThat(result.getMoneyRatio()).isNull();
		assertThat(result.getPointValue()).isNull();
		assertThat(result.getMinPointsPerTransaction()).isNull();
		assertThat(result.getMaxPointsPerTransaction()).isNull();
		assertThat(result.isActive()).isTrue();
		assertThat(result.getUpdatedAt()).isNull();
	}

	@Test
	void toBonusRules_ShouldMapWithNullActive() {
		BonusRulesRequest request = BonusRulesRequest.builder().points(100).moneyRatio(new BigDecimal("0.1")).build();

		BonusRules result = mapper.toBonusRules(request, BonusTransactionType.PURCHASE_WRITE_OFF);

		assertThat(result).isNotNull();
		assertThat(result.getPoints()).isEqualTo(100);
		assertThat(result.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(result.isActive()).isFalse();
	}
}