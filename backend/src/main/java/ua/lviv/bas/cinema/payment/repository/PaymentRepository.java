package ua.lviv.bas.cinema.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByLiqpayOrderId(String liqpayOrderId);

    @Query("SELECT DISTINCT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user "
            + "JOIN FETCH b.session s JOIN FETCH s.movie LEFT JOIN FETCH s.hall "
            + "LEFT JOIN FETCH b.seatReservations sr LEFT JOIN FETCH sr.seat WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

    List<Payment> findByStatusAndCreatedDateBefore(PaymentStatus status, LocalDateTime createdDate);

    List<Payment> findByStatusAndLastModifiedDateBefore(PaymentStatus status, LocalDateTime lastModifiedDate);

    @Query("SELECT DISTINCT p FROM Payment p JOIN FETCH p.booking b LEFT JOIN FETCH b.seatReservations JOIN FETCH b.session "
            + "WHERE p.status = :status AND p.createdDate < :createdDate")
    List<Payment> findByStatusAndCreatedDateBeforeWithBookingDetails(@Param("status") PaymentStatus status,
            @Param("createdDate") LocalDateTime createdDate);

    List<Payment> findByStatusInAndCreatedDateBefore(List<PaymentStatus> statuses, LocalDateTime createdDate);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus WHERE p.id = :id AND p.status IN :fromStatuses")
    int updateStatusIfCurrentIn(@Param("id") Long id, @Param("fromStatuses") List<PaymentStatus> fromStatuses,
                                @Param("newStatus") PaymentStatus newStatus);
}