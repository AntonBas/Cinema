package ua.lviv.bas.cinema.repository.bonus;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;

import java.util.Optional;

public interface BonusRulesRepository extends JpaRepository<BonusRules, Long> {
    Optional<BonusRules> findByBonusType(BonusTransactionType bonusType);

    Optional<BonusRules> findByBonusTypeAndActiveTrue(BonusTransactionType bonusType);
}