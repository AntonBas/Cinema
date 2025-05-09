package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Genre;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

}
