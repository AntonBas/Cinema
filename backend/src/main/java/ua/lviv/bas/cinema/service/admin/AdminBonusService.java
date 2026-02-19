package ua.lviv.bas.cinema.service.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBonusService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
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

		return bonusMapper.toBonusRulesResponse(bonusRulesRepository.save(rules));
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
		}

		return bonusMapper.toBonusRulesResponse(bonusRulesRepository.save(rules));
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
		Page<BonusTransactionProjection> page = bonusTransactionRepository.findProjectionsByUserId(userId, pageable);

		return page.map(projection -> {
			BonusTransactionResponse response = bonusMapper.toBonusTransactionResponse(projection);
			if (projection.getMovieTitle() != null) {
				response.setBookingDetails(bonusMapper.toBookingDetails(projection));
			}
			return response;
		});
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getAllTransactions(Pageable pageable) {
		Page<BonusTransactionProjection> page = bonusTransactionRepository.findAllProjectionsBy(pageable);

		return page.map(projection -> {
			BonusTransactionResponse response = bonusMapper.toBonusTransactionResponse(projection);
			if (projection.getMovieTitle() != null) {
				response.setBookingDetails(bonusMapper.toBookingDetails(projection));
			}
			return response;
		});
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getTransactionsByType(BonusTransactionType type, Pageable pageable) {
		Page<BonusTransactionProjection> page = bonusTransactionRepository.findProjectionsByType(type, pageable);

		return page.map(projection -> {
			BonusTransactionResponse response = bonusMapper.toBonusTransactionResponse(projection);
			if (projection.getMovieTitle() != null) {
				response.setBookingDetails(bonusMapper.toBookingDetails(projection));
			}
			return response;
		});
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