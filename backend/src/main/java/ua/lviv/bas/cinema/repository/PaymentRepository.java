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

import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByBookingId(Long bookingId);

	@Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.booking.user.id = :userId")
	Optional<Payment> findByBookingIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

	Optional<Payment> findByLiqpayOrderId(String liqpayOrderId);

	@Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user JOIN FETCH b.session s JOIN FETCH s.movie WHERE p.id = :paymentId")
	Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

	@Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.user.id = :userId ORDER BY p.createdAt DESC")
	Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);

	Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

	List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);

	List<Payment> findByStatusInAndCreatedAtBefore(List<PaymentStatus> statuses, LocalDateTime createdAt);

	List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	long countByStatus(PaymentStatus status);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}