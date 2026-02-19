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

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.bonusCard = :bonusCard")
	Page<BonusTransaction> findByBonusCard(@Param("bonusCard") BonusCard bonusCard, Pageable pageable);

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.bonusCard.user.id = :userId")
	Page<BonusTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT bt FROM BonusTransaction bt WHERE bt.type = :type")
	Page<BonusTransaction> findByType(@Param("type") BonusTransactionType type, Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "CAST(bt.type as string) as typeDisplay, "
			+ "bt.pointsChange as pointsChangeRaw, " + "bt.createdAt as createdAt, "
			+ "bt.bonusCard.pointsBalance as newBalance, " + "s.movie.title as movieTitle, "
			+ "CAST(b.id as string) as bookingReference, " + "h.name as cinemaHall, "
			+ "s.startTime as sessionDateTime " + "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b "
			+ "LEFT JOIN b.session s " + "LEFT JOIN s.hall h " + "LEFT JOIN s.movie m "
			+ "WHERE bt.bonusCard.user.id = :userId")
	Page<BonusTransactionProjection> findProjectionsByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "CAST(bt.type as string) as typeDisplay, "
			+ "bt.pointsChange as pointsChangeRaw, " + "bt.createdAt as createdAt, "
			+ "bt.bonusCard.pointsBalance as newBalance, " + "s.movie.title as movieTitle, "
			+ "CAST(b.id as string) as bookingReference, " + "h.name as cinemaHall, "
			+ "s.startTime as sessionDateTime " + "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b "
			+ "LEFT JOIN b.session s " + "LEFT JOIN s.hall h " + "LEFT JOIN s.movie m")
	Page<BonusTransactionProjection> findAllProjectionsBy(Pageable pageable);

	@Query("SELECT " + "bt.id as id, " + "bt.type as type, " + "CAST(bt.type as string) as typeDisplay, "
			+ "bt.pointsChange as pointsChangeRaw, " + "bt.createdAt as createdAt, "
			+ "bt.bonusCard.pointsBalance as newBalance, " + "s.movie.title as movieTitle, "
			+ "CAST(b.id as string) as bookingReference, " + "h.name as cinemaHall, "
			+ "s.startTime as sessionDateTime " + "FROM BonusTransaction bt " + "LEFT JOIN bt.booking b "
			+ "LEFT JOIN b.session s " + "LEFT JOIN s.hall h " + "LEFT JOIN s.movie m " + "WHERE bt.type = :type")
	Page<BonusTransactionProjection> findProjectionsByType(@Param("type") BonusTransactionType type, Pageable pageable);
}