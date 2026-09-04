package ua.lviv.bas.cinema.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.seatReservations "
            + "WHERE b.status = :status AND b.expiresAt < :expiresAt")
    List<Booking> findByStatusAndExpiresAtBefore(@Param("status") BookingStatus status,
                                                  @Param("expiresAt") LocalDateTime expiresAt);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdDate < :cutoffDate "
            + "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.booking = b AND p.status IN :everPaidStatuses)")
    int deleteByStatusInAndCreatedDateBefore(@Param("statuses") List<BookingStatus> statuses,
                                             @Param("cutoffDate") LocalDateTime cutoffDate,
                                             @Param("everPaidStatuses") List<PaymentStatus> everPaidStatuses);
}