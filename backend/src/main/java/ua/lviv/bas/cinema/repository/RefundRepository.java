package ua.lviv.bas.cinema.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.enums.RefundStatus;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

	List<Refund> findByPaymentId(Long paymentId);

	@Query("SELECT r FROM Refund r JOIN r.items ri WHERE ri.ticket.id = :ticketId")
	Optional<Refund> findByTicketId(@Param("ticketId") Long ticketId);

	List<Refund> findByStatus(RefundStatus status);

	List<Refund> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT DISTINCT r FROM Refund r " + "JOIN r.items ri " + "JOIN ri.ticket t " + "WHERE t.user.id = :userId "
			+ "ORDER BY r.createdAt DESC")
	List<Refund> findByUserId(@Param("userId") Long userId);

	long countByStatus(RefundStatus status);

	@Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Refund r WHERE r.status = 'COMPLETED' AND r.createdAt BETWEEN :start AND :end")
	BigDecimal sumCompletedAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}