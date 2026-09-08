package ua.lviv.bas.cinema.movie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.movie.domain.Genre;
import ua.lviv.bas.cinema.movie.repository.projection.GenreListProjection;

@Repository
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public interface GenreRepository extends JpaRepository<Genre, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query(value = """
            SELECT
                g.id as id,
                g.name as name,
                COALESCE(gc.movie_count, 0) as movieCount
            FROM genres g
            LEFT JOIN (
                SELECT genre_id, COUNT(*) as movie_count
                FROM movie_genres
                GROUP BY genre_id
            ) gc ON gc.genre_id = g.id
            WHERE (:query IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
            ORDER BY COALESCE(gc.movie_count, 0) DESC, g.name ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM genres g
            WHERE (:query IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
            """, nativeQuery = true)
    Page<GenreListProjection> findGenresByFilters(@Param("query") String query, Pageable pageable);
}