package ua.lviv.bas.cinema.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	Optional<Movie> findBySlug(String slug);

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.actors a WHERE a.id = :personId")
	long countByActorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.directors d WHERE d.id = :personId")
	long countByDirectorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.screenwriters s WHERE s.id = :personId")
	long countByScreenwritersId(@Param("personId") Long personId);

	@Query("SELECT m FROM Movie m WHERE " + "m.status = :status AND "
			+ "(:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<Movie> findByStatusWithSearch(@Param("status") MovieStatus status, @Param("search") String search,
			Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.releaseDate <= CURRENT_DATE AND " + "m.endShowingDate >= CURRENT_DATE "
			+ "ORDER BY m.releaseDate DESC")
	List<Movie> findCurrentlyShowing(Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.releaseDate <= CURRENT_DATE AND " + "m.endShowingDate >= CURRENT_DATE")
	Page<Movie> findCurrentlyShowingPage(Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.releaseDate > CURRENT_DATE " + "ORDER BY m.releaseDate")
	List<Movie> findUpcoming(Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.releaseDate > CURRENT_DATE")
	Page<Movie> findUpcomingPage(Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.endShowingDate < CURRENT_DATE")
	Page<Movie> findArchived(Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE " + "m.releaseDate <= :sessionDate AND "
			+ "m.endShowingDate >= :sessionDate AND "
			+ "(:searchTerm IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " + "ORDER BY m.title")
	List<Movie> findMoviesForSessionCreation(@Param("searchTerm") String searchTerm,
			@Param("sessionDate") LocalDate sessionDate);
}