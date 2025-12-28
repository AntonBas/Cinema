package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ua.lviv.bas.cinema.domain.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long>, QuerydslPredicateExecutor<Genre> {

	Page<Genre> findByNameContainingIgnoreCase(String name, Pageable pageable);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
