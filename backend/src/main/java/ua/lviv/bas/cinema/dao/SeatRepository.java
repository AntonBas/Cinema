package ua.lviv.bas.cinema.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
	List<Seat> findByHallId(Long hallId);

	Optional<Seat> findByHallIdAndRowAndNumber(Long hallId, int row, int number);

	boolean existsByHallIdAndRowAndNumber(Long hallId, int row, int number);

	@Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.row = :row AND s.number = :number")
	Optional<Seat> findSeatByPosition(@Param("hallId") Long hallId, @Param("row") int row, @Param("number") int number);

	long countByHallId(Long hallId);

}
