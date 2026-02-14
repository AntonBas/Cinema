package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;
import ua.lviv.bas.cinema.domain.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

	Optional<Movie> findBySlug(String slug);

	Optional<MovieDetailProjection> findDetailProjectionById(Long id);

	@Query("""
			    SELECT m.id as id, m.title as title, m.releaseDate as releaseDate,
			           m.durationMinutes as durationMinutes
			    FROM Movie m
			    WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')))
			      AND m.status IN ('CURRENT', 'UPCOMING')
			      AND m.endShowingDate >= CURRENT_DATE
			    ORDER BY m.title
			""")
	List<MovieSessionSearchProjection> findMoviesForSession(@Param("title") String title);

	@Query("""
			    SELECT m.id as id, m.title as title, m.slug as slug,
			           m.posterFileName as posterFileName, m.durationMinutes as durationMinutes,
			           m.ageRating as ageRating, m.status as status,
			           m.releaseDate as releaseDate, m.endShowingDate as endShowingDate
			    FROM Movie m
			""")
	Page<MovieCardProjection> findMovieCards(Specification<Movie> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Override
	Page<Movie> findAll(Specification<Movie> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "genres", "actors", "directors", "screenwriters" })
	@Override
	Optional<Movie> findById(Long id);
}