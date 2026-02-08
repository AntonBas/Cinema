package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.BonusRules;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;

public interface BonusRulesRepository extends JpaRepository<BonusRules, Long> {
	Optional<BonusRules> findByBonusType(BonusTransactionType bonusType);

	Optional<BonusRules> findByBonusTypeAndActiveTrue(BonusTransactionType bonusType);

	boolean existsByBonusType(BonusTransactionType bonusType);

	List<BonusRules> findAllByActiveTrue();
}