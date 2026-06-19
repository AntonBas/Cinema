package ua.lviv.bas.cinema.repository.promotion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionListProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByTitle(String title);

    @Query("""
            SELECT p.id as id,
                   p.title as title,
                   p.description as description,
                   p.bonusPoints as bonusPoints,
                   p.startDate as startDate,
                   p.endDate as endDate
            FROM UserPromotion up
            JOIN up.promotion p
            WHERE up.user = :user
            """)
    List<PromotionResponseProjection> findClaimedPromotionsByUser(@Param("user") User user);

    @Query("""
            SELECT
                p.id as id,
                p.title as title,
                p.bonusPoints as bonusPoints,
                p.startDate as startDate,
                p.endDate as endDate
            FROM Promotion p
            WHERE (:query IS NULL OR
                   LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
            """)
    Page<PromotionListProjection> findAllAdminProjections(@Param("query") String query, Pageable pageable);

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