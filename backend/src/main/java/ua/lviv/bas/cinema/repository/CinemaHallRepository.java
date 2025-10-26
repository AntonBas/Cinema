package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.CinemaHall;

public interface CinemaHallRepository extends JpaRepository<CinemaHall, Long> {

	Optional<CinemaHall> findByIdWithSeats(@Param("id") Long id);

	List<CinemaHall> findAllWithSeats();

	boolean existsByName(String name);

	List<CinemaHall> findByNameContainingIgnoreCase(String name);
}
