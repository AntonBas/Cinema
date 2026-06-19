package ua.lviv.bas.cinema.repository.cinema;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    Optional<Movie> findBySlug(String slug);

    @EntityGraph(attributePaths = {"genres", "actors", "directors", "screenwriters"})
    Optional<Movie> findMovieById(Long id);

    @Query("SELECT m.posterFileName FROM Movie m WHERE m.id = :id")
    Optional<String> findPosterFileNameById(@Param("id") Long id);

    boolean existsByTitle(String title);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE a.id = :personId) OR EXISTS (SELECT 1 FROM m.directors d WHERE d.id = :personId) OR EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = :personId)")
    long countMovieUsageByPersonId(@Param("personId") Long personId);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXISTS (SELECT 1 FROM m.genres g WHERE g.id = :genreId)")
    long countMovieUsageByGenreId(@Param("genreId") Long genreId);

    @EntityGraph(attributePaths = {"genres", "actors", "directors", "screenwriters"})
    @Query("SELECT m FROM Movie m WHERE m.slug = :slug AND m.status != 'ARCHIVED'")
    Optional<Movie> findMovieBySlug(@Param("slug") String slug);

    @Query("SELECT s FROM Session s JOIN FETCH s.hall h " +
            "WHERE s.movie.slug = :slug AND s.movie.status != 'ARCHIVED' " +
            "ORDER BY s.startTime ASC, h.name ASC")
    List<Session> findSessionsByMovieSlug(@Param("slug") String slug);
}