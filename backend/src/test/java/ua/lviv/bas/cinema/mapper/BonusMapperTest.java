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
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.PURCHASE_BONUS)
				.pointsChange(25).referenceId("PAYMENT_123").createdAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0))
				.build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("PURCHASE_BONUS");
		assertThat(response.getPointsChange()).isEqualTo(25);
		assertThat(response.getReferenceId()).isEqualTo("PAYMENT_123");
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
	}

	@Test
	void toBonusTransactionResponse_ShouldHandleNullValues() {
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.WELCOME_BONUS)
				.pointsChange(100).referenceId(null).createdAt(null).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPointsChange()).isEqualTo(100);
		assertThat(response.getReferenceId()).isNull();
		assertThat(response.getCreatedAt()).isNull();
	}

	@Test
	void toBonusRulesResponse_ShouldMapCorrectly() {
		BonusRules rules = BonusRules.builder().bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.moneyRatio(new BigDecimal("0.1")).pointValue(new BigDecimal("1.00")).minPointsPerTransaction(50)
				.maxPointsPerTransaction(300).isActive(true).updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
				.build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPoints()).isEqualTo(100);
		assertThat(response.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(response.getPointValue()).isEqualTo(new BigDecimal("1.00"));
		assertThat(response.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(response.getMaxPointsPerTransaction()).isEqualTo(300);
		assertThat(response.getIsActive()).isTrue();
		assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
	}

	@Test
	void toBonusRulesResponse_ShouldMapNullValues() {
		BonusRules rules = BonusRules.builder().bonusType(BonusTransactionType.BIRTHDAY_BONUS).points(200)
				.moneyRatio(null).pointValue(null).minPointsPerTransaction(null).maxPointsPerTransaction(null)
				.isActive(false).updatedAt(null).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getBonusType()).isEqualTo("BIRTHDAY_BONUS");
		assertThat(response.getPoints()).isEqualTo(200);
		assertThat(response.getMoneyRatio()).isNull();
		assertThat(response.getPointValue()).isNull();
		assertThat(response.getMinPointsPerTransaction()).isNull();
		assertThat(response.getMaxPointsPerTransaction()).isNull();
		assertThat(response.getIsActive()).isFalse();
		assertThat(response.getUpdatedAt()).isNull();
	}

	@Test
	void updateBonusRulesFromRequest_ShouldUpdateNonNullFields() {
		BonusRules existing = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS).points(0)
				.moneyRatio(new BigDecimal("0.05")).pointValue(new BigDecimal("0.50")).minPointsPerTransaction(10)
				.maxPointsPerTransaction(500).isActive(true).build();

		BonusRulesRequest request = new BonusRulesRequest();
		request.setPoints(150);
		request.setMoneyRatio(new BigDecimal("0.1"));
		request.setIsActive(false);

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(existing.getIsActive()).isFalse();
		assertThat(existing.getPointValue()).isEqualTo(new BigDecimal("0.50"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
	}

	@Test
	void updateBonusRulesFromRequest_ShouldIgnoreNullFields() {
		BonusRules existing = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_WRITE_OFF).points(0)
				.moneyRatio(new BigDecimal("0.05")).pointValue(new BigDecimal("1.00")).minPointsPerTransaction(50)
				.maxPointsPerTransaction(300).isActive(true).build();

		BonusRulesRequest request = new BonusRulesRequest();
		request.setPoints(null);
		request.setMoneyRatio(null);
		request.setPointValue(new BigDecimal("2.00"));
		request.setIsActive(null);

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(0);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.05"));
		assertThat(existing.getPointValue()).isEqualTo(new BigDecimal("2.00"));
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(300);
		assertThat(existing.getIsActive()).isTrue();
	}
}