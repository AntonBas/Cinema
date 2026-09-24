package ua.lviv.bas.cinema.bonus.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.bonus.domain.BonusRuleField;
import ua.lviv.bas.cinema.bonus.domain.BonusRuleSpec;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.dto.request.BonusRulesRequest;
import ua.lviv.bas.cinema.bonus.dto.response.BonusRulesResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotConfigurableException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InvalidBonusRuleFieldException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.bonus.mapper.BonusMapper;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.common.CacheableList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBonusService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusMapper bonusMapper;
	private final BonusProperties bonusProperties;
	private final AuditService auditService;

	@Cacheable(value = "bonusRules", key = "'list'")
	@Transactional(readOnly = true)
	public List<BonusRulesResponse> getRules() {
		return new CacheableList<>(bonusRulesRepository.findAll().stream()
				.filter(rule -> BonusRuleSpec.configurableTypes().contains(rule.getBonusType()))
				.sorted(Comparator.comparing(BonusRules::getBonusType)).map(bonusMapper::toResponse).toList());
	}

	@CacheEvict(value = "bonusRules", allEntries = true)
	@Transactional
	public BonusRulesResponse updateRule(BonusTransactionType type, BonusRulesRequest request) {
		var spec = getSpec(type);
		validateFields(type, spec, request);
		var rules = getRuleByType(type);
		var oldValues = captureCurrentValues(rules);

		bonusMapper.updateEntity(request, rules);

		var updated = bonusRulesRepository.save(rules);
		log.info("Updated bonus rule: {}", type);

		var newValues = captureCurrentValues(rules);
		if (!oldValues.equals(newValues)) {
			auditService.logChange("BonusRules", updated.getId(), type.name(), AuditAction.UPDATED, oldValues,
					newValues);
		}

		return bonusMapper.toResponse(updated);
	}

	@CacheEvict(value = "bonusRules", allEntries = true)
	@Transactional
	public BonusRulesResponse resetRuleToDefaults(BonusTransactionType type) {
		var spec = getSpec(type);
		var rules = getRuleByType(type);
		var oldValues = captureCurrentValues(rules);

		var defaults = bonusProperties.getDefaults().get(type);
		if (defaults != null) {
			var resetRequest = new BonusRulesRequest(
					spec.allows(BonusRuleField.POINTS) ? defaults.getPoints() : null,
					spec.allows(BonusRuleField.MONEY_RATIO) ? defaults.getMoneyRatio() : null,
					spec.allows(BonusRuleField.MIN_POINTS) ? defaults.getMinPoints() : null,
					spec.allows(BonusRuleField.MAX_POINTS) ? defaults.getMaxPoints() : null, true);
			validateFields(type, spec, resetRequest);
			bonusMapper.updateEntity(resetRequest, rules);
			log.info("Reset bonus rule {} to defaults", type);
		} else {
			log.warn("No defaults found for bonus rule type: {}", type);
		}

		var updated = bonusRulesRepository.save(rules);
		var newValues = captureCurrentValues(rules);

		if (!oldValues.equals(newValues)) {
			auditService.logChange("BonusRules", updated.getId(), type.name(), AuditAction.RESET_TO_DEFAULTS, oldValues,
					newValues);
		}

		return bonusMapper.toResponse(updated);
	}

	private BonusRules getRuleByType(BonusTransactionType type) {
		return bonusRulesRepository.findByBonusType(type).orElseThrow(() -> new BonusRuleNotFoundException(type));
	}

	private BonusRuleSpec getSpec(BonusTransactionType type) {
		return BonusRuleSpec.of(type).orElseThrow(() -> new BonusRuleNotConfigurableException(type));
	}

	private void validateFields(BonusTransactionType type, BonusRuleSpec spec, BonusRulesRequest request) {
		for (var field : BonusRuleField.values()) {
			var value = valueOf(request, field);
			if (value != null && !spec.allows(field)) {
				throw InvalidBonusRuleFieldException.notApplicable(type, field);
			}
			if (value == null && spec.requires(field)) {
				throw InvalidBonusRuleFieldException.required(type, field);
			}
		}
		validatePointsRange(request.minPointsPerTransaction(), request.maxPointsPerTransaction());
	}

	private Object valueOf(BonusRulesRequest request, BonusRuleField field) {
		return switch (field) {
			case POINTS -> request.points();
			case MONEY_RATIO -> request.moneyRatio();
			case MIN_POINTS -> request.minPointsPerTransaction();
			case MAX_POINTS -> request.maxPointsPerTransaction();
		};
	}

	private void validatePointsRange(Integer min, Integer max) {
		if (min != null && max != null && min > max) {
			throw new InvalidMinMaxPointsException(min, max);
		}
	}

	private Map<String, Object> captureCurrentValues(BonusRules rules) {
		Map<String, Object> values = new HashMap<>();
		values.put("points", rules.getPoints());
		values.put("moneyRatio", rules.getMoneyRatio());
		values.put("minPoints", rules.getMinPointsPerTransaction());
		values.put("maxPoints", rules.getMaxPointsPerTransaction());
		values.put("active", rules.getActive());
		return values;
	}
}