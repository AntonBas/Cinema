package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

	Optional<Movie> findBySlug(String slug);

	Optional<MovieDetailProjection> findDetailProjectionById(Long id);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Query("SELECT m FROM Movie m WHERE m.id = :id")
	Optional<Movie> findAdminMovieById(@Param("id") Long id);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Query("SELECT m FROM Movie m WHERE m.slug = :slug")
	Optional<Movie> findAdminMovieBySlug(@Param("slug") String slug);

	@Query("""
			SELECT m.id as id, m.title as title, m.durationMinutes as durationMinutes
			FROM Movie m
			WHERE m.status IN ('CURRENT', 'UPCOMING')
			  AND m.endShowingDate >= CURRENT_DATE
			  AND (:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')))
			ORDER BY m.title
			""")
	List<MovieSessionSearchProjection> findMoviesForSession(@Param("search") String search);

	@Query("""
			SELECT m.id as id, m.title as title, m.durationMinutes as durationMinutes
			FROM Movie m
			WHERE m.status IN ('CURRENT', 'UPCOMING')
			  AND m.endShowingDate >= CURRENT_DATE
			  AND :date BETWEEN m.releaseDate AND m.endShowingDate
			ORDER BY m.title
			""")
	List<MovieSessionSearchProjection> findMoviesByDate(@Param("date") java.time.LocalDate date);

	@Query("SELECT m.posterFileName FROM Movie m WHERE m.id = :id")
	Optional<String> findPosterFileNameById(@Param("id") Long id);

	@Query("SELECT COUNT(m) FROM Movie m WHERE " + "EXISTS (SELECT 1 FROM m.actors a WHERE a.id = :personId) OR "
			+ "EXISTS (SELECT 1 FROM m.directors d WHERE d.id = :personId) OR "
			+ "EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = :personId)")
	long countMovieUsageByPersonId(@Param("personId") Long personId);
}