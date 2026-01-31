package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
	Optional<Ticket> findByUniqueCode(String uniqueCode);

	@Query("SELECT t.user.id, COUNT(t) FROM Ticket t WHERE t.user.id IN :userIds GROUP BY t.user.id")
	List<Object[]> countTicketsByUserIds(@Param("userIds") List<Long> userIds);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
	boolean existsByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
	long countByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses")
	long countByTicketTypeIdAndStatusIn(@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses")
	boolean existsByTicketTypeIdAndStatusIn(@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);

	Optional<Ticket> findByIdAndUserIdAndStatus(Long ticketId, Long userId, TicketStatus active);

	@Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.purchaseTime < :time")
	List<Ticket> findByStatusInAndPurchaseTimeBefore(@Param("statuses") List<TicketStatus> statuses,
			@Param("time") LocalDateTime time);

	@Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.booking.session.startTime < :currentTime")
	List<Ticket> findActiveTicketsWithPastSessions(@Param("status") TicketStatus status,
			@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT t FROM Ticket t WHERE t.booking.session.startTime BETWEEN :reminderTime AND :twoHoursBefore AND t.status = :status")
	List<Ticket> findByBookingSessionStartTimeBetweenAndStatus(@Param("reminderTime") LocalDateTime reminderTime,
			@Param("twoHoursBefore") LocalDateTime twoHoursBefore, @Param("status") TicketStatus status);

	@Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND (:search IS NULL OR :search = '' OR "
			+ "t.booking.session.movie.title LIKE %:search% OR t.booking.session.hall.name LIKE %:search% OR "
			+ "t.uniqueCode LIKE %:search% OR t.ticketType.displayName LIKE %:search%) "
			+ "ORDER BY t.purchaseTime DESC")
	Page<Ticket> findAllByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.status = 'ACTIVE' "
			+ "AND (:search IS NULL OR :search = '' OR "
			+ "t.booking.session.movie.title LIKE %:search% OR t.booking.session.hall.name LIKE %:search% OR "
			+ "t.uniqueCode LIKE %:search% OR t.ticketType.displayName LIKE %:search%) "
			+ "ORDER BY t.booking.session.startTime ASC, t.purchaseTime DESC")
	Page<Ticket> findActiveByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.status = 'USED' "
			+ "AND (:search IS NULL OR :search = '' OR "
			+ "t.booking.session.movie.title LIKE %:search% OR t.booking.session.hall.name LIKE %:search% OR "
			+ "t.uniqueCode LIKE %:search% OR t.ticketType.displayName LIKE %:search%) "
			+ "ORDER BY t.booking.session.startTime DESC, t.purchaseTime DESC")
	Page<Ticket> findUsedByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.status = 'REFUNDED' "
			+ "AND (:search IS NULL OR :search = '' OR "
			+ "t.booking.session.movie.title LIKE %:search% OR t.booking.session.hall.name LIKE %:search% OR "
			+ "t.uniqueCode LIKE %:search% OR t.ticketType.displayName LIKE %:search%) "
			+ "ORDER BY t.purchaseTime DESC")
	Page<Ticket> findRefundedByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.booking.session.startTime > :currentTime "
			+ "AND t.status = 'ACTIVE' AND (:search IS NULL OR :search = '' OR "
			+ "t.booking.session.movie.title LIKE %:search% OR t.booking.session.hall.name LIKE %:search% OR "
			+ "t.uniqueCode LIKE %:search% OR t.ticketType.displayName LIKE %:search%) "
			+ "ORDER BY t.booking.session.startTime ASC, t.purchaseTime DESC")
	Page<Ticket> findUpcomingByUserId(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime,
			@Param("search") String search, Pageable pageable);
}