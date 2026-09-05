package ua.lviv.bas.cinema.bonus.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;

import java.util.Optional;

public interface BonusRulesRepository extends JpaRepository<BonusRules, Long> {
    Optional<BonusRules> findByBonusType(BonusTransactionType bonusType);

    @Cacheable(value = "bonusRules", key = "'active:' + #bonusType")
    Optional<BonusRules> findByBonusTypeAndActiveTrue(BonusTransactionType bonusType);
}