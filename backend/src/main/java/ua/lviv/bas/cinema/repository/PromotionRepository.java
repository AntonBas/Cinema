package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	boolean existsByTitle(String title);
}