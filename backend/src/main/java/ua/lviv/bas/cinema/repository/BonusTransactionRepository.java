package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusTransaction;

public interface BonusTransactionRepository
		extends JpaRepository<BonusTransaction, Long>, QuerydslPredicateExecutor<BonusTransaction> {

	Page<BonusTransaction> findByBonusCardOrderByCreatedAtDesc(BonusCard bonusCard, Pageable pageable);

	Page<BonusTransaction> findByBonusCardUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	List<BonusTransaction> findByBonusCard(BonusCard bonusCard);
}
