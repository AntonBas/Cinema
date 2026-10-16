package ua.lviv.bas.cinema.bonus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BonusCardRepository extends JpaRepository<BonusCard, Long> {

    Optional<BonusCard> findByUserId(Long userId);
}