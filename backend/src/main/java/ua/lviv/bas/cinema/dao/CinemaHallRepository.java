package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.CinemaHall;

public interface CinemaHallRepository extends JpaRepository<CinemaHall, Long> {

}
