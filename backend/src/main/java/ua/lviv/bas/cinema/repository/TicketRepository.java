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

	long countByUserId(@Param("userId") Long userId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId AND t.status = :status")
	long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketStatus status);

	@Query("SELECT t.user.id, COUNT(t) FROM Ticket t WHERE t.user.id IN :userIds GROUP BY t.user.id")
	List<Object[]> countTicketsByUserIds(@Param("userIds") List<Long> userIds);

	@Query("SELECT t FROM Ticket t WHERE t.booking.id = :bookingId")
	List<Ticket> findByBookingId(@Param("bookingId") Long bookingId);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId ORDER BY t.purchaseTime DESC")
	List<Ticket> findByUserIdOrderByPurchaseTimeDesc(@Param("userId") Long userId);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.status = :status ORDER BY t.purchaseTime DESC")
	List<Ticket> findByUserIdAndStatusOrderByPurchaseTimeDesc(@Param("userId") Long userId,
			@Param("status") TicketStatus status);

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

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId ORDER BY t.purchaseTime DESC")
	Page<Ticket> findByUserIdOrderByPurchaseTimeDesc(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.status = :status ORDER BY t.purchaseTime DESC")
	Page<Ticket> findByUserIdAndStatusOrderByPurchaseTimeDesc(@Param("userId") Long userId,
			@Param("status") TicketStatus status, Pageable pageable);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.booking.session.startTime > :currentTime ORDER BY t.booking.session.startTime ASC")
	List<Ticket> findUpcomingTickets(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.booking.session.startTime <= :currentTime ORDER BY t.booking.session.startTime DESC")
	List<Ticket> findPastTickets(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.booking.user.id = :userId AND t.booking.session.id = :sessionId AND t.status = :status")
	boolean existsByUserIdAndBookingSessionIdAndStatus(@Param("userId") Long userId, @Param("sessionId") Long sessionId,
			@Param("status") TicketStatus status);
}