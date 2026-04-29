package ua.lviv.bas.cinema.service.bonus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotConfigurableException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.bonus.BonusMapper;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminBonusServiceTest {

    @Mock
    private BonusRulesRepository bonusRulesRepository;

    @Mock
    private BonusMapper bonusMapper;

    @Mock
    private BonusProperties bonusProperties;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminBonusService adminBonusService;

    private static final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
    private static final BonusTransactionType BIRTHDAY = BonusTransactionType.BIRTHDAY_BONUS;
    private static final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;
    private static final BonusTransactionType ACCRUAL = BonusTransactionType.PAYMENT_ACCRUAL;
    private static final BonusTransactionType REFUND = BonusTransactionType.REFUND_RETURN;

    @Test
    void getRulesShouldReturnOnlyConfigurableRuleTypes() {
        BonusRules welcomeRule = createRule(WELCOME);
        BonusRules birthdayRule = createRule(BIRTHDAY);
        BonusRules spendRule = createRule(SPEND);
        BonusRules accuracyRule = createRule(ACCRUAL);
        BonusRules refundRule = createRule(REFUND);

        when(bonusRulesRepository.findAll())
                .thenReturn(List.of(welcomeRule, birthdayRule, spendRule, accuracyRule, refundRule));
        when(bonusMapper.toResponse(any(BonusRules.class))).thenAnswer(inv -> new BonusRulesResponse(1L,
                inv.getArgument(0, BonusRules.class).getBonusType(), 100, new BigDecimal("0.05"), 10, 500, true));

        List<BonusRulesResponse> result = adminBonusService.getRules();

        assertThat(result).hasSize(4);
        assertThat(result).extracting(BonusRulesResponse::bonusType).containsExactlyInAnyOrder(WELCOME, BIRTHDAY, SPEND,
                ACCRUAL);
        verify(bonusRulesRepository).findAll();
    }

    @Test
    void getRulesShouldReturnEmptyListWhenNoRules() {
        when(bonusRulesRepository.findAll()).thenReturn(List.of());

        List<BonusRulesResponse> result = adminBonusService.getRules();

        assertThat(result).isEmpty();
        verify(bonusRulesRepository).findAll();
    }

    @Test
    void updateRuleShouldUpdateSuccessfully() {
        BonusRules rule = createRuleWithValues(WELCOME, 50, new BigDecimal("0.05"), 10, 500);
        BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, false);
        BonusRulesResponse response = new BonusRulesResponse(1L, WELCOME, 100, new BigDecimal("0.05"), 10, 500, false);

        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
        when(bonusRulesRepository.save(rule)).thenReturn(rule);
        when(bonusMapper.toResponse(rule)).thenReturn(response);

        doAnswer(invocation -> {
            BonusRulesRequest req = invocation.getArgument(0);
            BonusRules r = invocation.getArgument(1);
            if (req.points() != null)
                r.setPoints(req.points());
            if (req.active() != null)
                r.setActive(req.active());
            return null;
        }).when(bonusMapper).updateFromRequest(any(BonusRulesRequest.class), any(BonusRules.class));

        BonusRulesResponse result = adminBonusService.updateRule(WELCOME, request);

        assertThat(result).isEqualTo(response);
        verify(bonusMapper).updateFromRequest(request, rule);
        verify(bonusRulesRepository).save(rule);
        verify(auditService).logChange(eq("BonusRules"), eq(1L), eq("WELCOME_BONUS"), eq(AuditAction.UPDATED), any(),
                any());
    }

    @Test
    void updateRuleShouldThrowExceptionWhenRuleNotFound() {
        BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, true);

        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminBonusService.updateRule(WELCOME, request))
                .isInstanceOf(BonusRuleNotFoundException.class);
    }

    @Test
    void updateRuleShouldThrowExceptionWhenTypeNotConfigurable() {
        BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, true);

        assertThatThrownBy(() -> adminBonusService.updateRule(REFUND, request))
                .isInstanceOf(BonusRuleNotConfigurableException.class);
    }

    @Test
    void updateRuleShouldValidatePointsRangeForBookingSpend() {
        BonusRules rule = createRuleWithValues(SPEND, null, null, null, null);
        BonusRulesRequest request = new BonusRulesRequest(null, null, 500, 100, null);

        when(bonusRulesRepository.findByBonusType(SPEND)).thenReturn(Optional.of(rule));

        doAnswer(invocation -> {
            BonusRulesRequest req = invocation.getArgument(0);
            BonusRules r = invocation.getArgument(1);
            if (req.minPointsPerTransaction() != null)
                r.setMinPointsPerTransaction(req.minPointsPerTransaction());
            if (req.maxPointsPerTransaction() != null)
                r.setMaxPointsPerTransaction(req.maxPointsPerTransaction());
            return null;
        }).when(bonusMapper).updateFromRequest(any(BonusRulesRequest.class), any(BonusRules.class));

        assertThatThrownBy(() -> adminBonusService.updateRule(SPEND, request))
                .isInstanceOf(InvalidMinMaxPointsException.class);

        verify(bonusRulesRepository, never()).save(any());
    }

    @Test
    void updateRuleShouldNotLogAuditWhenNoChanges() {
        BonusRules rule = createRuleWithValues(WELCOME, 100, new BigDecimal("0.05"), 10, 500);
        BonusRulesRequest request = new BonusRulesRequest(100, null, null, null, true);
        BonusRulesResponse response = new BonusRulesResponse(1L, WELCOME, 100, new BigDecimal("0.05"), 10, 500, true);

        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
        when(bonusRulesRepository.save(rule)).thenReturn(rule);
        when(bonusMapper.toResponse(rule)).thenReturn(response);

        doAnswer(invocation -> null).when(bonusMapper).updateFromRequest(any(), any());

        adminBonusService.updateRule(WELCOME, request);

        verify(auditService, never()).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void resetRuleToDefaultsShouldResetWhenDefaultsExist() {
        BonusRules rule = createRuleWithValues(WELCOME, 100, new BigDecimal("0.05"), 10, 500);
        BonusRulesResponse response = new BonusRulesResponse(1L, WELCOME, 200, new BigDecimal("0.10"), 50, 500, true);

        BonusProperties.RuleDefaults defaults = new BonusProperties.RuleDefaults();
        defaults.setPoints(200);
        defaults.setMoneyRatio(new BigDecimal("0.10"));
        defaults.setMinPoints(50);
        defaults.setMaxPoints(500);

        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
        when(bonusProperties.getDefaults()).thenReturn(Map.of(WELCOME, defaults));
        when(bonusRulesRepository.save(rule)).thenReturn(rule);
        when(bonusMapper.toResponse(rule)).thenReturn(response);

        doAnswer(invocation -> {
            BonusRulesRequest req = invocation.getArgument(0);
            BonusRules r = invocation.getArgument(1);
            r.setPoints(req.points());
            r.setMoneyRatio(req.moneyRatio());
            r.setMinPointsPerTransaction(req.minPointsPerTransaction());
            r.setMaxPointsPerTransaction(req.maxPointsPerTransaction());
            r.setActive(req.active());
            return null;
        }).when(bonusMapper).updateFromRequest(any(BonusRulesRequest.class), any(BonusRules.class));

        BonusRulesResponse result = adminBonusService.resetRuleToDefaults(WELCOME);

        assertThat(result).isEqualTo(response);
        verify(bonusRulesRepository).save(rule);
        verify(auditService).logChange(eq("BonusRules"), eq(1L), eq("WELCOME_BONUS"), eq(AuditAction.RESET_TO_DEFAULTS),
                any(), any());
    }

    @Test
    void resetRuleToDefaultsShouldNotChangeWhenDefaultsNull() {
        BonusRules rule = createRuleWithValues(WELCOME, 100, null, null, null);
        BonusRulesResponse response = new BonusRulesResponse(1L, WELCOME, 100, null, null, null, true);

        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.of(rule));
        when(bonusProperties.getDefaults()).thenReturn(Map.of());
        when(bonusRulesRepository.save(rule)).thenReturn(rule);
        when(bonusMapper.toResponse(rule)).thenReturn(response);

        BonusRulesResponse result = adminBonusService.resetRuleToDefaults(WELCOME);

        assertThat(result).isEqualTo(response);
        verify(bonusRulesRepository).save(rule);
    }

    @Test
    void resetRuleToDefaultsShouldThrowExceptionWhenRuleNotFound() {
        when(bonusRulesRepository.findByBonusType(WELCOME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminBonusService.resetRuleToDefaults(WELCOME))
                .isInstanceOf(BonusRuleNotFoundException.class);
    }

    @Test
    void resetRuleToDefaultsShouldThrowExceptionWhenTypeNotConfigurable() {
        assertThatThrownBy(() -> adminBonusService.resetRuleToDefaults(REFUND))
                .isInstanceOf(BonusRuleNotConfigurableException.class);
    }

    private BonusRules createRule(BonusTransactionType type) {
        BonusRules rule = new BonusRules();
        rule.setId(1L);
        rule.setBonusType(type);
        return rule;
    }

    private BonusRules createRuleWithValues(BonusTransactionType type, Integer points, BigDecimal moneyRatio,
                                            Integer minPoints, Integer maxPoints) {
        BonusRules rule = new BonusRules();
        rule.setId(1L);
        rule.setBonusType(type);
        rule.setPoints(points);
        rule.setMoneyRatio(moneyRatio);
        rule.setMinPointsPerTransaction(minPoints);
        rule.setMaxPointsPerTransaction(maxPoints);
        rule.setActive(true);
        return rule;
    }
}