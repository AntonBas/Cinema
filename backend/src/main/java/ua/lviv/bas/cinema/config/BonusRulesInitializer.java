package ua.lviv.bas.cinema.config;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.repository.BonusRulesRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BonusRulesInitializer implements ApplicationRunner {

	private final BonusRulesRepository bonusRulesRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		initializeWelcomeBonusRule();
		initializeBirthdayBonusRule();
		initializePurchaseBonusRule();
		initializeWriteOffRule();
		initializeRefundDeductionRule();
		initializePromotionBonusRule();
		initializeExpirationDeductionRule();

		log.info("Bonus rules initialization completed");
	}

	private void initializeWelcomeBonusRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.WELCOME_BONUS).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.WELCOME_BONUS).points(150)
					.active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default WELCOME_BONUS rule: {} points", rule.getPoints());
		}
	}

	private void initializeBirthdayBonusRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.BIRTHDAY_BONUS).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.BIRTHDAY_BONUS).points(200)
					.active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default BIRTHDAY_BONUS rule: {} points", rule.getPoints());
		}
	}

	private void initializePurchaseBonusRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.PURCHASE_BONUS).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_BONUS)
					.moneyRatio(new BigDecimal("0.05")).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default PURCHASE_BONUS rule: {} money ratio", rule.getMoneyRatio());
		}
	}

	private void initializeWriteOffRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.PURCHASE_WRITE_OFF).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PURCHASE_WRITE_OFF)
					.minPointsPerTransaction(200).maxPointsPerTransaction(500).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default PURCHASE_WRITE_OFF rule: min={}, max={}", rule.getMinPointsPerTransaction(),
					rule.getMaxPointsPerTransaction());
		}
	}

	private void initializeRefundDeductionRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.REFUND_DEDUCTION).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.REFUND_DEDUCTION)
					.moneyRatio(new BigDecimal("0.05")).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default REFUND_DEDUCTION rule: {} money ratio", rule.getMoneyRatio());
		}
	}

	private void initializePromotionBonusRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.PROMOTION_BONUS).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PROMOTION_BONUS).points(100)
					.active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default PROMOTION_BONUS rule: {} points", rule.getPoints());
		}
	}

	private void initializeExpirationDeductionRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.EXPIRATION_DEDUCTION).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.EXPIRATION_DEDUCTION)
					.moneyRatio(new BigDecimal("0.02")).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default EXPIRATION_DEDUCTION rule: {} money ratio", rule.getMoneyRatio());
		}
	}
}