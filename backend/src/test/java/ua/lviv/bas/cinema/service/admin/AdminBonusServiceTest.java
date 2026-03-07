package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.projection.BonusTransactionProjection;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@ExtendWith(MockitoExtension.class)
public class AdminBonusServiceTest {

	@Mock
	private BonusRulesRepository bonusRulesRepository;
	@Mock
	private BonusTransactionRepository bonusTransactionRepository;
	@Mock
	private BonusMapper bonusMapper;
	@Mock
	private BonusProperties bonusProperties;
	@InjectMocks
	private AdminBonusService service;

	private final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
	private final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;
	private final Long USER_ID = 1L;

	@Test
	void getAllRules_ReturnsList() {
		BonusRules rule1 = new BonusRules();
		BonusRules rule2 = new BonusRules();
		BonusRulesResponse response1 = new BonusRulesResponse();
		BonusRulesResponse response2 = new BonusRulesResponse();

		when(bonusRulesRepository.findAll()).thenReturn(List.of(rule1, rule2));
		when(bonusMapper.toBonusRulesResponse(rule1)).thenReturn(response1);
		when(bonusMapper.toBonusRulesResponse(rule2)).thenReturn(response2);

		List<BonusRulesResponse> result = service.getAllRules();

		assertThat(result).hasSize(2);
		verify(bonusRulesRepository).findAll();
	}

	@Test
	void getRule_ReturnsRule() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.getRule(WELCOME);

		assertThat(result).isEqualTo(response);
		verify(bonusRulesRepository).findByBonusType(WELCOME);
	}

	@Test
	void getRule_WhenNotFound_ThrowsException() {
		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getRule(WELCOME)).isInstanceOf(BonusRuleNotFoundException.class);
	}

	@Test
	void updateRule_Success() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(WELCOME);
		BonusRulesRequest request = new BonusRulesRequest();
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.updateRule(WELCOME, request);

		assertThat(result).isEqualTo(response);
		verify(bonusMapper).updateBonusRulesFromRequest(request, rule);
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void updateRule_ForBookingSpend_ValidatesPoints() {
		BonusRules rule = new BonusRules();
		rule.setBonusType(SPEND);

		BonusRulesRequest request = BonusRulesRequest.builder().minPointsPerTransaction(100).maxPointsPerTransaction(50)
				.build();

		when(bonusRulesRepository.findByBonusType(SPEND)).thenReturn(Optional.of(rule));

		doAnswer(invocation -> {
			BonusRulesRequest req = invocation.getArgument(0);
			BonusRules r = invocation.getArgument(1);
			r.setMinPointsPerTransaction(req.getMinPointsPerTransaction());
			r.setMaxPointsPerTransaction(req.getMaxPointsPerTransaction());
			return null;
		}).when(bonusMapper).updateBonusRulesFromRequest(any(BonusRulesRequest.class), any(BonusRules.class));

		assertThatThrownBy(() -> service.updateRule(SPEND, request)).isInstanceOf(InvalidMinMaxPointsException.class);

		verify(bonusRulesRepository, never()).save(any());
	}

	@Test
	void resetRuleToDefaults_WhenDefaultsExist_UpdatesRule() {
		BonusRules rule = new BonusRules();
		BonusRulesResponse response = new BonusRulesResponse();

		BonusProperties.RuleDefaults defaults = new BonusProperties.RuleDefaults();
		defaults.setPoints(200);
		defaults.setMoneyRatio(new BigDecimal("0.10"));
		defaults.setMinPoints(50);
		defaults.setMaxPoints(500);

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusProperties.getDefaults()).thenReturn(Map.of(WELCOME, defaults));
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.resetRuleToDefaults(WELCOME);

		assertThat(result).isEqualTo(response);
		assertThat(rule.getPoints()).isEqualTo(200);
		assertThat(rule.getMoneyRatio()).isEqualTo(new BigDecimal("0.10"));
		assertThat(rule.getMinPointsPerTransaction()).isEqualTo(50);
		assertThat(rule.getMaxPointsPerTransaction()).isEqualTo(500);
		assertThat(rule.getActive()).isTrue();
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void resetRuleToDefaults_WhenDefaultsNull_DoesNotUpdate() {
		BonusRules rule = new BonusRules();
		rule.setPoints(100);
		BonusRulesResponse response = new BonusRulesResponse();

		when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
		when(bonusProperties.getDefaults()).thenReturn(Map.of());
		when(bonusRulesRepository.save(rule)).thenReturn(rule);
		when(bonusMapper.toBonusRulesResponse(rule)).thenReturn(response);

		BonusRulesResponse result = service.resetRuleToDefaults(WELCOME);

		assertThat(result).isEqualTo(response);
		assertThat(rule.getPoints()).isEqualTo(100);
		verify(bonusRulesRepository).save(rule);
	}

	@Test
	void getUserTransactions_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		BonusTransactionProjection projection = createProjection("Inception");
		Page<BonusTransactionProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
		BonusTransactionResponse response = new BonusTransactionResponse();

		when(bonusTransactionRepository.findProjectionsByUserId(USER_ID, pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(projection)).thenReturn(response);
		when(bonusMapper.toBookingDetails(projection)).thenReturn(new BonusTransactionResponse.BookingDetails());

		Page<BonusTransactionResponse> result = service.getUserTransactions(USER_ID, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(bonusTransactionRepository).findProjectionsByUserId(USER_ID, pageable);
	}

	@Test
	void getAllTransactions_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		BonusTransactionProjection projection = createProjection(null);
		Page<BonusTransactionProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
		BonusTransactionResponse response = new BonusTransactionResponse();

		when(bonusTransactionRepository.findAllProjectionsBy(pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(projection)).thenReturn(response);

		Page<BonusTransactionResponse> result = service.getAllTransactions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(bonusTransactionRepository).findAllProjectionsBy(pageable);
	}

	@Test
	void getTransactionsByType_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		BonusTransactionProjection projection = createProjection("Inception");
		Page<BonusTransactionProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
		BonusTransactionResponse response = new BonusTransactionResponse();

		when(bonusTransactionRepository.findProjectionsByType(WELCOME, pageable)).thenReturn(page);
		when(bonusMapper.toBonusTransactionResponse(projection)).thenReturn(response);
		when(bonusMapper.toBookingDetails(projection)).thenReturn(new BonusTransactionResponse.BookingDetails());

		Page<BonusTransactionResponse> result = service.getTransactionsByType(WELCOME, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(bonusTransactionRepository).findProjectionsByType(WELCOME, pageable);
	}

	private BonusTransactionProjection createProjection(String movieTitle) {
		return new BonusTransactionProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getType() {
				return "WELCOME_BONUS";
			}

			@Override
			public String getTypeDisplay() {
				return "Welcome bonus";
			}

			@Override
			public Integer getPointsChangeRaw() {
				return 150;
			}

			@Override
			public LocalDateTime getCreatedAt() {
				return LocalDateTime.now();
			}

			@Override
			public Integer getNewBalance() {
				return 250;
			}

			@Override
			public String getMovieTitle() {
				return movieTitle;
			}

			@Override
			public String getBookingReference() {
				return "BK-123";
			}

			@Override
			public String getCinemaHall() {
				return "Hall 1";
			}

			@Override
			public LocalDateTime getSessionDateTime() {
				return LocalDateTime.now();
			}
		};
	}
}