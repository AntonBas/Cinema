package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.projection.BonusTransactionProjection;

public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

	Page<BonusTransaction> findByBonusCard(BonusCard bonusCard, Pageable pageable);

	Page<BonusTransaction> findByUserId(Long userId, Pageable pageable);

	Page<BonusTransaction> findByType(BonusTransactionType type, Pageable pageable);

	@Query("SELECT bt FROM BonusTransaction bt " + "LEFT JOIN bt.booking b " + "LEFT JOIN b.movieSession ms "
			+ "LEFT JOIN ms.movie m " + "LEFT JOIN ms.cinemaHall ch " + "WHERE bt.bonusCard.user.id = :userId "
			+ "ORDER BY bt.createdAt DESC")
	Page<BonusTransactionProjection> findProjectionsByUserId(@Param("userId") Long userId, Pageable pageable);
}