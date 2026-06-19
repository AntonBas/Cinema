package ua.lviv.bas.cinema.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.dto.booking.response.SeatStatusResponse;

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

    @Query("""
            SELECT new ua.lviv.bas.cinema.dto.booking.response.SeatStatusResponse(s.id, sr.status)
            FROM Seat s
            LEFT JOIN SeatReservation sr ON sr.seat.id = s.id AND sr.session.id = :sessionId AND sr.status IN :statuses
            WHERE s.hall.id = :hallId
            """)
    List<SeatStatusResponse> findBookedSeatStatuses(@Param("hallId") Long hallId,
                                                    @Param("sessionId") Long sessionId,
                                                    @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT s.id, (SELECT COUNT(seat) FROM Seat seat WHERE seat.hall.id = s.hall.id) " +
            "FROM Session s WHERE s.id IN :sessionIds")
    List<Object[]> findTotalSeatsBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT sr.session.id, COUNT(sr) FROM SeatReservation sr " +
            "WHERE sr.session.id IN :sessionIds AND sr.status IN :statuses " +
            "GROUP BY sr.session.id")
    List<Object[]> findBookedCountBySessionIds(@Param("sessionIds") List<Long> sessionIds,
                                               @Param("statuses") List<ReservationStatus> statuses);
}