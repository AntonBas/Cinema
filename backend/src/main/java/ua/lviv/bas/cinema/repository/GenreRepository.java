package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

	Page<Genre> findByNameContainingIgnoreCase(String name, Pageable pageable);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	@Query("SELECT g FROM Genre g WHERE " + "(:query IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')))")
	Page<Genre> searchByName(@Param("query") String query, Pageable pageable);
}