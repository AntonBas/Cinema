package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@ExtendWith(MockitoExtension.class)
class BonusAdminServiceTest {

	@Mock
	private BonusRulesRepository bonusRulesRepository;

	@Mock
	private BonusTransactionRepository bonusTransactionRepository;

	@Mock
	private BonusMapper bonusMapper;

	@InjectMocks
	private BonusAdminService bonusAdminService;

	@Test
	void getAllBonusRules_ShouldReturnAllRules() {
		BonusRules rule1 = createBonusRules(1L, BonusTransactionType.WELCOME_BONUS, 100);
		BonusRules rule2 = createBonusRules(2L, BonusTransactionType.BIRTHDAY_BONUS, 200);

		when(bonusRulesRepository.findAll()).thenReturn(List.of(rule1, rule2));
		when(bonusMapper.toBonusRulesResponse(rule1)).thenReturn(new BonusRulesResponse());
		when(bonusMapper.toBonusRulesResponse(rule2)).thenReturn(new BonusRulesResponse());

		List<BonusRulesResponse> result = bonusAdminService.getAllBonusRules();

		assertThat(result).hasSize(2);
		verify(bonusRulesRepository).findAll();
		verify(bonusMapper, times(2)).toBonusRulesResponse(any());
	}

	@Test
	void getAllBonusRules_ShouldReturnEmptyList() {
		when(bonusRulesRepository.findAll()).thenReturn(List.of());

		List<BonusRulesResponse> result = bonusAdminService.getAllBonusRules();

		assertThat(result).isEmpty();
		verify(bonusRulesRepository).findAll();
	}

	@Test
	void getBonusRule_ShouldReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRules rule = createBonusRules(1L, type, 100);
		BonusRulesResponse expectedResponse = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(rule));
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminService.getBonusRule(type);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).toBonusRulesResponse(rule);
	}

	@Test
	void getBonusRule_ShouldThrowWhenNotFound() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusAdminService.getBonusRule(type)).isInstanceOf(BonusRuleNotFoundException.class);

		verify(bonusRulesRepository).findByBonusType(type);
	}

	@Test
	void updateBonusRule_ShouldUpdateAndReturnUpdated() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_BONUS;
		BonusRules existing = createBonusRules(1L, type, null);
		existing.setMoneyRatio(new BigDecimal("0.05"));

		BonusRulesRequest request = BonusRulesRequest.builder().moneyRatio(new BigDecimal("0.1")).active(false).build();

		BonusRulesResponse expectedResponse = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(existing)).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminService.updateBonusRule(type, request);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).updateBonusRulesFromRequest(request, existing);
		verify(bonusRulesRepository).save(existing);
		verify(bonusMapper).toBonusRulesResponse(existing);
	}

	@Test
	void updateBonusRule_ShouldThrowWhenNotFound() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_BONUS;
		BonusRulesRequest request = new BonusRulesRequest();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusAdminService.updateBonusRule(type, request))
				.isInstanceOf(BonusRuleNotFoundException.class);

		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper, never()).updateBonusRulesFromRequest(any(), any());
		verify(bonusRulesRepository, never()).save(any());
	}

	@Test
	void getUserTransactions_ShouldReturnPagedTransactions() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(BonusTransactionType.PURCHASE_BONUS)
				.pointsChange(25).build();

		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);
		BonusTransactionResponse response = new BonusTransactionResponse();

		when(bonusTransactionRepository.findByBonusCardUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(transaction)).thenReturn(response);

		PageResponse<BonusTransactionResponse> result = bonusAdminService.getUserTransactions(userId, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isSameAs(response);
		assertThat(result.getCurrentPage()).isZero();
		assertThat(result.getTotalPages()).isOne();
		verify(bonusTransactionRepository).findByBonusCardUserIdOrderByCreatedAtDesc(userId, pageable);
		verify(bonusMapper).toBonusTransactionResponse(transaction);
	}

	@Test
	void getUserTransactions_ShouldReturnEmptyPage() {
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 20);
		Page<BonusTransaction> emptyPage = Page.empty(pageable);

		when(bonusTransactionRepository.findByBonusCardUserIdOrderByCreatedAtDesc(userId, pageable))
				.thenReturn(emptyPage);

		PageResponse<BonusTransactionResponse> result = bonusAdminService.getUserTransactions(userId, pageable);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.isEmpty()).isTrue();
		verify(bonusTransactionRepository).findByBonusCardUserIdOrderByCreatedAtDesc(userId, pageable);
		verify(bonusMapper, never()).toBonusTransactionResponse(any());
	}

	@Test
	void getAllTransactions_ShouldReturnAllPaged() {
		Pageable pageable = PageRequest.of(0, 20);
		BonusTransaction transaction1 = BonusTransaction.builder().id(1L).type(BonusTransactionType.WELCOME_BONUS)
				.pointsChange(100).build();

		BonusTransaction transaction2 = BonusTransaction.builder().id(2L).type(BonusTransactionType.PURCHASE_BONUS)
				.pointsChange(25).build();

		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);
		BonusTransactionResponse response1 = new BonusTransactionResponse();
		BonusTransactionResponse response2 = new BonusTransactionResponse();

		when(bonusTransactionRepository.findAll(pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(transaction1)).thenReturn(response1);
		when(bonusMapper.toBonusTransactionResponse(transaction2)).thenReturn(response2);

		PageResponse<BonusTransactionResponse> result = bonusAdminService.getAllTransactions(pageable);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()).containsExactly(response1, response2);
		assertThat(result.getTotalElements()).isEqualTo(2);
		verify(bonusTransactionRepository).findAll(pageable);
		verify(bonusMapper, times(2)).toBonusTransactionResponse(any());
	}

	@Test
	void createBonusRule_ShouldCreateAndReturnRule() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesRequest request = BonusRulesRequest.builder().points(100).active(true).build();

		BonusRules rules = new BonusRules();
		rules.setBonusType(type);
		rules.setPoints(100);
		rules.setActive(true);

		BonusRules savedRules = new BonusRules();
		savedRules.setId(1L);
		savedRules.setBonusType(type);
		savedRules.setPoints(100);
		savedRules.setActive(true);

		BonusRulesResponse expectedResponse = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.empty());
		when(bonusMapper.toBonusRules(request, type)).thenReturn(rules);
		when(bonusRulesRepository.save(rules)).thenReturn(savedRules);
		when(bonusMapper.toBonusRulesResponse(savedRules)).thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminService.createBonusRule(type, request);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).toBonusRules(request, type);
		verify(bonusRulesRepository).save(rules);
		verify(bonusMapper).toBonusRulesResponse(savedRules);
	}

	@Test
	void createBonusRule_ShouldThrowWhenAlreadyExists() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRulesRequest request = new BonusRulesRequest();
		BonusRules existing = new BonusRules();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> bonusAdminService.createBonusRule(type, request))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Bonus rule already exists for type: " + type);

		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper, never()).toBonusRules(any(), any());
		verify(bonusRulesRepository, never()).save(any());
	}

	private BonusRules createBonusRules(Long id, BonusTransactionType type, Integer points) {
		BonusRules rules = new BonusRules();
		rules.setId(id);
		rules.setBonusType(type);
		rules.setPoints(points);
		rules.setActive(true);
		return rules;
	}
}