package ua.lviv.bas.cinema.repository.cinema;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreListProjection;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT
                g.id as id,
                g.name as name,
                SIZE(g.movies) as movieCount
            FROM Genre g
            WHERE (:query IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
            ORDER BY SIZE(g.movies) DESC, g.name ASC
            """)
    Page<GenreListProjection> findGenresByFilters(@Param("query") String query, Pageable pageable);
}