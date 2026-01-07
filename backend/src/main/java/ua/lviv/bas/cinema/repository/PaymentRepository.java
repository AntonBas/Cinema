package ua.lviv.bas.cinema.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByBookingId(Long bookingId);

	Optional<Payment> findByLiqpayOrderId(String liqpayOrderId);

	List<Payment> findByStatus(PaymentStatus status);

	List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);

	List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	List<Payment> findByStatusInAndCreatedAtBefore(List<PaymentStatus> statuses, LocalDateTime createdAt);

	long countByStatus(PaymentStatus status);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT p FROM Payment p " + "LEFT JOIN FETCH p.booking b " + "LEFT JOIN FETCH b.session s "
			+ "LEFT JOIN FETCH s.movie m " + "LEFT JOIN FETCH b.user u " + "WHERE p.id = :id")
	Optional<Payment> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p "
			+ "WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :start AND :end")
	BigDecimal sumSuccessfulAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.user.id = :userId ORDER BY p.createdAt DESC")
	List<Payment> findByUserId(@Param("userId") Long userId);
}