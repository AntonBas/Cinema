package ua.lviv.bas.cinema.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findBySlug(String slug);

	List<Movie> findByStatus(MovieStatus status);

	Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

	List<Movie> findByReleaseDateBeforeAndEndShowingDateAfter(LocalDate date1, LocalDate date2);

	List<Movie> findByReleaseDateAfter(LocalDate date);

	@Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId")
	List<Movie> findByGenresContaining(@Param("genreId") Long genreId);
}