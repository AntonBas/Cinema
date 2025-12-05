package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

}
