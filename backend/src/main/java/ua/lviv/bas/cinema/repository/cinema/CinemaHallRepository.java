package ua.lviv.bas.cinema.repository.cinema;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;

@Repository
public interface CinemaHallRepository extends JpaRepository<CinemaHall, Long> {

	boolean existsByName(String name);

	@Query("""
			SELECT
			    ch.id as id,
			    ch.name as name,
			    COUNT(s.id) as seatsCount
			FROM CinemaHall ch
			LEFT JOIN ch.seats s
			GROUP BY ch.id, ch.name
			ORDER BY ch.name ASC
			""")
	List<CinemaHallListProjection> findAllProjected();

	@Query("SELECT ch FROM CinemaHall ch LEFT JOIN FETCH ch.seats WHERE ch.id = :id")
	Optional<CinemaHall> findByIdWithSeats(@Param("id") Long id);
}