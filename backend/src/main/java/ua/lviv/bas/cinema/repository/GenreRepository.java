package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.projection.GenreProjection;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	@Query("SELECT new ua.lviv.bas.cinema.domain.projection.GenreProjection("
			+ "g.id, g.name, SIZE(g.movies)) FROM Genre g")
	Page<GenreProjection> findAllProjections(Pageable pageable);

	@Query("SELECT new ua.lviv.bas.cinema.domain.projection.GenreProjection("
			+ "g.id, g.name, SIZE(g.movies)) FROM Genre g "
			+ "WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
	Page<GenreProjection> searchProjectionsByName(@Param("query") String query, Pageable pageable);
}