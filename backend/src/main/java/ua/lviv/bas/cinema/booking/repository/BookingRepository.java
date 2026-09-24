package ua.lviv.bas.cinema.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @EntityGraph(attributePaths = {"user", "session", "session.movie", "session.hall", "payment"})
    @Override
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "session", "session.movie", "session.hall", "payment",
            "seatReservations", "seatReservations.seat", "seatReservations.ticketType"})
    Optional<Booking> findWithDetailsById(Long id);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Optional<Booking> findByPublicIdAndUserId(UUID publicId, Long userId);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.seatReservations "
            + "WHERE b.status = :status AND b.expiresAt < :expiresAt "
            + "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.booking = b AND p.status IN :activePaymentStatuses)")
    List<Booking> findExpiredWithoutActivePayment(@Param("status") BookingStatus status,
            @Param("expiresAt") Instant expiresAt,
            @Param("activePaymentStatuses") List<PaymentStatus> activePaymentStatuses);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.seatReservations JOIN FETCH b.session s "
            + "WHERE b.status = :status AND s.status = :sessionStatus")
    List<Booking> findByStatusAndSessionStatus(@Param("status") BookingStatus status,
            @Param("sessionStatus") CinemaSessionStatus sessionStatus);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdDate < :cutoffDate "
            + "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.booking = b AND p.status IN :everPaidStatuses)")
    int deleteByStatusInAndCreatedDateBefore(@Param("statuses") List<BookingStatus> statuses,
                                             @Param("cutoffDate") Instant cutoffDate,
                                             @Param("everPaidStatuses") List<PaymentStatus> everPaidStatuses);
}