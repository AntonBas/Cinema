package ua.lviv.bas.cinema.service.admin;

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
		initializeBookingSpendRule();
		initializePaymentAccrualRule();
		initializeRefundReturnRule();

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

	private void initializeBookingSpendRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.BOOKING_SPEND).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.BOOKING_SPEND)
					.minPointsPerTransaction(100).maxPointsPerTransaction(1000).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default BOOKING_SPEND rule: min={}, max={}", rule.getMinPointsPerTransaction(),
					rule.getMaxPointsPerTransaction());
		}
	}

	private void initializePaymentAccrualRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.PAYMENT_ACCRUAL).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.PAYMENT_ACCRUAL)
					.moneyRatio(new BigDecimal("0.05")).minPointsPerTransaction(10).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default PAYMENT_ACCRUAL rule: {} money ratio", rule.getMoneyRatio());
		}
	}

	private void initializeRefundReturnRule() {
		if (!bonusRulesRepository.findByBonusType(BonusTransactionType.REFUND_RETURN).isPresent()) {
			BonusRules rule = BonusRules.builder().bonusType(BonusTransactionType.REFUND_RETURN).active(true).build();
			bonusRulesRepository.save(rule);
			log.info("Created default REFUND_RETURN rule");
		}
	}
}