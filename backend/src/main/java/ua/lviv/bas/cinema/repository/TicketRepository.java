package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Optional<Ticket> findByUniqueCode(String uniqueCode);

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
}