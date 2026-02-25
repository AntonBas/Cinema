package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;
import ua.lviv.bas.cinema.domain.projection.MovieDetailProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findBySlug(String slug);

	Optional<MovieDetailProjection> findDetailProjectionById(Long id);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Query("SELECT m FROM Movie m WHERE m.id = :id")
	Optional<Movie> findAdminMovieById(@Param("id") Long id);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Query("SELECT m FROM Movie m WHERE m.slug = :slug")
	Optional<Movie> findAdminMovieBySlug(@Param("slug") String slug);

	@Query("""
			SELECT m
			FROM Movie m
			WHERE (:title IS NULL OR LOWER(CAST(m.title AS string)) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
			  AND (:status IS NULL OR m.status = :status)
			ORDER BY
			    CASE
			        WHEN :status = 'CURRENT' THEN m.releaseDate END DESC,
			        CASE WHEN :status = 'UPCOMING' THEN m.releaseDate END ASC,
			        m.title ASC
			""")
	Page<Movie> findMoviesByFilters(@Param("title") String title, @Param("status") MovieStatus status,
			Pageable pageable);

	@Query("""
			SELECT m.id as id,
			       m.slug as slug,
			       m.title as title,
			       m.posterFileName as posterFileName,
			       m.durationMinutes as durationMinutes,
			       m.ageRating as ageRating,
			       m.status as status,
			       m.releaseDate as releaseDate,
			       m.endShowingDate as endShowingDate
			FROM Movie m
			WHERE m.status IN ('CURRENT', 'UPCOMING')
			  AND m.endShowingDate >= CURRENT_DATE
			  AND (:search IS NULL OR LOWER(CAST(m.title AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
			ORDER BY m.title
			""")
	List<MovieCardProjection> findMoviesForSession(@Param("search") String search);

	@Query("""
			SELECT m.id as id,
			       m.slug as slug,
			       m.title as title,
			       m.posterFileName as posterFileName,
			       m.durationMinutes as durationMinutes,
			       m.ageRating as ageRating,
			       m.status as status,
			       m.releaseDate as releaseDate,
			       m.endShowingDate as endShowingDate
			FROM Movie m
			WHERE m.status IN ('CURRENT', 'UPCOMING')
			  AND m.endShowingDate >= CURRENT_DATE
			  AND :date BETWEEN m.releaseDate AND m.endShowingDate
			ORDER BY m.title
			""")
	List<MovieCardProjection> findMoviesByDate(@Param("date") java.time.LocalDate date);

	@Query("SELECT m.posterFileName FROM Movie m WHERE m.id = :id")
	Optional<String> findPosterFileNameById(@Param("id") Long id);

	@Query("SELECT COUNT(m) FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE a.id = :personId) OR EXISTS (SELECT 1 FROM m.directors d WHERE d.id = :personId) OR EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = :personId)")
	long countMovieUsageByPersonId(@Param("personId") Long personId);
}