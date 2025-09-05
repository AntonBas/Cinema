package ua.lviv.bas.cinema.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
	List<Seat> findByHall(CinemaHall hall);

	@Modifying
	@Query("DELETE FROM Seat s WHERE s.hall.id = :hallId")
	void deleteAllByHallId(Long hallId);
}
