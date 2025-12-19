package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ua.lviv.bas.cinema.domain.BonusCard;

public interface BonusCardRepository extends JpaRepository<BonusCard, Long>, QuerydslPredicateExecutor<BonusCard> {

	Optional<BonusCard> findByUserId(Long userId);

	boolean existsByUserId(Long userId);
}
