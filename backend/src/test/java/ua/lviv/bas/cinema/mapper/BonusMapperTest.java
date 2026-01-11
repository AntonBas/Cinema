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
				.type(BonusTransactionType.PAYMENT_ACCRUAL).pointsChange(25).referenceId("PAYMENT_123")
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0)).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("PAYMENT_ACCRUAL");
		assertThat(response.getPointsChange()).isEqualTo(25);
		assertThat(response.getReferenceId()).isEqualTo("PAYMENT_123");
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
		assertThat(response.getNewBalance()).isEqualTo(150);
	}

	@Test
	void toBonusTransactionResponse_ShouldHandleNullBonusCard() {
		BonusTransaction transaction = BonusTransaction.builder().id(1L).bonusCard(null)
				.type(BonusTransactionType.REFUND_RETURN).pointsChange(-50).referenceId("REFUND_123")
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0)).build();

		BonusTransactionResponse response = mapper.toBonusTransactionResponse(transaction);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getType()).isEqualTo("REFUND_RETURN");
		assertThat(response.getPointsChange()).isEqualTo(-50);
		assertThat(response.getReferenceId()).isEqualTo("REFUND_123");
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
		assertThat(response.getNewBalance()).isNull();
	}

	@Test
	void toBonusRulesResponse_ShouldMapCorrectly() {
		BonusRules rules = BonusRules.builder().id(1L).bonusType(BonusTransactionType.WELCOME_BONUS).points(100)
				.moneyRatio(new BigDecimal("0.1")).minPointsPerTransaction(50).maxPointsPerTransaction(300).active(true)
				.updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).build();

		BonusRulesResponse response = mapper.toBonusRulesResponse(rules);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBonusType()).isEqualTo("WELCOME_BONUS");
		assertThat(response.getPoints()).isEqualTo(100);
		assertThat(response.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(response.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(response.getMaxPointsPerTransaction()).isEqualTo(300);
		assertThat(response.getActive()).isTrue();
		assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
	}

	@Test
	void updateBonusRulesFromRequest_ShouldUpdateNonNullFields() {
		BonusRules existing = BonusRules.builder().bonusType(BonusTransactionType.PAYMENT_ACCRUAL).points(0)
				.moneyRatio(new BigDecimal("0.05")).minPointsPerTransaction(10).maxPointsPerTransaction(500)
				.active(true).build();

		BonusRulesRequest request = BonusRulesRequest.builder().points(150).moneyRatio(new BigDecimal("0.1"))
				.active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(existing.isActive()).isFalse();
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
		assertThat(existing.getBonusType()).isEqualTo(BonusTransactionType.PAYMENT_ACCRUAL);
	}

	@Test
	void updateBonusRulesFromRequest_ShouldIgnoreNullFields() {
		BonusRules existing = BonusRules.builder().bonusType(BonusTransactionType.PAYMENT_ACCRUAL).points(100)
				.moneyRatio(new BigDecimal("0.1")).minPointsPerTransaction(10).maxPointsPerTransaction(500).active(true)
				.build();

		BonusRulesRequest request = BonusRulesRequest.builder().active(false).build();

		mapper.updateBonusRulesFromRequest(request, existing);

		assertThat(existing.getPoints()).isEqualTo(100);
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.1"));
		assertThat(existing.isActive()).isFalse();
		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(10);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
	}
}