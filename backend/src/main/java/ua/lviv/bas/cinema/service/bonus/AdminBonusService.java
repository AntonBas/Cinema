package ua.lviv.bas.cinema.service.bonus;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.bonus.BonusMapper;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.service.shared.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bonusRules")
public class AdminBonusService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusMapper bonusMapper;
	private final BonusProperties bonusProperties;
	private final AuditService auditService;

	private static final Set<BonusTransactionType> RULE_TYPES = Set.of(BonusTransactionType.WELCOME_BONUS,
			BonusTransactionType.BIRTHDAY_BONUS, BonusTransactionType.BOOKING_SPEND,
			BonusTransactionType.PAYMENT_ACCRUAL);

	@Cacheable(key = "'all'")
	@Transactional(readOnly = true)
	public List<BonusRulesResponse> getAllRules() {
		return bonusRulesRepository.findAll().stream().filter(rule -> RULE_TYPES.contains(rule.getBonusType()))
				.sorted(Comparator.comparing(BonusRules::getBonusType)).map(bonusMapper::toBonusRulesResponse).toList();
	}

	@Cacheable(key = "#type")
	@Transactional(readOnly = true)
	public BonusRulesResponse getRule(BonusTransactionType type) {
		validateRuleType(type);
		BonusRules rules = getRuleByType(type);
		return bonusMapper.toBonusRulesResponse(rules);
	}

	@Caching(evict = { @CacheEvict(key = "'all'"), @CacheEvict(key = "#type"), @CacheEvict(allEntries = true) })
	@Transactional
	public BonusRulesResponse updateRule(BonusTransactionType type, BonusRulesRequest request) {
		validateRuleType(type);
		BonusRules rules = getRuleByType(type);

		Map<String, Object> oldValues = new HashMap<>();
		Map<String, Object> newValues = new HashMap<>();

		if (request.points() != null && !request.points().equals(rules.getPoints())) {
			oldValues.put("points", rules.getPoints());
			newValues.put("points", request.points());
			rules.setPoints(request.points());
		}

		if (request.moneyRatio() != null && !request.moneyRatio().equals(rules.getMoneyRatio())) {
			oldValues.put("moneyRatio", rules.getMoneyRatio());
			newValues.put("moneyRatio", request.moneyRatio());
			rules.setMoneyRatio(request.moneyRatio());
		}

		if (request.minPointsPerTransaction() != null
				&& !request.minPointsPerTransaction().equals(rules.getMinPointsPerTransaction())) {
			oldValues.put("minPoints", rules.getMinPointsPerTransaction());
			newValues.put("minPoints", request.minPointsPerTransaction());
			rules.setMinPointsPerTransaction(request.minPointsPerTransaction());
		}

		if (request.maxPointsPerTransaction() != null
				&& !request.maxPointsPerTransaction().equals(rules.getMaxPointsPerTransaction())) {
			oldValues.put("maxPoints", rules.getMaxPointsPerTransaction());
			newValues.put("maxPoints", request.maxPointsPerTransaction());
			rules.setMaxPointsPerTransaction(request.maxPointsPerTransaction());
		}

		if (request.active() != null && !request.active().equals(rules.getActive())) {
			oldValues.put("active", rules.getActive());
			newValues.put("active", request.active());
			rules.setActive(request.active());
		}

		if (type == BonusTransactionType.BOOKING_SPEND) {
			validatePointsRange(rules.getMinPointsPerTransaction(), rules.getMaxPointsPerTransaction());
		}

		BonusRules updated = bonusRulesRepository.save(rules);
		log.info("Updated bonus rule: {}", type);

		if (!oldValues.isEmpty()) {
			auditService.logChange("BonusRules", updated.getId(), "Bonus Rule " + type.name(), AuditAction.UPDATED,
					oldValues, newValues);
		}

		return bonusMapper.toBonusRulesResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "'all'"), @CacheEvict(key = "#type"), @CacheEvict(allEntries = true) })
	@Transactional
	public BonusRulesResponse resetRuleToDefaults(BonusTransactionType type) {
		validateRuleType(type);
		BonusRules rules = getRuleByType(type);

		Map<String, Object> oldValues = new HashMap<>();
		Map<String, Object> newValues = new HashMap<>();

		BonusProperties.RuleDefaults defaults = bonusProperties.getDefaults().get(type);

		if (defaults != null) {
			if (defaults.getPoints() != null && !defaults.getPoints().equals(rules.getPoints())) {
				oldValues.put("points", rules.getPoints());
				newValues.put("points", defaults.getPoints());
				rules.setPoints(defaults.getPoints());
			}

			if (defaults.getMoneyRatio() != null && !defaults.getMoneyRatio().equals(rules.getMoneyRatio())) {
				oldValues.put("moneyRatio", rules.getMoneyRatio());
				newValues.put("moneyRatio", defaults.getMoneyRatio());
				rules.setMoneyRatio(defaults.getMoneyRatio());
			}

			if (defaults.getMinPoints() != null
					&& !defaults.getMinPoints().equals(rules.getMinPointsPerTransaction())) {
				oldValues.put("minPoints", rules.getMinPointsPerTransaction());
				newValues.put("minPoints", defaults.getMinPoints());
				rules.setMinPointsPerTransaction(defaults.getMinPoints());
			}

			if (defaults.getMaxPoints() != null
					&& !defaults.getMaxPoints().equals(rules.getMaxPointsPerTransaction())) {
				oldValues.put("maxPoints", rules.getMaxPointsPerTransaction());
				newValues.put("maxPoints", defaults.getMaxPoints());
				rules.setMaxPointsPerTransaction(defaults.getMaxPoints());
			}

			if (rules.getActive() == null || !rules.getActive()) {
				oldValues.put("active", rules.getActive());
				newValues.put("active", true);
				rules.setActive(true);
			}

			log.info("Reset bonus rule {} to defaults", type);
		} else {
			log.warn("No defaults found for bonus rule type: {}", type);
		}

		BonusRules updated = bonusRulesRepository.save(rules);

		if (!oldValues.isEmpty()) {
			auditService.logChange("BonusRules", updated.getId(), "Bonus Rule " + type.name(),
					AuditAction.RESET_TO_DEFAULTS, oldValues, newValues);
		}

		return bonusMapper.toBonusRulesResponse(updated);
	}

	private BonusRules getRuleByType(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusType(type).orElseThrow(() -> new BonusRuleNotFoundException(type));
	}

	private void validatePointsRange(Integer min, Integer max) {
		if (min != null && max != null && min > max) {
			throw new InvalidMinMaxPointsException(min, max);
		}
	}

	private void validateRuleType(BonusTransactionType type) {
		if (!RULE_TYPES.contains(type)) {
			throw new IllegalArgumentException("No bonus rule configuration for type: " + type);
		}
	}
}