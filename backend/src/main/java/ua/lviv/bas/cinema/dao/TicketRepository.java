package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
