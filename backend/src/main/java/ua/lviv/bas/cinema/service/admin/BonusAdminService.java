package ua.lviv.bas.cinema.service.admin;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.exception.domain.bonus.BonusRuleNotFoundException;
import ua.lviv.bas.cinema.exception.domain.bonus.InvalidBonusTransactionTypeException;
import ua.lviv.bas.cinema.exception.domain.bonus.InvalidMinMaxPointsException;
import ua.lviv.bas.cinema.mapper.BonusMapper;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.BonusTransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusAdminService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;

	@Transactional(readOnly = true)
	public List<BonusRulesResponse> getAllBonusRules() {
		log.debug("Admin: getting all bonus rules");
		return bonusRulesRepository.findAll().stream().map(bonusMapper::toBonusRulesResponse).toList();
	}

	@Transactional(readOnly = true)
	public BonusRulesResponse getBonusRule(BonusTransactionType type) {
		log.debug("Admin: getting bonus rule for type: {}", type);
		BonusRules rules = bonusRulesRepository.findByBonusType(type).orElseThrow(() -> {
			log.error("Admin: bonus rule not found for type: {}", type);
			return new BonusRuleNotFoundException(type);
		});
		return bonusMapper.toBonusRulesResponse(rules);
	}

	@Transactional
	public BonusRulesResponse updateBonusRule(BonusTransactionType type, BonusRulesRequest request) {
		log.info("Admin: updating bonus rule for type: {}, data: {}", type, request);
		BonusRules rules = bonusRulesRepository.findByBonusType(type)
				.orElseThrow(() -> new BonusRuleNotFoundException(type));

		bonusMapper.updateBonusRulesFromRequest(request, rules);

		if (type == BonusTransactionType.BOOKING_SPEND) {
			validateMinMaxPoints(rules.getMinPointsPerTransaction(), rules.getMaxPointsPerTransaction());
		}

		BonusRules updated = bonusRulesRepository.save(rules);
		log.info("Admin: successfully updated bonus rule for type: {}", type);
		return bonusMapper.toBonusRulesResponse(updated);
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
		log.debug("Admin: getting transactions for user: {}, page: {}, size: {}", userId, pageable.getPageNumber(),
				pageable.getPageSize());

		Page<BonusTransaction> page = bonusTransactionRepository.findByBonusCardUserIdOrderByCreatedAtDesc(userId,
				pageable);

		return page.map(bonusMapper::toBonusTransactionResponse);
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getAllTransactions(Pageable pageable) {
		log.debug("Admin: getting all transactions, page: {}, size: {}", pageable.getPageNumber(),
				pageable.getPageSize());

		Page<BonusTransaction> page = bonusTransactionRepository.findAllByOrderByCreatedAtDesc(pageable);
		return page.map(bonusMapper::toBonusTransactionResponse);
	}

	@Transactional(readOnly = true)
	public Page<BonusTransactionResponse> getTransactionsByType(BonusTransactionType type, Pageable pageable) {
		log.debug("Admin: getting transactions by type: {}, page: {}, size: {}", type, pageable.getPageNumber(),
				pageable.getPageSize());

		Page<BonusTransaction> page = bonusTransactionRepository.findByTypeOrderByCreatedAtDesc(type, pageable);

		return page.map(bonusMapper::toBonusTransactionResponse);
	}

	@Transactional
	public BonusRulesResponse resetBonusRuleToDefaults(BonusTransactionType type) {
		log.info("Admin: resetting bonus rule to defaults for type: {}", type);

		BonusRules rules = bonusRulesRepository.findByBonusType(type)
				.orElseThrow(() -> new BonusRuleNotFoundException(type));

		switch (type) {
		case WELCOME_BONUS:
			rules.setPoints(150);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(null);
			rules.setMaxPointsPerTransaction(null);
			break;

		case BIRTHDAY_BONUS:
			rules.setPoints(200);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(null);
			rules.setMaxPointsPerTransaction(null);
			break;

		case PROMOTION_BONUS:
			rules.setPoints(100);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(null);
			rules.setMaxPointsPerTransaction(null);
			break;

		case BOOKING_SPEND:
			rules.setPoints(null);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(100);
			rules.setMaxPointsPerTransaction(1000);
			validateMinMaxPoints(100, 1000);
			break;

		case PAYMENT_ACCRUAL:
			rules.setPoints(null);
			rules.setMoneyRatio(new BigDecimal("0.05")); // 5% від суми
			rules.setMinPointsPerTransaction(10);
			rules.setMaxPointsPerTransaction(null);
			break;

		case REFUND_RETURN:
			rules.setPoints(null);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(null);
			rules.setMaxPointsPerTransaction(null);
			break;

		case BOOKING_CANCEL:
			rules.setPoints(null);
			rules.setMoneyRatio(null);
			rules.setMinPointsPerTransaction(null);
			rules.setMaxPointsPerTransaction(null);
			break;

		default:
			log.warn("Unknown bonus transaction type: {}", type);
			throw new InvalidBonusTransactionTypeException(type.name());
		}

		BonusRules updated = bonusRulesRepository.save(rules);
		log.info("Admin: successfully reset bonus rule for type: {} to defaults", type);
		return bonusMapper.toBonusRulesResponse(updated);
	}

	private void validateMinMaxPoints(Integer min, Integer max) {
		if (min != null && max != null && min > max) {
			throw new InvalidMinMaxPointsException(min, max);
		}
	}
}