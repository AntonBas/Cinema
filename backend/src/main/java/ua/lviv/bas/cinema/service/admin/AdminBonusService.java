package ua.lviv.bas.cinema.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBonusService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusMapper bonusMapper;
	private final BonusProperties bonusProperties;

	@Transactional(readOnly = true)
	public List<BonusRulesResponse> getAllRules() {
		return bonusRulesRepository.findAll().stream().map(bonusMapper::toBonusRulesResponse).toList();
	}

	@Transactional(readOnly = true)
	public BonusRulesResponse getRule(BonusTransactionType type) {
		BonusRules rules = getRuleByType(type);
		return bonusMapper.toBonusRulesResponse(rules);
	}

	@Transactional
	public BonusRulesResponse updateRule(BonusTransactionType type, BonusRulesRequest request) {
		BonusRules rules = getRuleByType(type);
		bonusMapper.updateBonusRulesFromRequest(request, rules);

		if (type == BonusTransactionType.BOOKING_SPEND) {
			validatePointsRange(rules.getMinPointsPerTransaction(), rules.getMaxPointsPerTransaction());
		}

		BonusRules updated = bonusRulesRepository.save(rules);
		log.info("Updated bonus rule: {}", type);
		return bonusMapper.toBonusRulesResponse(updated);
	}

	@Transactional
	public BonusRulesResponse resetRuleToDefaults(BonusTransactionType type) {
		BonusRules rules = getRuleByType(type);
		BonusProperties.RuleDefaults defaults = bonusProperties.getDefaults().get(type);

		if (defaults != null) {
			rules.setPoints(defaults.getPoints());
			rules.setMoneyRatio(defaults.getMoneyRatio());
			rules.setMinPointsPerTransaction(defaults.getMinPoints());
			rules.setMaxPointsPerTransaction(defaults.getMaxPoints());
			rules.setActive(true);
			log.info("Reset bonus rule {} to defaults", type);
		} else {
			log.warn("No defaults found for bonus rule type: {}", type);
		}

		BonusRules updated = bonusRulesRepository.save(rules);
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
}