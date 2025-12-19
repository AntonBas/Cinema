package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.BonusCard;

public interface BonusCardRepository extends JpaRepository<BonusCard, Long> {

	Optional<BonusCard> findByUserId(Long userId);

	boolean existsByUserId(Long userId);
}
