package ua.lviv.bas.cinema.repository.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByBookingId(Long bookingId);

	@Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.booking.user.id = :userId")
	Optional<Payment> findByBookingIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

	Optional<Payment> findByLiqpayOrderId(String liqpayOrderId);

	@Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user JOIN FETCH b.session s JOIN FETCH s.movie WHERE p.id = :paymentId")
	Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

	List<Payment> findByStatusAndCreatedDateBefore(PaymentStatus status, LocalDateTime createdDate);

	List<Payment> findByStatusInAndCreatedDateBefore(List<PaymentStatus> statuses, LocalDateTime createdDate);

	long countByStatus(PaymentStatus status);

	long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId")
	List<Payment> findByUserId(@Param("userId") Long userId);
}