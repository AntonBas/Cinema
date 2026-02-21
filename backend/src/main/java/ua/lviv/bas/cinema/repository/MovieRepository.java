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
			WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')))
			  AND m.status IN ('CURRENT', 'UPCOMING')
			  AND m.endShowingDate >= CURRENT_DATE
			ORDER BY m.title
			""")
	List<MovieSessionSearchProjection> findMoviesForSession(@Param("title") String title);

	@Query("SELECT m.posterFileName FROM Movie m WHERE m.id = :id")
	Optional<String> findPosterFileNameById(@Param("id") Long id);
}