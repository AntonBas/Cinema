package ua.lviv.bas.cinema.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user JOIN FETCH b.session s JOIN FETCH s.movie WHERE p.id = :paymentId")
	Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

	@Query("SELECT p FROM Payment p WHERE " + "(:status IS NULL OR p.status = :status) AND "
			+ "(:dateFrom IS NULL OR DATE(p.createdAt) >= :dateFrom) AND "
			+ "(:dateTo IS NULL OR DATE(p.createdAt) <= :dateTo)")
	Page<Payment> findWithFilters(@Param("status") PaymentStatus status, @Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo, Pageable pageable);

	@Query("SELECT DATE(p.createdAt) as paymentDate, SUM(p.amount) as totalAmount " + "FROM Payment p "
			+ "WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate " + "AND p.status = 'SUCCESS' "
			+ "GROUP BY DATE(p.createdAt) " + "ORDER BY DATE(p.createdAt)")
	List<Object[]> findDailyPaymentStatistics(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p "
			+ "WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :start AND :end")
	BigDecimal sumSuccessfulAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.user.id = :userId ORDER BY p.createdAt DESC")
	List<Payment> findByUserId(@Param("userId") Long userId);
}