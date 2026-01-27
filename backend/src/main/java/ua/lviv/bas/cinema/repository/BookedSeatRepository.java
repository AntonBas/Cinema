package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {
	@Query("SELECT COUNT(bs) > 0 FROM BookedSeat bs WHERE bs.session.id = :sessionId AND bs.seat.id = :seatId AND bs.status IN :statuses")
	boolean existsBySessionIdAndSeatIdAndStatusIn(@Param("sessionId") Long sessionId, @Param("seatId") Long seatId,
			@Param("statuses") List<BookedSeatStatus> statuses);

	List<BookedSeat> findBySessionId(Long sessionId);

	List<BookedSeat> findBySessionIdAndStatusIn(Long sessionId, List<BookedSeatStatus> statuses);

	@Query("SELECT COUNT(bs) FROM BookedSeat bs WHERE bs.session.id = :sessionId AND bs.status IN :statuses")
	long countBySessionIdAndStatusIn(@Param("sessionId") Long sessionId,
			@Param("statuses") List<BookedSeatStatus> statuses);

	@Query("SELECT bs FROM BookedSeat bs WHERE bs.booking.id = :bookingId")
	List<BookedSeat> findByBookingId(@Param("bookingId") Long bookingId);

	@Query("SELECT bs FROM BookedSeat bs WHERE bs.booking.user.id = :userId")
	List<BookedSeat> findByUserId(@Param("userId") Long userId);
}