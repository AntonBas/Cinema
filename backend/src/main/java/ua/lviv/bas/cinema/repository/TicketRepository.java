package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
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

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId ORDER BY t.purchaseTime DESC")
	List<Ticket> findByUserIdOrderByPurchaseTimeDesc(@Param("userId") Long userId);

	@Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.status = :status ORDER BY t.purchaseTime DESC")
	List<Ticket> findByUserIdAndStatusOrderByPurchaseTimeDesc(@Param("userId") Long userId, @Param("status") TicketStatus status);

	@Query("SELECT t FROM Ticket t WHERE t.booking.session.id = :sessionId")
	List<Ticket> findByBookedSeatSessionId(@Param("sessionId") Long sessionId);

	@Query("SELECT t FROM Ticket t WHERE t.booking.session.id = :sessionId AND t.status = :status")
	List<Ticket> findByBookedSeatSessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") TicketStatus status);

	List<Ticket> findByPurchaseTimeBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t JOIN t.booking b JOIN b.bookedSeats bs WHERE bs.seat.id = :seatId AND b.session.id = :sessionId AND t.status IN :statuses")
	boolean existsByBookedSeatSeatIdAndBookedSeatSessionIdAndStatusIn(
			@Param("seatId") Long seatId, 
			@Param("sessionId") Long sessionId,
			@Param("statuses") List<TicketStatus> statuses);

	@Query("SELECT COUNT(t) FROM Ticket t JOIN t.booking b WHERE b.session.id = :sessionId AND t.status IN :statuses")
	long countByBookedSeatSessionIdAndStatusIn(
			@Param("sessionId") Long sessionId, 
			@Param("statuses") List<TicketStatus> statuses);

	@Query("SELECT t FROM Ticket t " +
			"LEFT JOIN FETCH t.booking b " +
			"LEFT JOIN FETCH b.session s " +
			"LEFT JOIN FETCH s.movie " +
			"LEFT JOIN FETCH s.hall " +
			"LEFT JOIN FETCH b.user " +
			"LEFT JOIN FETCH t.ticketType " +
			"WHERE t.id = :id")
	Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT t FROM Ticket t " +
			"LEFT JOIN FETCH t.booking b " +
			"LEFT JOIN FETCH b.session s " +
			"LEFT JOIN FETCH s.movie " +
			"LEFT JOIN FETCH s.hall " +
			"LEFT JOIN FETCH t.ticketType " +
			"WHERE b.user.id = :userId")
	List<Ticket> findByUserIdWithDetails(@Param("userId") Long userId);

	@Query("SELECT t FROM Ticket t WHERE t.payment.booking.id = :bookingId")
	List<Ticket> findByPaymentBookingId(@Param("bookingId") Long bookingId);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
	boolean existsByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses")
	boolean existsByTicketTypeIdAndStatusIn(
			@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);

	long countByTicketTypeId(Long ticketTypeId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses")
	long countByTicketTypeIdAndStatusIn(
			@Param("ticketTypeId") Long ticketTypeId,
			@Param("statuses") List<TicketStatus> statuses);
}