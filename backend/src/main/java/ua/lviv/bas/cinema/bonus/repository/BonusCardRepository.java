package ua.lviv.bas.cinema.bonus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;

import java.util.Optional;

public interface BonusCardRepository extends JpaRepository<BonusCard, Long> {

    Optional<BonusCard> findByUserId(Long userId);
}