package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

	@Query("SELECT COUNT(sr) > 0 FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId AND sr.status IN :statuses")
	boolean existsBySessionIdAndSeatIdAndStatusIn(@Param("sessionId") Long sessionId, @Param("seatId") Long seatId,
			@Param("statuses") List<ReservationStatus> statuses);

	@Query("SELECT sr.status FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.seat.id = :seatId")
	List<ReservationStatus> findStatusesBySessionIdAndSeatId(@Param("sessionId") Long sessionId,
			@Param("seatId") Long seatId);

	List<SeatReservation> findBySessionId(Long sessionId);

	List<SeatReservation> findBySessionIdAndStatusIn(Long sessionId, List<ReservationStatus> statuses);

	@Query("SELECT COUNT(sr) FROM SeatReservation sr WHERE sr.session.id = :sessionId AND sr.status IN :statuses")
	long countBySessionIdAndStatusIn(@Param("sessionId") Long sessionId,
			@Param("statuses") List<ReservationStatus> statuses);

	@Query("SELECT sr FROM SeatReservation sr WHERE sr.booking.id = :bookingId")
	List<SeatReservation> findByBookingId(@Param("bookingId") Long bookingId);

	@Query("SELECT sr FROM SeatReservation sr WHERE sr.booking.user.id = :userId")
	List<SeatReservation> findByUserId(@Param("userId") Long userId);

	@Query("""
			SELECT
			    s.id,
			    CASE WHEN sr.id IS NOT NULL AND sr.status IN :statuses THEN true ELSE false END
			FROM Seat s
			LEFT JOIN SeatReservation sr ON sr.seat.id = s.id AND sr.session.id = :sessionId
			WHERE s.hall.id = :hallId
			""")
	List<Object[]> findBookedSeatIds(@Param("hallId") Long hallId, @Param("sessionId") Long sessionId,
			@Param("statuses") List<ReservationStatus> statuses);

	@Query("""
			SELECT s.id
			FROM SeatReservation sr
			JOIN sr.seat s
			WHERE sr.session.id = :sessionId AND sr.status = 'PENDING'
			""")
	List<Long> findPendingSeatIdsBySessionId(@Param("sessionId") Long sessionId);
}