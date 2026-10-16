package ua.lviv.bas.cinema.booking;

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
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.cinema.session.CinemaSessionStatus;
import ua.lviv.bas.cinema.payment.PaymentStatus;

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

    Optional<Booking> findByPublicIdAndUserId(UUID publicId, Long userId);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.seatReservations LEFT JOIN FETCH b.payment "
            + "WHERE b.status = :status AND b.expiresAt < :expiresAt "
            + "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.booking = b AND p.status IN :activePaymentStatuses)")
    List<Booking> findExpiredWithoutActivePayment(@Param("status") BookingStatus status,
            @Param("expiresAt") Instant expiresAt,
            @Param("activePaymentStatuses") List<PaymentStatus> activePaymentStatuses);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.seatReservations LEFT JOIN FETCH b.payment "
            + "JOIN FETCH b.session s WHERE b.status = :status AND s.status = :sessionStatus")
    List<Booking> findByStatusAndSessionStatus(@Param("status") BookingStatus status,
            @Param("sessionStatus") CinemaSessionStatus sessionStatus);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdDate < :cutoffDate "
            + "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.booking = b AND p.status IN :everPaidStatuses)")
    int deleteByStatusInAndCreatedDateBefore(@Param("statuses") List<BookingStatus> statuses,
                                             @Param("cutoffDate") Instant cutoffDate,
                                             @Param("everPaidStatuses") List<PaymentStatus> everPaidStatuses);

    @Query("SELECT b.id FROM Booking b WHERE b.status = :status AND b.ticketsEmailSentAt IS NULL "
            + "AND b.lastModifiedDate >= :from AND b.lastModifiedDate < :to ORDER BY b.lastModifiedDate")
    List<Long> findIdsAwaitingTicketsEmail(@Param("status") BookingStatus status, @Param("from") Instant from,
                                           @Param("to") Instant to);

    @Transactional
    @Modifying
    @Query("UPDATE Booking b SET b.ticketsEmailSentAt = :sentAt WHERE b.id = :id AND b.ticketsEmailSentAt IS NULL")
    int markTicketsEmailSent(@Param("id") Long id, @Param("sentAt") Instant sentAt);
}
