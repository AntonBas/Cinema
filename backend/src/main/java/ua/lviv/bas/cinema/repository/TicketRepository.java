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

	List<Ticket> findByUserIdOrderByPurchaseTimeDesc(Long userId);

	List<Ticket> findByUserIdAndStatusOrderByPurchaseTimeDesc(Long userId, TicketStatus status);

	List<Ticket> findByBookedSeatSessionId(Long sessionId);

	List<Ticket> findByBookedSeatSessionIdAndStatus(Long sessionId, TicketStatus status);

	List<Ticket> findByPurchaseTimeBetween(LocalDateTime start, LocalDateTime end);

	boolean existsByBookedSeatSeatIdAndBookedSeatSessionIdAndStatusIn(Long seatId, Long sessionId,
			List<TicketStatus> statuses);

	long countByBookedSeatSessionIdAndStatusIn(Long sessionId, List<TicketStatus> statuses);

	@Query("SELECT t FROM Ticket t " + "LEFT JOIN FETCH t.bookedSeat bs " + "LEFT JOIN FETCH bs.seat "
			+ "LEFT JOIN FETCH bs.session s " + "LEFT JOIN FETCH s.movie " + "LEFT JOIN FETCH s.hall "
			+ "WHERE t.id = :id")
	Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT t FROM Ticket t " + "LEFT JOIN FETCH t.bookedSeat bs " + "LEFT JOIN FETCH bs.seat "
			+ "LEFT JOIN FETCH bs.session s " + "LEFT JOIN FETCH s.movie " + "LEFT JOIN FETCH s.hall "
			+ "WHERE t.user.id = :userId")
	List<Ticket> findByUserIdWithDetails(@Param("userId") Long userId);
}