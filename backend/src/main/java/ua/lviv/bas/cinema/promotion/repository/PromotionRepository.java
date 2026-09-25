package ua.lviv.bas.cinema.promotion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.promotion.domain.Promotion;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionListProjection;
import ua.lviv.bas.cinema.promotion.repository.projection.PromotionResponseProjection;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    @Query("""
            SELECT p.id as id,
                   p.title as title,
                   p.description as description,
                   p.bonusPoints as bonusPoints,
                   p.startDate as startDate,
                   p.endDate as endDate,
                   p.active as active
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
                p.endDate as endDate,
                   p.active as active
            FROM Promotion p
            WHERE (:query IS NULL OR
                   LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
            ORDER BY p.createdDate DESC, p.id DESC
            """)
    Page<PromotionListProjection> findAllAdminProjections(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT
                p.id as id,
                p.title as title,
                p.description as description,
                p.bonusPoints as bonusPoints,
                p.startDate as startDate,
                p.endDate as endDate,
                   p.active as active
            FROM Promotion p
            WHERE p.active = true
              AND (p.startDate IS NULL OR p.startDate <= :today)
              AND (p.endDate IS NULL OR p.endDate >= :today)
            ORDER BY p.createdDate DESC, p.id DESC
            """)
    List<PromotionResponseProjection> findAllActivePromotions(@Param("today") LocalDate today);
}