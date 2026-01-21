package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;

public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

	Page<BonusTransaction> findByBonusCardOrderByCreatedAtDesc(BonusCard bonusCard, Pageable pageable);

	Page<BonusTransaction> findByBonusCardUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Page<BonusTransaction> findByTypeOrderByCreatedAtDesc(@Param("type") BonusTransactionType type, Pageable pageable);

	Page<BonusTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}