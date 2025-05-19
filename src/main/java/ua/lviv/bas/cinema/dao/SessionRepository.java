package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

}
