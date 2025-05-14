package ua.lviv.bas.cinema.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
	Optional<Movie> findBySlug(String slug);
}
