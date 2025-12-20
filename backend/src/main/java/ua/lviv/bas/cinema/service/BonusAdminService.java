package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusAdminService {

	private final BonusRulesRepository bonusRulesRepository;
	private final BonusTransactionRepository bonusTransactionRepository;
	private final BonusMapper bonusMapper;

	public List<BonusRulesResponse> getAllBonusRules() {
		log.debug("Admin: getting all bonus rules");
		return bonusRulesRepository.findAll().stream().map(bonusMapper::toBonusRulesResponse).toList();
	}

	public BonusRulesResponse getBonusRule(BonusTransactionType type) {
		log.debug("Admin: getting bonus rule for type: {}", type);
		BonusRules rules = bonusRulesRepository.findById(type).orElseThrow(() -> {
			log.error("Admin: bonus rule not found for type: {}", type);
			return new BonusRuleNotFoundException(type);
		});
		return bonusMapper.toBonusRulesResponse(rules);
	}

	public BonusRulesResponse updateBonusRule(BonusTransactionType type, BonusRulesRequest request) {
		log.info("Admin: updating bonus rule for type: {}, data: {}", type, request);
		BonusRules rules = bonusRulesRepository.findById(type).orElseThrow(() -> new BonusRuleNotFoundException(type));

		bonusMapper.updateBonusRulesFromRequest(request, rules);
		BonusRules updated = bonusRulesRepository.save(rules);

		log.info("Admin: successfully updated bonus rule for type: {}", type);
		return bonusMapper.toBonusRulesResponse(updated);
	}

	public PageResponse<BonusTransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		log.debug("Admin: getting transactions for user: {}, page: {}, size: {}", userId, pageNumber, pageSize);

		Page<BonusTransaction> page = bonusTransactionRepository.findByBonusCardUserIdOrderByCreatedAtDesc(userId,
				pageable);
		return PageResponse.of(page, bonusMapper::toBonusTransactionResponse);
	}

	public PageResponse<BonusTransactionResponse> getAllTransactions(Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		log.debug("Admin: getting all transactions, page: {}, size: {}", pageNumber, pageSize);

		Page<BonusTransaction> page = bonusTransactionRepository.findAll(pageable);
		return PageResponse.of(page, bonusMapper::toBonusTransactionResponse);
	}

}
