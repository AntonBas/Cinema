package ua.lviv.bas.cinema.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Query("SELECT COUNT(sr) > 0 FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId AND sr.status IN :statuses")
    boolean existsBySessionIdAndSeatIdAndStatusIn(@Param("sessionId") Long sessionId, @Param("seatId") Long seatId,
                                                  @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT sr.status FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId")
    List<ReservationStatus> findStatusesBySessionIdAndSeatId(@Param("sessionId") Long sessionId,
                                                             @Param("seatId") Long seatId);

    List<SeatReservation> findByStatus(ReservationStatus status);

    List<SeatReservation> findByStatusAndReservedUntilBefore(ReservationStatus status, LocalDateTime reservedUntil);

    Optional<SeatReservation> findBySessionIdAndSeatIdAndStatusAndReservedByUserId(Long sessionId, Long seatId,
                                                                                   ReservationStatus status, Long userId);

    @Query("SELECT COUNT(sr) FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.status IN :statuses")
    long countBySessionIdAndStatusIn(@Param("sessionId") Long sessionId,
                                     @Param("statuses") List<ReservationStatus> statuses);

    @Query("""
            SELECT s.id, sr.status
            FROM Seat s
            LEFT JOIN SeatReservation sr ON sr.seat.id = s.id AND sr.session.id = :sessionId AND sr.status IN :statuses
            WHERE s.hall.id = :hallId
            """)
    List<Object[]> findBookedSeatIds(@Param("hallId") Long hallId, @Param("sessionId") Long sessionId,
                                     @Param("statuses") List<ReservationStatus> statuses);

    @Query(value = """
            SELECT
                sr.session_id as sessionId,
                (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = s.hall_id) - COUNT(sr.id) as availableSeats
            FROM seat_reservations sr
            JOIN sessions s ON s.id = sr.session_id
            WHERE sr.session_id IN (:sessionIds)
            AND sr.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
            GROUP BY sr.session_id, s.hall_id
            """, nativeQuery = true)
    List<Object[]> findAvailableSeatsBatch(@Param("sessionIds") List<Long> sessionIds);
}