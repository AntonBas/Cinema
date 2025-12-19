package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;

public interface BonusRulesRepository extends JpaRepository<BonusRules, BonusTransactionType> {

	Optional<BonusRules> findByBonusType(BonusTransactionType bonusType);

	Optional<BonusRules> findByBonusTypeAndIsActiveTrue(BonusTransactionType bonusType);
}
