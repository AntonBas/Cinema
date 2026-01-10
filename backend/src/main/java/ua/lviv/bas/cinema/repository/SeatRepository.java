package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;

public interface SeatRepository extends JpaRepository<Seat, Long> {
	List<Seat> findByHallId(Long hallId);

	Optional<Seat> findByHallIdAndRowAndNumber(Long hallId, int row, int number);

	boolean existsByHallIdAndRowAndNumber(Long hallId, int row, int number);

	long countByHallId(Long hallId);

	List<Seat> findByHallIdAndSeatType(Long hallId, SeatType seatType);

	@Modifying
	@Query("DELETE FROM Seat s WHERE s.hall.id = :hallId")
	void deleteByHallId(@Param("hallId") Long hallId);

	boolean existsByHallIdAndRowAndNumberAndActiveTrue(Long hallId, int row, int number);

	List<Seat> findByHallIdAndActiveTrue(Long hallId);

	List<Seat> findByHallIdAndActiveFalse(Long hallId);

	@Query("SELECT DISTINCT s.row FROM Seat s WHERE s.hall.id = :hallId ORDER BY s.row")
	List<Integer> findDistinctRowsByHallId(@Param("hallId") Long hallId);

	@Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.id IN :seatIds")
	List<Seat> findByHallIdAndIdIn(@Param("hallId") Long hallId, @Param("seatIds") List<Long> seatIds);

	long countByHallIdAndActiveTrue(Long hallId);

	@Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.row = :row ORDER BY s.number")
	List<Seat> findByHallIdAndRow(@Param("hallId") Long hallId, @Param("row") Integer row);
}