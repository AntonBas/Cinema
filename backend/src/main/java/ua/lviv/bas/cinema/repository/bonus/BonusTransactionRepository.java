package ua.lviv.bas.cinema.repository.bonus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.repository.bonus.projection.BonusTransactionProjection;

public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.bonusCard = :bonusCard")
	Page<BonusTransaction> findByBonusCard(@Param("bonusCard") BonusCard bonusCard, Pageable pageable);

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.bonusCard.user.id = :userId")
	Page<BonusTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.type = :type")
	Page<BonusTransaction> findByType(@Param("type") BonusTransactionType type, Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "bt.pointsChange as pointsChangeRaw, "
			+ "bt.createdDate as createdAt, "
			+ "(SELECT SUM(t.pointsChange) FROM BonusTransaction t WHERE t.bonusCard = bt.bonusCard AND t.id <= bt.id) as newBalance "
			+ "FROM BonusTransaction bt " + "WHERE bt.bonusCard.user.id = :userId " + "ORDER BY bt.createdDate DESC")
	Page<BonusTransactionProjection> findProjectionsByUserId(@Param("userId") Long userId, Pageable pageable);
}