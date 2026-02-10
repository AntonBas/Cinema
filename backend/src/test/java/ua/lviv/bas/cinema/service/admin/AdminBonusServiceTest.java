package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@ExtendWith(MockitoExtension.class)
class AdminBonusServiceTest {

	@Mock
	private BonusRulesRepository bonusRulesRepository;

	@Mock
	private BonusTransactionRepository bonusTransactionRepository;

	@Mock
	private BonusMapper bonusMapper;

	@InjectMocks
	private AdminBonusService service;

	@Test
	void getAllRules() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findAll()).thenReturn(List.of(rule));
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		List<BonusRulesResponse> result = service.getAllRules();

		assertThat(result).hasSize(1);
	}

	@Test
	void getRule() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(BonusTransactionType.WELCOME_BONUS)).thenReturn(Optional.of(rule));
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.getRule(BonusTransactionType.WELCOME_BONUS);

		assertThat(result).isNotNull();
	}

	@Test
	void getRuleNotFound() {
		when(bonusRulesRepository.findByBonusType(BonusTransactionType.WELCOME_BONUS)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getRule(BonusTransactionType.WELCOME_BONUS))
				.isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void updateRule() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.WELCOME_BONUS);
		BonusRulesRequest request = new BonusRulesRequest();
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(BonusTransactionType.WELCOME_BONUS)).thenReturn(Optional.of(rule));
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.updateRule(BonusTransactionType.WELCOME_BONUS, request);

		assertThat(result).isNotNull();
		verify(bonusMapper).updateBonusRulesFromRequest(request, rule);
	}

	@Test
	void updateBookingSpendWithInvalidPoints() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(BonusTransactionType.BOOKING_SPEND);
		BonusRulesRequest request = new BonusRulesRequest();
		request.setMinPointsPerTransaction(100);
		request.setMaxPointsPerTransaction(50);

		when(bonusRulesRepository.findByBonusType(BonusTransactionType.BOOKING_SPEND)).thenReturn(Optional.of(rule));

		assertThatThrownBy(() -> service.updateRule(BonusTransactionType.BOOKING_SPEND, request))
				.isInstanceOf(InvalidMinMaxPointsException.class);
	}

	@Test
	void getAllTransactions() {
		Pageable pageable = Pageable.unpaged();
		BonusTransaction transaction = new BonusTransaction();
		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction));

		when(bonusTransactionRepository.findAll(pageable)).thenReturn(page);

		Page<BonusTransactionResponse> result = service.getAllTransactions(pageable);

		assertThat(result).isNotNull();
	}

	@Test
	void getUserTransactions() {
		Pageable pageable = Pageable.unpaged();
		BonusTransaction transaction = new BonusTransaction();
		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction));

		when(bonusTransactionRepository.findByUserId(1L, pageable)).thenReturn(page);

		Page<BonusTransactionResponse> result = service.getUserTransactions(1L, pageable);

		assertThat(result).isNotNull();
	}
}