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

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.bonusCard.user.id = :userId")
	Page<BonusTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

	Page<BonusTransaction> findByType(BonusTransactionType type, Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "bt.pointsChange as pointsChangeRaw, "
			+ "bt.createdAt as createdAt, " + "bt.bonusCard.pointsBalance as newBalance, "
			+ "b.session.movie.title as movieTitle, " + "CAST(b.id as string) as bookingReference, "
			+ "b.session.hall.name as cinemaHall, " + "b.session.startTime as sessionDateTime "
			+ "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b " + "WHERE bt.bonusCard.user.id = :userId "
			+ "ORDER BY bt.createdAt DESC")
	Page<BonusTransactionProjection> findProjectionsByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "bt.pointsChange as pointsChangeRaw, "
			+ "bt.createdAt as createdAt, " + "bt.bonusCard.pointsBalance as newBalance, "
			+ "b.session.movie.title as movieTitle, " + "CAST(b.id as string) as bookingReference, "
			+ "b.session.hall.name as cinemaHall, " + "b.session.startTime as sessionDateTime "
			+ "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b " + "ORDER BY bt.createdAt DESC")
	Page<BonusTransactionProjection> findAllProjectionsBy(Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "bt.pointsChange as pointsChangeRaw, "
			+ "bt.createdAt as createdAt, " + "bt.bonusCard.pointsBalance as newBalance, "
			+ "b.session.movie.title as movieTitle, " + "CAST(b.id as string) as bookingReference, "
			+ "b.session.hall.name as cinemaHall, " + "b.session.startTime as sessionDateTime "
			+ "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b " + "WHERE bt.type = :type "
			+ "ORDER BY bt.createdAt DESC")
	Page<BonusTransactionProjection> findProjectionsByType(@Param("type") BonusTransactionType type, Pageable pageable);
}