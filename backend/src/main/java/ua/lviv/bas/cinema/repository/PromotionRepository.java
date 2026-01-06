package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	@Query("SELECT p FROM Promotion p WHERE " + "(p.startDate IS NULL OR p.startDate <= :currentTime) AND "
			+ "(p.endDate IS NULL OR p.endDate >= :currentTime)")
	List<Promotion> findActivePromotions(@Param("currentTime") LocalDateTime currentDate);

	List<Promotion> findByTitleContainingIgnoreCase(String title);

	boolean existsByTitle(String title);

	List<Promotion> findUpcomingPromotions(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT p FROM Promotion p WHERE p.endDate < :currentTime")
	List<Promotion> findExpiredPromotions(@Param("currentTime") LocalDateTime currentTime);
}