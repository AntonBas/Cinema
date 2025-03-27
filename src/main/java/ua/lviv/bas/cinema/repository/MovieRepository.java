package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

}
