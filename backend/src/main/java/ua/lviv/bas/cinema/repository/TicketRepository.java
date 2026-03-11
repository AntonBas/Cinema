package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	Optional<Ticket> findByUniqueCode(String uniqueCode);

	Optional<Ticket> findByIdAndUserIdAndStatus(Long ticketId, Long userId, TicketStatus status);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
	long countByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses")
	long countByTicketTypeIdAndStatusIn(@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);

	@Query("SELECT EXISTS(SELECT 1 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses)")
	boolean existsByTicketTypeIdAndStatusIn(@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);

	@Query("SELECT EXISTS(SELECT 1 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId)")
	boolean existsByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

	@Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.booking.session.startTime < :currentTime")
	List<Ticket> findActiveTicketsWithPastSessions(@Param("status") TicketStatus status,
			@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT t FROM Ticket t WHERE t.booking.session.startTime BETWEEN :fromTime AND :toTime AND t.status = :status")
	List<Ticket> findByBookingSessionStartTimeBetweenAndStatus(@Param("fromTime") LocalDateTime fromTime,
			@Param("toTime") LocalDateTime toTime, @Param("status") TicketStatus status);

	@Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.purchaseTime < :time")
	List<Ticket> findByStatusInAndPurchaseTimeBefore(@Param("statuses") List<TicketStatus> statuses,
			@Param("time") LocalDateTime time);

	long countByStatus(TicketStatus status);

	@Query("SELECT t.user.id, COUNT(t) FROM Ticket t WHERE t.user.id IN :userIds GROUP BY t.user.id")
	List<Object[]> countTicketsByUserIds(@Param("userIds") List<Long> userIds);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND CAST(t.purchaseTime AS date) = CURRENT_DATE")
	long countByStatusToday(@Param("status") TicketStatus status);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE CAST(t.purchaseTime AS date) = CURRENT_DATE")
	long countToday();

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses AND t.booking.session.startTime > :currentTime")
	long countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses, @Param("currentTime") LocalDateTime currentTime);
}