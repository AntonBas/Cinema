package ua.lviv.bas.cinema.repository.cinema;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.cinema.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByHallId(Long hallId);

	long countByHallId(Long hallId);

	@Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.seatReservation.seat.hall.id = :hallId")
	boolean hasTicketsForHall(@Param("hallId") Long hallId);

	@Modifying
	@Query("DELETE FROM Seat s WHERE s.hall.id = :hallId")
	void deleteByHallId(@Param("hallId") Long hallId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Seat s WHERE s.id = :seatId")
	Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
}