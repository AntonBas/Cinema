package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

}
