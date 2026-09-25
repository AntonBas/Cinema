package ua.lviv.bas.cinema.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

import java.time.Instant;
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


    List<Payment> findByStatusAndLastModifiedDateBefore(PaymentStatus status, Instant lastModifiedDate);

    @Query("SELECT DISTINCT p FROM Payment p JOIN FETCH p.booking b LEFT JOIN FETCH b.seatReservations JOIN FETCH b.session "
            + "WHERE p.status IN :statuses AND b.status = :bookingStatus AND b.expiresAt < :expiresAt")
    List<Payment> findByStatusInAndBookingExpiredBefore(@Param("statuses") List<PaymentStatus> statuses,
            @Param("bookingStatus") BookingStatus bookingStatus, @Param("expiresAt") Instant expiresAt);

    List<Payment> findByStatusInAndCreatedDateBefore(List<PaymentStatus> statuses, Instant createdDate);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.booking.status = :bookingStatus "
            + "AND p.lastModifiedDate < :cutoff")
    List<Payment> findByStatusAndBookingStatusAndLastModifiedDateBefore(@Param("status") PaymentStatus status,
            @Param("bookingStatus") BookingStatus bookingStatus, @Param("cutoff") Instant cutoff);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.booking.status IN :bookingStatuses "
            + "AND p.lastModifiedDate < :cutoff AND NOT EXISTS (SELECT 1 FROM Ticket t WHERE t.payment = p)")
    List<Payment> findWithoutTicketsByStatusAndBookingStatusIn(@Param("status") PaymentStatus status,
            @Param("bookingStatuses") List<BookingStatus> bookingStatuses, @Param("cutoff") Instant cutoff);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = :newStatus, p.version = p.version + 1 "
            + "WHERE p.id = :id AND p.status IN :fromStatuses")
    int updateStatusIfCurrentIn(@Param("id") Long id, @Param("fromStatuses") List<PaymentStatus> fromStatuses,
                                @Param("newStatus") PaymentStatus newStatus);
}