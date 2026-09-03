package ua.lviv.bas.cinema.bonus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.repository.projection.BonusTransactionProjection;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

    @Query(value = """
            SELECT
                bt.id as id,
                bt.type as type,
                bt.points_change as pointsChangeRaw,
                bt.created_date as createdAt,
                SUM(bt.points_change) OVER (
                    PARTITION BY bt.bonus_card_id
                    ORDER BY bt.id
                    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                ) as newBalance
            FROM bonus_transactions bt
            JOIN bonus_cards bc ON bc.id = bt.bonus_card_id
            WHERE bc.user_id = :userId
            ORDER BY bt.created_date DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM bonus_transactions bt
            JOIN bonus_cards bc ON bc.id = bt.bonus_card_id
            WHERE bc.user_id = :userId
            """, nativeQuery = true)
    Page<BonusTransactionProjection> findProjectionsByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByReferenceId(String referenceId);
}