package ua.lviv.bas.cinema.repository.promotion;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	boolean existsByTitle(String title);

	@Query("""
			    SELECT
			        p.id as id,
			        p.title as title,
			        p.bonusPoints as bonusPoints,
			        p.startDate as startDate,
			        p.endDate as endDate
			    FROM Promotion p
			""")
	Page<PromotionAdminProjection> findAllAdminList(Pageable pageable);

	@Query("""
			    SELECT
			        p.id as id,
			        p.title as title,
			        p.description as description,
			        p.bonusPoints as bonusPoints,
			        p.startDate as startDate,
			        p.endDate as endDate
			    FROM Promotion p
			    WHERE (p.startDate IS NULL OR p.startDate <= CURRENT_DATE)
			      AND (p.endDate IS NULL OR p.endDate >= CURRENT_DATE)
			    ORDER BY p.createdDate DESC
			""")
	List<PromotionResponseProjection> findAllActivePromotions();
}