package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Repository
public interface SeatAvailableRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByHallId(Long hallId);

	List<Seat> findByHallIdAndActiveTrue(Long hallId);

	List<Seat> findByHallIdAndSeatType(Long hallId, SeatType seatType);

	long countByHallId(Long hallId);

	long countByHallIdAndActiveTrue(Long hallId);

	@Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.row = :row ORDER BY s.number")
	List<Seat> findByHallIdAndRow(@Param("hallId") Long hallId, @Param("row") Integer row);

	@Query("SELECT DISTINCT s.row FROM Seat s WHERE s.hall.id = :hallId ORDER BY s.row")
	List<Integer> findDistinctRowsByHallId(@Param("hallId") Long hallId);

	@Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.id IN :seatIds")
	List<Seat> findByHallIdAndIdIn(@Param("hallId") Long hallId, @Param("seatIds") List<Long> seatIds);
}