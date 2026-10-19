package ua.lviv.bas.cinema.booking.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.booking.reservation.dto.SeatStatusResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Query("SELECT COUNT(sr) > 0 FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId AND sr.status IN :statuses "
            + "AND (sr.status <> ua.lviv.bas.cinema.booking.reservation.ReservationStatus.PENDING OR sr.reservedUntil > CURRENT_TIMESTAMP)")
    boolean existsBySessionIdAndSeatIdAndStatusIn(@Param("sessionId") Long sessionId, @Param("seatId") Long seatId,
                                                  @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT sr FROM SeatReservation sr WHERE sr.status = :status "
            + "AND NOT EXISTS (SELECT 1 FROM Ticket t WHERE t.seatReservation = sr)")
    List<SeatReservation> findByStatusWithoutTickets(@Param("status") ReservationStatus status);

    List<SeatReservation> findByStatusAndReservedUntilBefore(ReservationStatus status, Instant reservedUntil);

    @Modifying
    @Query("DELETE FROM SeatReservation sr WHERE sr.id = :id AND sr.status = :status AND sr.reservedUntil < :cutoff")
    int deleteByIdIfStillExpired(@Param("id") Long id, @Param("status") ReservationStatus status,
                                 @Param("cutoff") Instant cutoff);

    @Modifying
    @Query("DELETE FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId "
            + "AND sr.status = ua.lviv.bas.cinema.booking.reservation.ReservationStatus.PENDING "
            + "AND sr.reservedUntil < CURRENT_TIMESTAMP")
    int deleteExpiredHolds(@Param("sessionId") Long sessionId, @Param("seatId") Long seatId);

    Optional<SeatReservation> findBySessionIdAndSeatIdAndStatusAndReservedByUserId(Long sessionId, Long seatId,
                                                                                   ReservationStatus status, Long userId);

    List<SeatReservation> findBySessionIdAndSeatId(Long sessionId, Long seatId);

    @Query("""
            SELECT new ua.lviv.bas.cinema.booking.reservation.dto.SeatStatusResponse(s.id, sr.status)
            FROM Seat s
            LEFT JOIN SeatReservation sr ON sr.seat.id = s.id AND sr.session.id = :sessionId AND sr.status IN :statuses
                AND (sr.status <> ua.lviv.bas.cinema.booking.reservation.ReservationStatus.PENDING OR sr.reservedUntil > CURRENT_TIMESTAMP)
            WHERE s.hall.id = :hallId
            """)
    List<SeatStatusResponse> findBookedSeatStatuses(@Param("hallId") Long hallId,
                                                    @Param("sessionId") Long sessionId,
                                                    @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT s.id, COUNT(seat) FROM Session s LEFT JOIN s.hall.seats seat " +
            "WHERE s.id IN :sessionIds GROUP BY s.id")
    List<Object[]> findTotalSeatsBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT sr.session.id, COUNT(sr) FROM SeatReservation sr " +
            "WHERE sr.session.id IN :sessionIds AND sr.status IN :statuses " +
            "AND (sr.status <> ua.lviv.bas.cinema.booking.reservation.ReservationStatus.PENDING OR sr.reservedUntil > CURRENT_TIMESTAMP) " +
            "GROUP BY sr.session.id")
    List<Object[]> findBookedCountBySessionIds(@Param("sessionIds") List<Long> sessionIds,
                                               @Param("statuses") List<ReservationStatus> statuses);

    boolean existsByTicketTypeId(Long ticketTypeId);
}
