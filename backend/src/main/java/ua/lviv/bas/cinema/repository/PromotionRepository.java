package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	boolean existsByTitle(String title);

	@Query("""
			    SELECT
			        p.id as id,
			        p.title as title,
			        p.description as description,
			        p.bonusPoints as bonusPoints,
			        p.startDate as startDate,
			        p.endDate as endDate
			    FROM Promotion p
			    WHERE (:activeOnly = false OR
			           (p.startDate IS NULL OR p.startDate <= CURRENT_DATE)
			           AND (p.endDate IS NULL OR p.endDate >= CURRENT_DATE))
			    ORDER BY p.createdAt DESC
			""")
	List<PromotionResponseProjection> findAllPromotions(@Param("activeOnly") boolean activeOnly);

	@Query("""
			    SELECT
			        p.id as id,
			        p.title as title,
			        p.description as description,
			        p.bonusPoints as bonusPoints,
			        p.startDate as startDate,
			        p.endDate as endDate
			    FROM Promotion p
			    WHERE p.id = :promotionId
			""")
	PromotionResponseProjection findPromotionById(@Param("promotionId") Long promotionId);
}