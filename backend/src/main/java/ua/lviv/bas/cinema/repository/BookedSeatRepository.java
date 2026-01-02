package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

	boolean existsBySessionIdAndSeatIdAndStatusIn(Long sessionId, Long seatId, List<BookedSeatStatus> statuses);

	List<BookedSeat> findBySessionId(Long sessionId);

	List<BookedSeat> findBySessionIdAndStatusIn(Long sessionId, List<BookedSeatStatus> statuses);

	List<BookedSeat> findByBookingId(Long bookingId);

	Optional<BookedSeat> findBySessionIdAndSeatId(Long sessionId, Long seatId);

	long countBySessionIdAndStatusIn(Long sessionId, List<BookedSeatStatus> statuses);

	@Query("SELECT bs FROM BookedSeat bs " + "JOIN bs.booking b "
			+ "WHERE b.user.id = :userId AND bs.status IN (:statuses)")
	List<BookedSeat> findByUserIdAndStatusIn(@Param("userId") Long userId,
			@Param("statuses") List<BookedSeatStatus> statuses);

	@Query("SELECT bs FROM BookedSeat bs " + "LEFT JOIN FETCH bs.seat " + "LEFT JOIN FETCH bs.ticketType "
			+ "LEFT JOIN FETCH bs.session s " + "LEFT JOIN FETCH s.movie " + "WHERE bs.booking.id = :bookingId")
	List<BookedSeat> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);
}