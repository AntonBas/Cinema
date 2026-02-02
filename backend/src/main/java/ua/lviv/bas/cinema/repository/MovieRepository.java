package ua.lviv.bas.cinema.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.projection.MovieProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

	Optional<Movie> findBySlug(String slug);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.actors a WHERE a.id = :personId")
	long countByActorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.directors d WHERE d.id = :personId")
	long countByDirectorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.screenwriters s WHERE s.id = :personId")
	long countByScreenwritersId(@Param("personId") Long personId);

	@Query("SELECT m FROM Movie m WHERE m.releaseDate <= :sessionDate AND m.endShowingDate >= :sessionDate AND "
			+ "(:searchTerm IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY m.title")
	List<Movie> findMoviesForSessionCreation(@Param("searchTerm") String searchTerm,
			@Param("sessionDate") LocalDate sessionDate);

	@Query("SELECT m FROM Movie m WHERE (:searchTerm IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) "
			+ "AND m.status IN ('CURRENT', 'UPCOMING') ORDER BY m.title")
	List<Movie> findActiveMoviesForSearch(@Param("searchTerm") String searchTerm);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.posterFileName as posterFileName, "
			+ "m.durationMinutes as durationMinutes, m.ageRating as ageRating, m.status as status, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames " + "FROM Movie m")
	Page<MovieProjection> findAllProjections(Pageable pageable);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.posterFileName as posterFileName, "
			+ "m.durationMinutes as durationMinutes, m.ageRating as ageRating, m.status as status, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames "
			+ "FROM Movie m WHERE m.status = :status")
	Page<MovieProjection> findProjectionsByStatus(@Param("status") MovieStatus status, Pageable pageable);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.posterFileName as posterFileName, "
			+ "m.durationMinutes as durationMinutes, m.ageRating as ageRating, m.status as status, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames "
			+ "FROM Movie m WHERE m.releaseDate <= CURRENT_DATE AND m.endShowingDate >= CURRENT_DATE")
	Page<MovieProjection> findCurrentlyShowingProjections(Pageable pageable);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.posterFileName as posterFileName, "
			+ "m.durationMinutes as durationMinutes, m.ageRating as ageRating, m.status as status, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames "
			+ "FROM Movie m WHERE m.releaseDate > CURRENT_DATE")
	Page<MovieProjection> findUpcomingProjections(Pageable pageable);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.posterFileName as posterFileName, "
			+ "m.durationMinutes as durationMinutes, m.ageRating as ageRating, m.status as status, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames "
			+ "FROM Movie m WHERE m.endShowingDate < CURRENT_DATE")
	Page<MovieProjection> findArchivedProjections(Pageable pageable);

	@Query("SELECT m.id as id, m.title as title, m.slug as slug, m.trailerUrl as trailerUrl, "
			+ "m.description as description, m.durationMinutes as durationMinutes, "
			+ "m.releaseDate as releaseDate, m.endShowingDate as endShowingDate, "
			+ "m.status as status, m.posterFileName as posterFileName, m.ageRating as ageRating, "
			+ "(SELECT STRING_AGG(g.name, ', ') FROM m.genres g) as genreNames, "
			+ "(SELECT STRING_AGG(p.name, ', ') FROM m.actors p) as actorNames, "
			+ "(SELECT STRING_AGG(p.name, ', ') FROM m.directors p) as directorNames, "
			+ "(SELECT STRING_AGG(p.name, ', ') FROM m.screenwriters p) as screenwriterNames "
			+ "FROM Movie m WHERE m.id = :id")
	Optional<MovieProjection> findProjectionById(@Param("id") Long id);

	@Query("SELECT COUNT(m) FROM Movie m WHERE m.status = :status")
	long countByStatus(@Param("status") MovieStatus status);
}