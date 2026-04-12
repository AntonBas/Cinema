package ua.lviv.bas.cinema.repository.cinema;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findBySlug(String slug);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	Optional<Movie> findMovieById(Long id);

	@Query("""
			SELECT m
			FROM Movie m
			WHERE (:query IS NULL OR LOWER(CAST(m.title AS string)) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
			  AND (:status IS NULL OR m.status = :status)
			""")
	Page<Movie> findMoviesByQueryAndStatus(@Param("query") String query, @Param("status") MovieStatus status,
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
			  AND (:query IS NULL OR LOWER(CAST(m.title AS string)) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
			ORDER BY m.title
			""")
	List<MovieCardProjection> findMoviesForSessionSearch(@Param("query") String query);

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
			WHERE m.status = 'CURRENT'
			  AND m.releaseDate <= CURRENT_DATE
			  AND m.endShowingDate >= CURRENT_DATE
			""")
	Page<MovieCardProjection> findCurrentMovies(Pageable pageable);

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
			WHERE m.status = 'UPCOMING'
			  AND m.releaseDate > CURRENT_DATE
			""")
	Page<MovieCardProjection> findUpcomingMovies(Pageable pageable);

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
			WHERE m.status = 'CURRENT'
			  AND m.endShowingDate BETWEEN CURRENT_DATE AND CURRENT_DATE + 7 DAY
			""")
	Page<MovieCardProjection> findLeavingSoonMovies(Pageable pageable);

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
			  AND LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))
			ORDER BY m.title
			""")
	List<MovieCardProjection> findMoviesByDateAndTitle(@Param("date") LocalDate date, @Param("title") String title);

	@Query("SELECT m.posterFileName FROM Movie m WHERE m.id = :id")
	Optional<String> findPosterFileNameById(@Param("id") Long id);

	@Query("SELECT COUNT(m) FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE a.id = :personId) OR EXISTS (SELECT 1 FROM m.directors d WHERE d.id = :personId) OR EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = :personId)")
	long countMovieUsageByPersonId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m WHERE EXISTS (SELECT 1 FROM m.genres g WHERE g.id = :genreId)")
	long countMovieUsageByGenreId(@Param("genreId") Long genreId);

	boolean existsByTitle(String title);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters", "sessions", "sessions.hall" })
	@Query("""
			SELECT DISTINCT m FROM Movie m
			WHERE m.slug = :slug
			AND m.status != 'ARCHIVED'
			""")
	Optional<Movie> findBySlugWithFutureSessions(@Param("slug") String slug);
}