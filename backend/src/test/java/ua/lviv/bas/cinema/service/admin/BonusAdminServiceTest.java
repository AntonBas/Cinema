package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

import com.querydsl.core.BooleanBuilder;

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

		// Не використовуємо when() для void методу, а перевіряємо що він викликався
		BonusRulesResponse result = bonusAdminService.updateBonusRule(type, request);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).updateBonusRulesFromRequest(request, existing);
		verify(bonusRulesRepository).save(existing);
		verify(bonusMapper).toBonusRulesResponse(existing);
	}

	@Test
	void updateBonusRule_ShouldValidateMinMaxForWriteOff() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_WRITE_OFF;
		BonusRules existing = createBonusRules(1L, type, null);
		existing.setMinPointsPerTransaction(50);
		existing.setMaxPointsPerTransaction(300);

		BonusRulesRequest request = BonusRulesRequest.builder().minPointsPerTransaction(100)
				.maxPointsPerTransaction(200).build();

		BonusRulesResponse expectedResponse = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(existing)).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(expectedResponse);

		bonusAdminService.updateBonusRule(type, request);

		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).updateBonusRulesFromRequest(request, existing);
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void updateBonusRule_ShouldThrowWhenMinGreaterThanMax() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_WRITE_OFF;
		BonusRules existing = createBonusRules(1L, type, null);
		existing.setMinPointsPerTransaction(50);
		existing.setMaxPointsPerTransaction(300);

		BonusRulesRequest request = BonusRulesRequest.builder().minPointsPerTransaction(400)
				.maxPointsPerTransaction(200).build();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));

		doAnswer(invocation -> {
			BonusRules rules = invocation.getArgument(1);
			rules.setMinPointsPerTransaction(400);
			rules.setMaxPointsPerTransaction(200);
			return null;
		}).when(bonusMapper).updateBonusRulesFromRequest(request, existing);

		assertThatThrownBy(() -> bonusAdminService.updateBonusRule(type, request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Min points cannot be greater than max points");

		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusMapper).updateBonusRulesFromRequest(request, existing);
		verify(bonusRulesRepository, never()).save(any());
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
	void getTransactionsByType_ShouldReturnFilteredTransactions() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		Pageable pageable = PageRequest.of(0, 20);
		BonusTransaction transaction = BonusTransaction.builder().id(1L).type(type).pointsChange(100).build();

		Page<BonusTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);
		BonusTransactionResponse response = new BonusTransactionResponse();

		when(bonusTransactionRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(transaction)).thenReturn(response);

		PageResponse<BonusTransactionResponse> result = bonusAdminService.getTransactionsByType(type, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isSameAs(response);
		verify(bonusTransactionRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
		verify(bonusMapper).toBonusTransactionResponse(transaction);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetWelcomeBonus() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;
		BonusRules existing = createBonusRules(1L, type, 50);
		existing.setMoneyRatio(new BigDecimal("0.1"));
		existing.setMinPointsPerTransaction(10);
		existing.setMaxPointsPerTransaction(100);

		BonusRulesResponse expectedResponse = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(expectedResponse);

		BonusRulesResponse result = bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(result).isSameAs(expectedResponse);
		verify(bonusRulesRepository).findByBonusType(type);
		verify(bonusRulesRepository).save(existing);
		verify(bonusMapper).toBonusRulesResponse(existing);
		assertThat(existing.getPoints()).isEqualTo(150);
		assertThat(existing.getMoneyRatio()).isNull();
		assertThat(existing.getMinPointsPerTransaction()).isNull();
		assertThat(existing.getMaxPointsPerTransaction()).isNull();
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetBirthdayBonus() {
		BonusTransactionType type = BonusTransactionType.BIRTHDAY_BONUS;
		BonusRules existing = createBonusRules(1L, type, 100);

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(new BonusRulesResponse());

		bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(existing.getPoints()).isEqualTo(300);
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetPurchaseBonus() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_BONUS;
		BonusRules existing = createBonusRules(1L, type, 100);
		existing.setPoints(50);

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(new BonusRulesResponse());

		bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(existing.getPoints()).isNull();
		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.05"));
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetWriteOffRule() {
		BonusTransactionType type = BonusTransactionType.PURCHASE_WRITE_OFF;
		BonusRules existing = createBonusRules(1L, type, null);

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(new BonusRulesResponse());

		bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(existing.getMinPointsPerTransaction()).isEqualTo(20);
		assertThat(existing.getMaxPointsPerTransaction()).isEqualTo(500);
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetRefundDeduction() {
		BonusTransactionType type = BonusTransactionType.REFUND_DEDUCTION;
		BonusRules existing = createBonusRules(1L, type, null);

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(new BonusRulesResponse());

		bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(existing.getMoneyRatio()).isEqualTo(new BigDecimal("0.05"));
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldResetPromotionBonus() {
		BonusTransactionType type = BonusTransactionType.PROMOTION_BONUS;
		BonusRules existing = createBonusRules(1L, type, 50);

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.of(existing));
		when(bonusRulesRepository.save(any(BonusRules.class))).thenReturn(existing);
		when(bonusMapper.toBonusRulesResponse(existing)).thenReturn(new BonusRulesResponse());

		bonusAdminService.resetBonusRuleToDefaults(type);

		assertThat(existing.getPoints()).isEqualTo(100);
		verify(bonusRulesRepository).save(existing);
	}

	@Test
	void resetBonusRuleToDefaults_ShouldThrowWhenNotFound() {
		BonusTransactionType type = BonusTransactionType.WELCOME_BONUS;

		when(bonusRulesRepository.findByBonusType(type)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bonusAdminService.resetBonusRuleToDefaults(type))
				.isInstanceOf(BonusRuleNotFoundException.class);

		verify(bonusRulesRepository).findByBonusType(type);
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